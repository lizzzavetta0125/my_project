package com.finmate.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.finmate.app.data.model.TransactionType
import com.finmate.app.domain.service.BudgetStatus
import com.finmate.app.domain.service.DashboardData
import com.finmate.app.domain.service.FinanceService
import com.finmate.app.domain.service.TransactionUi
import com.finmate.app.domain.strategy.ReportRow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FinanceUiState(
    val dashboard: DashboardData? = null,
    val transactions: List<TransactionUi> = emptyList(),
    val budgets: List<BudgetStatus> = emptyList(),
    val reportRows: List<ReportRow> = emptyList(),
    val reportStrategy: String = "byCategory",
    val message: String? = null,
    val error: String? = null
)

/** ViewModel: состояние и действия экранов. */
class FinanceViewModel(
    private val financeService: FinanceService
) : ViewModel() {

    private val _state = MutableStateFlow(FinanceUiState())
    val state: StateFlow<FinanceUiState> = _state.asStateFlow()

    init {
        refreshAll()
    }

    fun refreshAll() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    dashboard = financeService.getDashboard(),
                    transactions = financeService.listTransactions(),
                    budgets = financeService.getBudgetStatus(),
                    reportRows = financeService.getReport(it.reportStrategy),
                    error = null
                )
            }
        }
    }

    fun setReportStrategy(name: String) {
        _state.update {
            it.copy(
                reportStrategy = name,
                reportRows = financeService.getReport(name)
            )
        }
    }

    fun addTransaction(type: TransactionType, amount: Double, categoryId: String, note: String) {
        runCatching {
            financeService.createTransaction(type, amount, categoryId, note)
        }.onSuccess {
            refreshAll()
            _state.update { state -> state.copy(message = "Операция добавлена") }
        }.onFailure { e ->
            _state.update { it.copy(error = e.message) }
        }
    }

    fun deleteTransaction(id: String) {
        runCatching { financeService.deleteTransaction(id) }
            .onSuccess {
                refreshAll()
                _state.update { it.copy(message = "Операция удалена") }
            }
            .onFailure { e ->
                _state.update { it.copy(error = e.message) }
            }
    }

    fun saveBudget(categoryId: String, limit: Double) {
        runCatching { financeService.saveBudget(categoryId, limit) }
            .onSuccess {
                refreshAll()
                _state.update { it.copy(message = "Бюджет сохранён") }
            }
            .onFailure { e ->
                _state.update { it.copy(error = e.message) }
            }
    }

    fun consumeMessage() {
        _state.update { it.copy(message = null, error = null) }
    }

    fun categories() = financeService.listCategories()

    class Factory(private val service: FinanceService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FinanceViewModel(service) as T
        }
    }
}
