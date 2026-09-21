package com.finmate.app.data.local

import android.content.Context
import com.finmate.app.data.model.Alert
import com.finmate.app.data.model.AppData
import com.finmate.app.data.model.Budget
import com.finmate.app.data.model.Category
import com.finmate.app.data.model.TransactionDto
import com.finmate.app.data.model.TransactionType
import com.google.gson.Gson
import java.io.File

/** Singleton: JSON-хранилище (getInstance). */
class LocalDatabase private constructor(context: Context) {
    private val file = File(context.filesDir, "db.json")
    private val gson = Gson()
    private var data: AppData = load()

    fun getCategories(): List<Category> = data.categories
    fun getBudgets(): List<Budget> = data.budgets
    fun getTransactions(): List<TransactionDto> = data.transactions
    fun getAlerts(): List<Alert> = data.alerts

    fun setCategories(items: List<Category>) {
        data = data.copy(categories = items)
        save()
    }

    fun setBudgets(items: List<Budget>) {
        data = data.copy(budgets = items)
        save()
    }

    fun setTransactions(items: List<TransactionDto>) {
        data = data.copy(transactions = items)
        save()
    }

    fun setAlerts(items: List<Alert>) {
        data = data.copy(alerts = items)
        save()
    }

    private fun load(): AppData {
        if (!file.exists()) {
            val seed = seedData()
            file.writeText(gson.toJson(seed))
            return seed
        }
        return gson.fromJson(file.readText(), AppData::class.java) ?: seedData()
    }

    private fun save() {
        file.writeText(gson.toJson(data))
    }

    companion object {
        @Volatile
        private var instance: LocalDatabase? = null

        fun getInstance(context: Context): LocalDatabase {
            return instance ?: synchronized(this) {
                instance ?: LocalDatabase(context.applicationContext).also { instance = it }
            }
        }
    }
}

private fun seedData(): AppData = AppData(
    categories = listOf(
        Category("cat-salary", "Зарплата", TransactionType.INCOME, "payments"),
        Category("cat-freelance", "Фриланс", TransactionType.INCOME, "laptop"),
        Category("cat-food", "Еда", TransactionType.EXPENSE, "restaurant"),
        Category("cat-transport", "Транспорт", TransactionType.EXPENSE, "directions_bus"),
        Category("cat-home", "Жильё", TransactionType.EXPENSE, "home"),
        Category("cat-fun", "Развлечения", TransactionType.EXPENSE, "sports_esports"),
        Category("cat-health", "Здоровье", TransactionType.EXPENSE, "favorite"),
        Category("cat-other", "Прочее", TransactionType.EXPENSE, "more_horiz")
    ),
    budgets = listOf(
        Budget("cat-food", 18000.0),
        Budget("cat-transport", 4000.0),
        Budget("cat-fun", 6000.0),
        Budget("cat-health", 3000.0)
    ),
    transactions = listOf(
        TransactionDto("tx-1", "income", 85000.0, "cat-salary", "Зарплата", "2026-09-01T09:00:00.000Z"),
        TransactionDto("tx-2", "income", 12000.0, "cat-freelance", "Правки лендинга", "2026-09-04T16:20:00.000Z"),
        TransactionDto("tx-3", "expense", 18500.0, "cat-home", "Аренда", "2026-09-02T10:00:00.000Z"),
        TransactionDto("tx-4", "expense", 2340.0, "cat-food", "Пятёрочка", "2026-09-03T18:40:00.000Z"),
        TransactionDto("tx-5", "expense", 690.0, "cat-transport", "Проездной", "2026-09-03T08:15:00.000Z"),
        TransactionDto("tx-6", "expense", 1540.0, "cat-food", "Обеды", "2026-09-05T13:10:00.000Z"),
        TransactionDto("tx-7", "expense", 2100.0, "cat-fun", "Кино", "2026-09-06T19:30:00.000Z"),
        TransactionDto("tx-8", "expense", 890.0, "cat-health", "Аптека", "2026-09-07T11:05:00.000Z"),
        TransactionDto("tx-9", "expense", 3200.0, "cat-food", "Ресторан", "2026-09-08T20:00:00.000Z"),
        TransactionDto("tx-10", "expense", 450.0, "cat-transport", "Такси", "2026-09-08T22:15:00.000Z"),
        TransactionDto("tx-11", "expense", 1670.0, "cat-food", "Магнит", "2026-09-09T12:40:00.000Z")
    ),
    alerts = emptyList()
)
