package com.finmate.app.domain.service

import com.finmate.app.data.model.Alert
import com.finmate.app.data.model.Budget
import com.finmate.app.data.model.Category
import com.finmate.app.data.model.Transaction
import com.finmate.app.data.model.TransactionType
import com.finmate.app.data.repository.FinanceRepository
import com.finmate.app.domain.factory.TransactionFactory
import com.finmate.app.domain.observer.EventBus
import com.finmate.app.domain.strategy.ReportContext
import com.finmate.app.domain.strategy.ReportRow
import com.finmate.app.domain.strategy.createReportStrategy
import java.time.LocalDate

data class BudgetStatus(
    val categoryId: String,
    val categoryName: String,
    val icon: String,
    val limit: Double,
    val spent: Double,
    val left: Double,
    val percent: Int,
    val over: Boolean
)

data class DashboardData(
    val balance: Double,
    val income: Double,
    val expense: Double,
    val transactionCount: Int,
    val recent: List<TransactionUi>,
    val budgets: List<BudgetStatus>,
    val alerts: List<Alert>,
    val chart: List<ReportRow>
)

data class TransactionUi(
    val transaction: Transaction,
    val categoryName: String,
    val categoryIcon: String
)

/** Facade: операции, бюджеты и отчёты. */
class FinanceService(
    private val repository: FinanceRepository,
    private val eventBus: EventBus
) {
    fun getDashboard(): DashboardData {
        val now = LocalDate.now()
        val monthTx = repository.transactionsForMonth(now.year, now.monthValue)
        val income = monthTx.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expense = monthTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        return DashboardData(
            balance = income - expense,
            income = income,
            expense = expense,
            transactionCount = monthTx.size,
            recent = listTransactions().take(6),
            budgets = getBudgetStatus(),
            alerts = repository.getAlerts().take(5),
            chart = getReport("byCategory").filter { it.type == TransactionType.EXPENSE }
        )
    }

    fun listTransactions(): List<TransactionUi> {
        val categories = repository.getCategories()
        return repository.getTransactions().map { withCategory(it, categories) }
    }

    fun listCategories(): List<Category> = repository.getCategories()

    fun createTransaction(type: TransactionType, amount: Double, categoryId: String, note: String): TransactionUi {
        val category = repository.getCategories().find { it.id == categoryId }
            ?: throw IllegalArgumentException("Категория не найдена")
        if (category.type != type) throw IllegalArgumentException("Тип не совпадает с категорией")
        if (amount <= 0) throw IllegalArgumentException("Сумма должна быть больше нуля")

        val tx = TransactionFactory.create(type, amount, categoryId, note)
        repository.addTransaction(tx)
        eventBus.emit("transaction:created", tx)
        return withCategory(tx, repository.getCategories())
    }

    fun deleteTransaction(id: String) {
        if (!repository.removeTransaction(id)) {
            throw IllegalArgumentException("Операция не найдена")
        }
    }

    fun getBudgetStatus(): List<BudgetStatus> {
        val now = LocalDate.now()
        val monthTx = repository.transactionsForMonth(now.year, now.monthValue)
        val categories = repository.getCategories()
        return repository.getBudgets().map { budget ->
            val spent = monthTx
                .filter { it.type == TransactionType.EXPENSE && it.categoryId == budget.categoryId }
                .sumOf { it.amount }
            val category = categories.find { it.id == budget.categoryId }
            val percent = if (budget.limit == 0.0) 0 else ((spent / budget.limit) * 100).toInt()
            BudgetStatus(
                categoryId = budget.categoryId,
                categoryName = category?.name ?: "Категория",
                icon = category?.icon ?: "label",
                limit = budget.limit,
                spent = spent,
                left = budget.limit - spent,
                percent = percent,
                over = spent > budget.limit
            )
        }
    }

    fun saveBudget(categoryId: String, limit: Double): Budget {
        val category = repository.getCategories().find { it.id == categoryId }
        if (category == null || category.type != TransactionType.EXPENSE) {
            throw IllegalArgumentException("Бюджет только для расходов")
        }
        repository.upsertBudget(categoryId, limit)
        return Budget(categoryId, limit)
    }

    fun getReport(strategyName: String): List<ReportRow> {
        val context = ReportContext(createReportStrategy(strategyName))
        return context.generate(repository.getTransactions(), repository.getCategories())
    }

    private fun withCategory(tx: Transaction, categories: List<Category>): TransactionUi {
        val category = categories.find { it.id == tx.categoryId }
        return TransactionUi(
            transaction = tx,
            categoryName = category?.name ?: "Без категории",
            categoryIcon = category?.icon ?: "label"
        )
    }
}
