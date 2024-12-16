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

    // 멀티 쓰레드 사용 (ExecutorService)
    @Test
    @DisplayName("동시에 충전, 사용 요청이 들어왔을경우 순차적으로 처리되어야 합니다.")
    fun shouldProcessChargeAndUsageRequestsSequentially() {
        // given
        val threadCount = 3
        val countDownLatch = CountDownLatch(threadCount)
        val executor = Executors.newFixedThreadPool(threadCount) // 동시성 테스트를 위해 멀티 쓰레드 사용.
        val task = Runnable {
            try{
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
        for (i: Int in 1..threadCount) {
            executor.submit(task)
        }
        countDownLatch.await()

        // then
        Assertions.assertEquals(0, pointService.getUserPoint(0).point)
        Assertions.assertEquals(0, pointService.getUserPoint(1).point)
    }


    // 비동기를 사용한 테스트. (CompletableFuture)
    @Test
    @DisplayName("동시에 충전, 사용 요청이 들어왔을경우 순차적으로 처리되어야 합니다.")
    fun shouldProcessChargeAndUsageRequestsSequentiallyTestWithAsync() {
        // given
        val futures = mutableListOf<CompletableFuture<Void>>()
        val taskTimes = 3
        val task = Runnable {
            pointService.chargePoint(0, 100)
            pointService.chargePoint(1, 100)
            pointService.usePoint(1, 100)
            pointService.usePoint(0, 100)
            pointService.chargePoint(1, 100)
            pointService.usePoint(1, 100)
        }

        // when
        for (i: Int in 1..taskTimes) {
            futures += CompletableFuture.runAsync(task)
        }

        CompletableFuture.allOf(*futures.toTypedArray()).join() // 모든 작업이 끝날 때까지 대기

        // then
        Assertions.assertEquals(pointService.getUserPoint(0).point, 0)
        Assertions.assertEquals(pointService.getUserPoint(1).point, 0)
    }
}