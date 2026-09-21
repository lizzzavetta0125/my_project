package com.finmate.app.data.repository

import com.finmate.app.data.local.LocalDatabase
import com.finmate.app.data.model.Alert
import com.finmate.app.data.model.Budget
import com.finmate.app.data.model.Category
import com.finmate.app.data.model.Transaction
import com.finmate.app.domain.factory.TransactionFactory
import java.time.OffsetDateTime
import java.time.ZoneOffset

/** Repository: чтение и запись данных. */
class FinanceRepository(private val db: LocalDatabase) {

    fun getCategories(): List<Category> = db.getCategories()

    fun getTransactions(): List<Transaction> =
        db.getTransactions().map(TransactionFactory::fromDto)
            .sortedByDescending { it.date }

    fun getBudgets(): List<Budget> = db.getBudgets()

    fun getAlerts(): List<Alert> = db.getAlerts().asReversed().take(10)

    fun addTransaction(tx: Transaction) {
        val list = db.getTransactions().toMutableList()
        list.add(TransactionFactory.toDto(tx))
        db.setTransactions(list)
    }

    fun removeTransaction(id: String): Boolean {
        val list = db.getTransactions()
        val next = list.filterNot { it.id == id }
        if (next.size == list.size) return false
        db.setTransactions(next)
        return true
    }

    fun upsertBudget(categoryId: String, limit: Double) {
        val list = db.getBudgets().toMutableList()
        val index = list.indexOfFirst { it.categoryId == categoryId }
        val budget = Budget(categoryId, limit)
        if (index == -1) list.add(budget) else list[index] = budget
        db.setBudgets(list)
    }

    fun addAlertUnique(alert: Alert) {
        val exists = db.getAlerts().any {
            it.categoryId == alert.categoryId && it.monthKey == alert.monthKey
        }
        if (exists) return
        db.setAlerts(db.getAlerts() + alert)
    }

    fun transactionsForMonth(year: Int, month: Int): List<Transaction> {
        return getTransactions().filter {
            val date = OffsetDateTime.parse(it.date).withOffsetSameInstant(ZoneOffset.UTC)
            date.year == year && date.monthValue == month
        }
    }
}
