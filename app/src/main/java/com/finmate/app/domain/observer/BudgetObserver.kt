package com.finmate.app.domain.observer

import com.finmate.app.data.model.Alert
import com.finmate.app.data.model.Transaction
import com.finmate.app.data.model.TransactionType
import com.finmate.app.data.repository.FinanceRepository
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

/** Observer: предупреждение при превышении бюджета. */
class BudgetObserver(
    eventBus: EventBus,
    private val repository: FinanceRepository
) {
    init {
        eventBus.subscribe("transaction:created") { payload ->
            if (payload is Transaction) onTransaction(payload)
        }
    }

    private fun onTransaction(tx: Transaction) {
        if (tx.type != TransactionType.EXPENSE) return
        val budget = repository.getBudgets().find { it.categoryId == tx.categoryId } ?: return
        val date = OffsetDateTime.parse(tx.date).withOffsetSameInstant(ZoneOffset.UTC)
        val monthKey = "${date.year}-${date.monthValue}"
        val spent = repository.transactionsForMonth(date.year, date.monthValue)
            .filter { it.type == TransactionType.EXPENSE && it.categoryId == tx.categoryId }
            .sumOf { it.amount }
        if (spent <= budget.limit) return

        repository.addAlertUnique(
            Alert(
                id = UUID.randomUUID().toString(),
                categoryId = tx.categoryId,
                monthKey = monthKey,
                spent = spent,
                limit = budget.limit,
                message = "Бюджет превышен: потрачено ${spent.toInt()} ₽ из ${budget.limit.toInt()} ₽",
                createdAt = java.time.Instant.now().toString()
            )
        )
    }
}
