package io.hhplus.tdd

import io.hhplus.tdd.point.PointHistory
import io.hhplus.tdd.point.PointRepository
import io.hhplus.tdd.point.PointService
import io.hhplus.tdd.point.UserPoint
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

@Suppress("unused")
class PointServiceUnitTest {

    // 서비스 로직 단위테스트
    // 다른 구현체에 의존하지 않은 상태로 순수 비즈니스 로직만 테스트
    // 테스트는 실패 케이스를 중심으로 진행 (성공 테스트는 통합테스트로 충분할 것으로 예상했습니다.)

    @Mock
    private lateinit var pointRepository: PointRepository

    @InjectMocks
    private lateinit var pointService: PointService


    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    // 포인트 조회시 ID 음수
    @Test
    @DisplayName("포인트 조회 Id가 음수일 경우 IllegalArgumentException 을 던집니다.")
    fun shouldThrowExceptionWhenUserIdIsNegativeForPointRetrieval() {
        // given
        val userId = -1L
        val userPoint = UserPoint(userId, 0, System.currentTimeMillis())
        `when`(pointRepository.getPoint(userId)).thenReturn(userPoint)

        // when - then
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            pointService.getUserPoint(userId)
        }
    }

    // 이용 내역 조회시 ID 음수
    @Test
    @DisplayName("이용 내역 조회 Id가 음수일 경우 IllegalArgumentException 을 던집니다.")
    fun shouldThrowExceptionWhenUserIdIsNegativeForHistoryRetrieval() {
        // given
        val userId = -1L
        val userPoint = emptyList<PointHistory>()
        `when`(pointRepository.getPointHistory(userId)).thenReturn(userPoint)

        // when - then
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            pointService.getUserPoint(userId)
        }
    }

    // 포인트 충전시 ID 음수
    @Test
    @DisplayName("포인트 충전 Id가 음수일 경우 IllegalArgumentException 을 던집니다.")
    fun shouldThrowExceptionWhenUserIdIsNegativeForChargePoint() {
        // given
        val userId = -1L
        val chargeAmount = 0L
        val userPoint = UserPoint(userId, 0, System.currentTimeMillis())
        `when`(pointRepository.getPoint(userId)).thenReturn(userPoint)

        // when - then
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            pointService.chargePoint(userId, chargeAmount)
        }
    }

    // 포인트 사용시 ID 음수
    @Test
    @DisplayName("포인트 사용 Id가 음수일 경우 IllegalArgumentException 을 던집니다.")
    fun shouldThrowExceptionWhenUserIdIsNegativeForUsePoint() {
        // given
        val userId = -1L
        val usageAmount = 50L
        val userPoint = UserPoint(userId, 100, System.currentTimeMillis())
        `when`(pointRepository.getPoint(userId)).thenReturn(userPoint)

        // when - then
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            pointService.usePoint(userId, usageAmount)
        }
    }

    // 0 이하 충전
    @Test
    @DisplayName("충전 포인트가 0 이하인경우 IllegalArgumentException 을 던집니다.")
    fun shouldThrowExceptionWhenChargePointsAreZeroOrLess() {
        // given
        val userId = 0L
        val chargeAmount = 0L
        val userPoint = UserPoint(userId, 0, System.currentTimeMillis())
        `when`(pointRepository.getPoint(userId)).thenReturn(userPoint)

        // when - then
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            pointService.chargePoint(userId, chargeAmount)
        }
    }

    // 최대 포인트 이상 충전
    @Test
    @DisplayName("충전 포인트가 최대 포인트를 넘을 경우 IllegalArgumentException 을 던집니다.")
    fun shouldThrowExceptionWhenUsagePointsAreMoreThanMaximum() {
        // given
        val userId = 0L
        val chargeAmount = 1000L
        val maximumPoint = PointService.MAX_POINTS
        val userPoint = UserPoint(userId, maximumPoint, System.currentTimeMillis())
        `when`(pointRepository.getPoint(userId)).thenReturn(userPoint)

        // when - then
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            pointService.chargePoint(userId, chargeAmount)
        }

    }

    // 0 이하 사용
    @Test
    @DisplayName("사용 포인트가 0 이하인 경우 IllegalArgumentException 을 던집니다.")
    fun shouldThrowExceptionWhenUsagePointsAreZeroOrLess() {
        // given
        val userId = 0L
        val usageAmount = 0L
        val userPoint = UserPoint(userId, 100, System.currentTimeMillis())
        `when`(pointRepository.getPoint(userId)).thenReturn(userPoint)

        // when - then
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            pointService.chargePoint(userId, usageAmount)
        }
    }

    // 보유 포인트 이상 사용
    @Test
    @DisplayName("사용 포인트가 보유 포인트를 초과할 경우 IllegalArgumentException 을 던집니다.")
    fun shouldThrowExceptionWhenUsagePointsExceedAvailablePoints() {
        // given
        val userId = 0L
        val usageAmount = 100L
        val chargedUserPoint = UserPoint(userId, 50L, System.currentTimeMillis())
        `when`(pointRepository.getPoint(userId)).thenReturn(chargedUserPoint)

        // when - then
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            pointService.usePoint(userId, usageAmount)
        }
    }
}