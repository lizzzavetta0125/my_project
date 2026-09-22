package com.finmate.app

import android.app.Application
import com.finmate.app.data.local.LocalDatabase
import com.finmate.app.data.repository.FinanceRepository
import com.finmate.app.domain.observer.BudgetObserver
import com.finmate.app.domain.observer.EventBus
import com.finmate.app.domain.service.FinanceService

/** Точка сборки зависимостей приложения. */
class FinMateApp : Application() {
    lateinit var financeService: FinanceService
        private set

    override fun onCreate() {
        super.onCreate()
        val db = LocalDatabase.getInstance(this)
        val repository = FinanceRepository(db)
        val eventBus = EventBus()
        BudgetObserver(eventBus, repository)
        financeService = FinanceService(repository, eventBus)
    }
}
