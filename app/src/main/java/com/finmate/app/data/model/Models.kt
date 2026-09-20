package com.finmate.app.data.model

enum class TransactionType {
    INCOME,
    EXPENSE
}

data class Category(
    val id: String,
    val name: String,
    val type: TransactionType,
    val icon: String
)

data class Budget(
    val categoryId: String,
    val limit: Double
)

data class Alert(
    val id: String,
    val categoryId: String,
    val monthKey: String,
    val spent: Double,
    val limit: Double,
    val message: String,
    val createdAt: String
)

/** Базовый класс операции (Income / Expense). */
sealed class Transaction(
    open val id: String,
    open val amount: Double,
    open val categoryId: String,
    open val note: String,
    open val date: String
) {
    abstract val type: TransactionType
    abstract fun signedAmount(): Double
}

data class IncomeTransaction(
    override val id: String,
    override val amount: Double,
    override val categoryId: String,
    override val note: String,
    override val date: String
) : Transaction(id, amount, categoryId, note, date) {
    override val type = TransactionType.INCOME
    override fun signedAmount(): Double = amount
}

data class ExpenseTransaction(
    override val id: String,
    override val amount: Double,
    override val categoryId: String,
    override val note: String,
    override val date: String
) : Transaction(id, amount, categoryId, note, date) {
    override val type = TransactionType.EXPENSE
    override fun signedAmount(): Double = -amount
}

data class AppData(
    val categories: List<Category> = emptyList(),
    val budgets: List<Budget> = emptyList(),
    val transactions: List<TransactionDto> = emptyList(),
    val alerts: List<Alert> = emptyList()
)

/** DTO операции для Gson. */
data class TransactionDto(
    val id: String,
    val type: String,
    val amount: Double,
    val categoryId: String,
    val note: String,
    val date: String
)
