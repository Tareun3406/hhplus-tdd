package io.hhplus.tdd.point

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock

@Service
class PointService(
    private val pointRepository: PointRepository
) {
    companion object {
        const val MAX_POINTS = 1_000_000L
    }

    private val logger: Logger = LoggerFactory.getLogger(javaClass)
    private val locks = ConcurrentHashMap<Long, ReentrantLock>() // 사용자별 ReentrantLock 관리

    // 포인트 조회
    fun getUserPoint(id: Long): UserPoint{
        val lock = locks.computeIfAbsent(id) { ReentrantLock(true) }
        try {
            val startTime = System.currentTimeMillis()
            lock.lock()
            logger.info("Lock 획득에 걸린 시간: " + (System.currentTimeMillis() - startTime))

            if (id < 0) {
                throw IllegalArgumentException("Id 는 음수가 될 수 없습니다.")
            }

            return pointRepository.getPoint(id)
        } finally {
            lock.unlock()
        }
    }

    // 포인트 이용 내역
    fun getPointHistory(id: Long): List<PointHistory> {
        val lock = locks.computeIfAbsent(id) { ReentrantLock(true) }
        try {
            val startTime = System.currentTimeMillis()
            lock.lock()
            logger.info("Lock 획득에 걸린 시간: " + (System.currentTimeMillis() - startTime))

        if (id < 0) {
            throw IllegalArgumentException("Id 는 음수가 될 수 없습니다.")
        }

        return pointRepository.getPointHistory(id)

        } finally {
            lock.unlock()
        }
    }

    // 포인트 충전
    fun chargePoint(id: Long, amount: Long): UserPoint {
        val lock = locks.computeIfAbsent(id) { ReentrantLock(true) }
        try {
            val startTime = System.currentTimeMillis()
            lock.lock()
            logger.info("Lock 획득에 걸린 시간: " + (System.currentTimeMillis() - startTime))


            val userPoint = pointRepository.getPoint(id)

            if (id < 0) {
                throw IllegalArgumentException("Id 는 음수가 될 수 없습니다.")
            } else if (amount <= 0) {
                throw IllegalArgumentException("충전 포인트는 양수만 가능합니다.")
            } else if (userPoint.point + amount > MAX_POINTS) {
                throw IllegalArgumentException("포인트는 ${MAX_POINTS}를 넘을 수 없습니다.")
            }

            pointRepository.addPointHistory(id, amount, TransactionType.CHARGE)
            return pointRepository.updatePoint(id, userPoint.point + amount)
        } finally {
            lock.unlock()
        }
    }

    // 포인트 사용
    fun usePoint(id: Long, amount: Long): UserPoint {
        val lock = locks.computeIfAbsent(id) { ReentrantLock(true) }
        try {
            val startTime = System.currentTimeMillis()
            lock.lock()
            logger.info("Lock 획득에 걸린 시간: " + (System.currentTimeMillis() - startTime))

            val userPoint = pointRepository.getPoint(id)

            if (id < 0) {
                throw IllegalArgumentException("Id 는 음수가 될 수 없습니다.")
            } else if (amount <= 0) {
                throw IllegalArgumentException("사용 포인트는 양수만 가능합니다.")
            } else if (userPoint.point < amount) {
                throw IllegalArgumentException("사용 포인트는 보유 포인트를 초과할 수 없습니다.")
            }

            pointRepository.addPointHistory(id, amount, TransactionType.USE)
            return pointRepository.updatePoint(id, userPoint.point - amount)
        } finally {
            lock.unlock()
        }
    }
}