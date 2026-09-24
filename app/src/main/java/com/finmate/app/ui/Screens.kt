package com.finmate.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.finmate.app.FinMateApp
import com.finmate.app.data.model.TransactionType
import com.finmate.app.domain.service.TransactionUi
import com.finmate.app.ui.theme.ExpenseColor
import com.finmate.app.ui.theme.IncomeColor
import java.text.NumberFormat
import java.util.Locale

private enum class Tab(val title: String) {
    DASHBOARD("Дашборд"),
    TRANSACTIONS("Операции"),
    BUDGETS("Бюджеты"),
    REPORTS("Отчёты")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinMateAppRoot(app: FinMateApp) {
    val vm: FinanceViewModel = viewModel(factory = FinanceViewModel.Factory(app.financeService))
    val state by vm.state.collectAsState()
    var tab by remember { mutableStateOf(Tab.DASHBOARD) }
    var showAdd by remember { mutableStateOf(false) }
    var showBudget by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.message, state.error) {
        val text = state.message ?: state.error
        if (text != null) {
            snackbar.showSnackbar(text)
            vm.consumeMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(tab.title)
                        Text(
                            text = when (tab) {
                                Tab.DASHBOARD -> "Сводка за текущий месяц"
                                Tab.TRANSACTIONS -> "Все доходы и расходы"
                                Tab.BUDGETS -> "Лимиты по категориям"
                                Tab.REPORTS -> "Один интерфейс — разные стратегии"
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) {
                Icon(Icons.Default.Add, contentDescription = "Добавить")
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == Tab.DASHBOARD,
                    onClick = { tab = Tab.DASHBOARD },
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Дашборд") }
                )
                NavigationBarItem(
                    selected = tab == Tab.TRANSACTIONS,
                    onClick = { tab = Tab.TRANSACTIONS },
                    icon = { Icon(Icons.Default.List, null) },
                    label = { Text("Операции") }
                )
                NavigationBarItem(
                    selected = tab == Tab.BUDGETS,
                    onClick = { tab = Tab.BUDGETS },
                    icon = { Icon(Icons.Default.PieChart, null) },
                    label = { Text("Бюджеты") }
                )
                NavigationBarItem(
                    selected = tab == Tab.REPORTS,
                    onClick = { tab = Tab.REPORTS },
                    icon = { Icon(Icons.Default.ShowChart, null) },
                    label = { Text("Отчёты") }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            when (tab) {
                Tab.DASHBOARD -> DashboardScreen(state)
                Tab.TRANSACTIONS -> TransactionsScreen(state) { vm.deleteTransaction(it) }
                Tab.BUDGETS -> BudgetsScreen(state) { showBudget = true }
                Tab.REPORTS -> ReportsScreen(state) { vm.setReportStrategy(it) }
            }
        }
    }

    if (showAdd) {
        AddTransactionDialog(
            categories = vm.categories(),
            onDismiss = { showAdd = false },
            onSave = { type, amount, categoryId, note ->
                vm.addTransaction(type, amount, categoryId, note)
                showAdd = false
            }
        )
    }

    if (showBudget) {
        BudgetDialog(
            categories = vm.categories().filter { it.type == TransactionType.EXPENSE },
            onDismiss = { showBudget = false },
            onSave = { categoryId, limit ->
                vm.saveBudget(categoryId, limit)
                showBudget = false
            }
        )
    }
}

@Composable
private fun DashboardScreen(state: FinanceUiState) {
    val dash = state.dashboard ?: return
    val max = dash.chart.maxOfOrNull { it.total }?.coerceAtLeast(1.0) ?: 1.0

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("Баланс", money(dash.balance), Modifier.weight(1f))
                StatCard("Доходы", money(dash.income), Modifier.weight(1f), IncomeColor)
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("Расходы", money(dash.expense), Modifier.weight(1f), ExpenseColor)
                StatCard("Операций", dash.transactionCount.toString(), Modifier.weight(1f))
            }
        }
        item {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Расходы по категориям", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    dash.chart.forEach { row ->
                        Text(row.label, style = MaterialTheme.typography.bodySmall)
                        LinearProgressIndicator(
                            progress = { (row.total / max).toFloat() },
                            modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(999.dp))
                        )
                        Text(money(row.total), fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Предупреждения", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    if (dash.alerts.isEmpty()) {
                        Text("Предупреждений нет")
                    } else {
                        dash.alerts.forEach {
                            Text(it.message, color = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(6.dp))
                        }
                    }
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Последние операции", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    dash.recent.forEach { TxRow(it) }
                }
            }
        }
    }
}

@Composable
private fun TransactionsScreen(state: FinanceUiState, onDelete: (String) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(state.transactions, key = { it.transaction.id }) { item ->
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                Row(
                    Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        TxRow(item)
                    }
                    IconButton(onClick = { onDelete(item.transaction.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = ExpenseColor)
                    }
                }
            }
        }
    }
}

