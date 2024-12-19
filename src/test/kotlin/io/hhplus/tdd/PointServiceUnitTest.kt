package io.hhplus.tdd

import io.hhplus.tdd.point.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations


class PointServiceUnitTest {

    // 서비스 로직 단위테스트
    // 다른 구현체에 의존하지 않은 상태로 순수 비즈니스 로직만 테스트
    // 테스트는 실패 케이스를 중심으로 진행
    // - 성공 케이스는 비즈니스 로직 상 '해야하는 일' 을 하는가를 기준으로 테스트 하였습니다. ex) 충전, 사용 시 히스토리 추가 메서드를 호출하는가.
    // - 단순 기능 동작의 성공 여부는 통합테스트에서 검증이 될거라 생각하여 테스트 하지 않았습니다.


    @Mock // 가짜 객체 지정
    private lateinit var pointRepository: PointRepository

    @InjectMocks // 가짜 객체를 주입 받는 대상
    private lateinit var pointService: PointService


    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this) // 가짜 객체와 서비스 객체를 생성(초기화) 및 주입
    }

    // 포인트 조회시 ID 음수
    // 단순 조회 API 라서 비즈니스 로직이 없기에 해당 테스트로 인해 정책을 추가하게 되면서
    // 추후 리팩토링에서의 변화를 보다 잘 느끼기 위해 테스트를 추가하였습니다.
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
    // 단순 조회 API 라서 비즈니스 로직이 없기에 해당 테스트로 인해 정책을 추가하게 되면서
    // 추후 리팩토링에서의 변화를 보다 잘 느끼기 위해 테스트를 추가하였습니다.
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
    // 단순 조회 API 라서 비즈니스 로직이 없기에 해당 테스트로 인해 정책을 추가하게 되면서
    // 추후 리팩토링에서의 변화를 보다 잘 느끼기 위해 테스트를 추가하였습니다.
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
    // 단순 조회 API 라서 비즈니스 로직이 없기에 해당 테스트로 인해 정책을 추가하게 되면서
    // 추후 리팩토링에서의 변화를 보다 잘 느끼기 위해 테스트를 추가하였습니다.
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
    // 실패 케이스 추가. 비즈니스 로직상 들어가야할 내용(정책)을 테스트로 작성.
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
    // 실패 케이스 추가. 비즈니스 로직상 들어가야할 내용(정책)을 테스트로 작성.
    @Test
    @DisplayName("충전 포인트가 최대 포인트를 넘을 경우 IllegalArgumentException 을 던집니다.")
    fun shouldThrowExceptionWhenUsagePointsAreMoreThanMaximum() {
        // given
        val userId = 0L
        val chargeAmount = 1000L
        val maximumPoint = 1_000_000L
        val userPoint = UserPoint(userId, maximumPoint, System.currentTimeMillis())
        `when`(pointRepository.getPoint(userId)).thenReturn(userPoint)

        // when - then
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            pointService.chargePoint(userId, chargeAmount)
        }

    }

    // 포인트 충전시 히스토리추가
    // 비즈니스 로직상 해야하는 동작을 진행하는지 체크, 리팩토링 등에서 기능의 누락을 방지
    @Test
    @DisplayName("포인트 충전시 히스토리에 추가되어야 합니다.")
    fun shouldAddHistoryWhenChargePoint() {
        // given
        val userId = 1L
        val usageAmount = 50L
        val userPoint = UserPoint(userId, 100, System.currentTimeMillis())
        `when`(pointRepository.getPoint(userId)).thenReturn(userPoint)

        // when
        pointService.chargePoint(userId, usageAmount)

        // then
        verify(pointRepository).addPointHistory(userId, usageAmount, TransactionType.CHARGE)
    }


    // 0 이하 사용
    // 실패 케이스 추가. 비즈니스 로직상 들어가야할 내용(정책)을 테스트로 작성.
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
    // 실패 케이스 추가. 비즈니스 로직상 들어가야할 내용(정책)을 테스트로 작성.
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

    // 포인트 사용시 히스토리 추가
    // 비즈니스 로직상 해야하는 동작을 진행하는지 체크, 리팩토링 등에서 기능의 누락을 방지
    @Test
    @DisplayName("포인트 사용시 히스토리에 추가되어야 합니다.")
    fun shouldAddHistoryWhenUsePoint() {
        // given
        val userId = 1L
        val usageAmount = 50L
        val userPoint = UserPoint(userId, 100, System.currentTimeMillis())
        `when`(pointRepository.getPoint(userId)).thenReturn(userPoint)

        // when
        pointService.usePoint(userId, usageAmount)

        // then
        verify(pointRepository).addPointHistory(userId, usageAmount, TransactionType.USE)
    }
}