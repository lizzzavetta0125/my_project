package com.finmate.app.domain.factory

import com.finmate.app.data.model.ExpenseTransaction
import com.finmate.app.data.model.IncomeTransaction
import com.finmate.app.data.model.Transaction
import com.finmate.app.data.model.TransactionDto
import com.finmate.app.data.model.TransactionType
import java.util.UUID

/** Factory Method: создание Income/Expense. */
object TransactionFactory {
    fun create(
        type: TransactionType,
        amount: Double,
        categoryId: String,
        note: String,
        date: String = java.time.Instant.now().toString(),
        id: String = UUID.randomUUID().toString()
    ): Transaction {
        return when (type) {
            TransactionType.INCOME -> IncomeTransaction(id, amount, categoryId, note, date)
            TransactionType.EXPENSE -> ExpenseTransaction(id, amount, categoryId, note, date)
        }
    }

    fun fromDto(dto: TransactionDto): Transaction {
        val type = TransactionType.valueOf(dto.type.uppercase())
        return create(type, dto.amount, dto.categoryId, dto.note, dto.date, dto.id)
    }

    fun toDto(tx: Transaction): TransactionDto = TransactionDto(
        id = tx.id,
        type = tx.type.name.lowercase(),
        amount = tx.amount,
        categoryId = tx.categoryId,
        note = tx.note,
        date = tx.date
    )
}
