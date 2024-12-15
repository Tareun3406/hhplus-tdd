package io.hhplus.tdd.point

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import org.springframework.stereotype.Repository

@Repository
class PointRepository(
    private val userPointTable: UserPointTable,
    private val pointHistoryTable: PointHistoryTable
) {
    fun addPointHistory(id: Long, amount: Long, transactionType: TransactionType): PointHistory {
        return pointHistoryTable.insert(id, amount, transactionType, System.currentTimeMillis())
    }

    fun getPointHistory(id: Long): List<PointHistory> {
        return pointHistoryTable.selectAllByUserId(id)
    }

    fun updatePoint(id: Long, amount: Long): UserPoint {
        return userPointTable.insertOrUpdate(id, amount)
    }

    fun getPoint(id: Long): UserPoint {
        return userPointTable.selectById(id)
    }
}