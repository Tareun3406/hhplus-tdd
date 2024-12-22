package io.hhplus.tdd

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import io.hhplus.tdd.point.PointRepository
import io.hhplus.tdd.point.PointService

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

class PointServiceIntegrationTest {
    private val userPointTable = UserPointTable()
    private val pointHistoryTable = PointHistoryTable()
    private val pointRepository = PointRepository(userPointTable, pointHistoryTable)
    private val pointService = PointService(pointRepository)

    // 통합 테스트는 객체/기능 간의 연계가 잘 이루어지는 지를 포함하여 테스트합니다.
    // 기능이 맞물렸을 때 정상적으로 동작하는지를 확인하는 테스트라고 생각하여 성공 케이스 위주로 작성했습니다.

    // ExecutorService 를 이용한 비동기 테스트
    // 멀티 쓰레드를 통해 동시성 문제를 유도
    @Test
    @DisplayName("동시에 충전, 사용 요청이 들어왔을경우 순차적으로 처리되어야 합니다.")
    fun shouldProcessChargeAndUsageRequestsSequentially() {
        // given
        val threadCount = 5
        val countDownLatch = CountDownLatch(threadCount)
        val executor = Executors.newFixedThreadPool(threadCount) // 동시성 테스트를 위해 멀티 쓰레드 사용.

        val task = Runnable {
            try {
                pointService.chargePoint(0, 100)
                pointService.chargePoint(1, 100)
                pointService.usePoint(1, 100)
                pointService.usePoint(0, 100)
                pointService.chargePoint(1, 100)
                pointService.usePoint(1, 100)
            } finally {
                countDownLatch.countDown()
            }
        }

        // when
        for (i: Int in 0 .. threadCount) {
            executor.submit(task)
        }
        countDownLatch.await()

        // then
        Assertions.assertEquals(0, pointService.getUserPoint(0).point)
        Assertions.assertEquals(0, pointService.getUserPoint(1).point)
    }


    // CompletableFuture 를 사용한 비동기 테스트
    // try - finally 와 countDownLatch 를 사용한 코드가 개인적으로 맘에 안들어서 다른 방법을 찾아보았습니다.
    // 내부적으론 마찬가지로 ExecutorService 사용하기에 기능상 큰 차이는 없는 것 같습니다.
    @Test
    @DisplayName("동시에 충전, 사용 요청이 들어왔을경우 순차적으로 처리되어야 합니다.")
    fun shouldProcessChargeAndUsageRequestsSequentiallyTestWithAsync() {
        // given
        val futures = mutableListOf<CompletableFuture<Void>>()
        val taskTimes = 5
        val task = Runnable {
            pointService.chargePoint(0, 100)
            pointService.chargePoint(1, 100)
            pointService.usePoint(1, 100)
            pointService.usePoint(0, 100)
            pointService.chargePoint(1, 100)
            pointService.usePoint(1, 100)
        }

        // when
        for (i: Int in 0 .. taskTimes) {
            futures += CompletableFuture.runAsync(task)
        }
        CompletableFuture.allOf(*futures.toTypedArray()).join() // 모든 작업이 끝날 때까지 대기

        // then
        Assertions.assertEquals(0, pointService.getUserPoint(0).point)
        Assertions.assertEquals(0, pointService.getUserPoint(1).point)
    }

    // 충전후 조회가 잘 이루어지는지 테스트합니다. (기능의 연계)
    @Test
    @DisplayName("포인트 충전시 충전된 값으로 조회됩니다.")
    fun shouldSuccessfullyChargePointsWhenRequested() {
        // given
        val userId = 2L
        val chargeAmount = 100L

        // when
        pointService.chargePoint(userId, chargeAmount)

        // then
        Assertions.assertEquals(chargeAmount, pointService.getUserPoint(userId).point)
    }

    // 사용후 조회가 잘 이루어지는지 테스트합니다. (기능의 연계)
    // fixme DB 에 기본 데이터가 없기에 이전 테스트에서 충전한 데이터를 사용합니다.
    //  테스트간 의존성이 생겨서 이러면 안될것 같은 생각이 드는데 해결에 도움이 되는 방법, 노하우, 키워드가 있다면 알고싶습니다.
    @Test
    @DisplayName("포인트 사용시 사용후 값으로 조회됩니다.")
    fun shouldSuccessfullyUsePointsWhenRequested() {
        // given
        val userId = 2L
        val useAmount = 100L

        // when
        pointService.chargePoint(userId, useAmount)

        // then
        Assertions.assertEquals(useAmount, pointService.getUserPoint(userId).point)
    }

}