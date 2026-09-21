package com.finmate.app.domain.strategy

import com.finmate.app.data.model.Category
import com.finmate.app.data.model.Transaction
import com.finmate.app.data.model.TransactionType

data class ReportRow(
    val key: String,
    val label: String,
    val total: Double = 0.0,
    val income: Double? = null,
    val expense: Double? = null,
    val type: TransactionType? = null
)

/** Strategy: варианты построения отчёта. */
interface ReportStrategy {
    fun execute(transactions: List<Transaction>, categories: List<Category>): List<ReportRow>
}

class ByCategoryStrategy : ReportStrategy {
    override fun execute(transactions: List<Transaction>, categories: List<Category>): List<ReportRow> {
        return transactions
            .groupBy { it.categoryId }
            .map { (categoryId, items) ->
                val category = categories.find { it.id == categoryId }
                ReportRow(
                    key = categoryId,
                    label = category?.name ?: "Без категории",
                    total = items.sumOf { it.amount },
                    type = category?.type
                )
            }
    }
}

class ByDayStrategy : ReportStrategy {
    override fun execute(transactions: List<Transaction>, categories: List<Category>): List<ReportRow> {
        return transactions
            .groupBy { it.date.take(10) }
            .toSortedMap()
            .map { (day, items) ->
                val income = items.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                val expense = items.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                ReportRow(
                    key = day,
                    label = formatDay(day),
                    income = income,
                    expense = expense,
                    total = income - expense
                )
            }
    }

    private fun formatDay(iso: String): String {
        val parts = iso.split("-")
        if (parts.size != 3) return iso
        return "${parts[2]}.${parts[1]}.${parts[0]}"
    }
}

class ByTypeStrategy : ReportStrategy {
    override fun execute(transactions: List<Transaction>, categories: List<Category>): List<ReportRow> {
        val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        return listOf(
            ReportRow(key = "income", label = "Доходы", total = income, type = TransactionType.INCOME),
            ReportRow(key = "expense", label = "Расходы", total = expense, type = TransactionType.EXPENSE)
        )
    }
}

class ReportContext(private var strategy: ReportStrategy) {
    fun setStrategy(strategy: ReportStrategy) {
        this.strategy = strategy
    }

    fun generate(transactions: List<Transaction>, categories: List<Category>): List<ReportRow> {
        return strategy.execute(transactions, categories)
    }
}

fun createReportStrategy(name: String): ReportStrategy = when (name) {
    "byDay" -> ByDayStrategy()
    "byType" -> ByTypeStrategy()
    else -> ByCategoryStrategy()
}
