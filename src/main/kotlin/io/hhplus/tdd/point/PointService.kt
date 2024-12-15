package io.hhplus.tdd.point

import org.springframework.stereotype.Service

@Service
class PointService(
    private val pointRepository: PointRepository
) {

    // 포인트 조회
    fun getUserPoint(id: Long): UserPoint{
        return pointRepository.getPoint(id)
    }

    // 포인트 이용 내역
    fun getPointHistory(id: Long): List<PointHistory> {
        return pointRepository.getPointHistory(id)
    }

    // 포인트 충전
    fun chargePoint(id: Long, amount: Long): UserPoint {
        val userPoint = pointRepository.getPoint(id)
        pointRepository.addPointHistory(id, amount, TransactionType.CHARGE)
        return pointRepository.updatePoint(id, userPoint.point + amount)
    }

    // 포인트 사용
    fun usePoint(id: Long, amount: Long): UserPoint {
        val userPoint = pointRepository.getPoint(id)
        pointRepository.addPointHistory(id, amount, TransactionType.USE)
        return pointRepository.updatePoint(id, userPoint.point - amount)
    }
}