@Composable
private fun BudgetsScreen(state: FinanceUiState, onAdd: () -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(state.budgets) { budget ->
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text(budget.categoryName, fontWeight = FontWeight.Bold)
                    Text("${money(budget.spent)} из ${money(budget.limit)}")
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { (budget.percent / 100f).coerceAtMost(1f) },
                        modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(999.dp)),
                        color = if (budget.over) ExpenseColor else MaterialTheme.colorScheme.primary
                    )
                    Text(if (budget.over) "сверх лимита" else "осталось ${money(budget.left)}")
                }
            }
        }
        item {
            Button(onClick = onAdd, modifier = Modifier.fillMaxWidth()) {
                Text("Задать лимит")
            }
        }
    }
}

@Composable
private fun ReportsScreen(state: FinanceUiState, onStrategy: (String) -> Unit) {
    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = state.reportStrategy == "byCategory",
                onClick = { onStrategy("byCategory") },
                label = { Text("По категориям") }
            )
            FilterChip(
                selected = state.reportStrategy == "byDay",
                onClick = { onStrategy("byDay") },
                label = { Text("По дням") }
            )
            FilterChip(
                selected = state.reportStrategy == "byType",
                onClick = { onStrategy("byType") },
                label = { Text("По типу") }
            )
        }
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.reportRows) { row ->
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(row.label, fontWeight = FontWeight.SemiBold)
                        Text(
                            if (row.income != null) {
                                "${money(row.income)} / ${money(row.expense ?: 0.0)}"
                            } else money(row.total)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(label, style = MaterialTheme.typography.bodySmall)
            Text(value, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}

@Composable
private fun TxRow(item: TransactionUi) {
    val tx = item.transaction
    val sign = if (tx.type == TransactionType.INCOME) "+" else "−"
    val color = if (tx.type == TransactionType.INCOME) IncomeColor else ExpenseColor
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(item.categoryName, fontWeight = FontWeight.SemiBold)
            Text(
                "${tx.note.ifBlank { "Без комментария" }} · ${tx.date.take(10)}",
                style = MaterialTheme.typography.bodySmall
            )
        }
        Text("$sign${money(tx.amount)}", color = color, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTransactionDialog(
    categories: List<com.finmate.app.data.model.Category>,
    onDismiss: () -> Unit,
    onSave: (TransactionType, Double, String, String) -> Unit
) {
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var expandedType by remember { mutableStateOf(false) }
    var expandedCat by remember { mutableStateOf(false) }
    val filtered = categories.filter { it.type == type }
    var categoryId by remember { mutableStateOf(filtered.firstOrNull()?.id.orEmpty()) }

    LaunchedEffect(type) {
        categoryId = categories.filter { it.type == type }.firstOrNull()?.id.orEmpty()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая операция") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ExposedDropdownMenuBox(expanded = expandedType, onExpandedChange = { expandedType = it }) {
                    OutlinedTextField(
                        value = if (type == TransactionType.EXPENSE) "Расход" else "Доход",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Тип") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedType) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expandedType, onDismissRequest = { expandedType = false }) {
                        DropdownMenuItem(text = { Text("Расход") }, onClick = {
                            type = TransactionType.EXPENSE
                            expandedType = false
                        })
                        DropdownMenuItem(text = { Text("Доход") }, onClick = {
                            type = TransactionType.INCOME
                            expandedType = false
                        })
                    }
                }
                ExposedDropdownMenuBox(expanded = expandedCat, onExpandedChange = { expandedCat = it }) {
                    val name = filtered.find { it.id == categoryId }?.name.orEmpty()
                    OutlinedTextField(
                        value = name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Категория") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedCat) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expandedCat, onDismissRequest = { expandedCat = false }) {
                        filtered.forEach { cat ->
                            DropdownMenuItem(text = { Text(cat.name) }, onClick = {
                                categoryId = cat.id
                                expandedCat = false
                            })
                        }
                    }
                }
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Сумма, ₽") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Комментарий") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val value = amount.toDoubleOrNull() ?: return@Button
                if (categoryId.isBlank()) return@Button
                onSave(type, value, categoryId, note)
            }) { Text("Сохранить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BudgetDialog(
    categories: List<com.finmate.app.data.model.Category>,
    onDismiss: () -> Unit,
    onSave: (String, Double) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var categoryId by remember { mutableStateOf(categories.firstOrNull()?.id.orEmpty()) }
    var limit by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Лимит категории") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = categories.find { it.id == categoryId }?.name.orEmpty(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Категория") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        categories.forEach { cat ->
                            DropdownMenuItem(text = { Text(cat.name) }, onClick = {
                                categoryId = cat.id
                                expanded = false
                            })
                        }
                    }
                }
                OutlinedTextField(
                    value = limit,
                    onValueChange = { limit = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Лимит в месяц, ₽") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val value = limit.toDoubleOrNull() ?: return@Button
                if (categoryId.isBlank()) return@Button
                onSave(categoryId, value)
            }) { Text("Сохранить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

private fun money(value: Double): String {
    val format = NumberFormat.getNumberInstance(Locale("ru", "RU"))
    return "${format.format(value.toLong())} ₽"
}
