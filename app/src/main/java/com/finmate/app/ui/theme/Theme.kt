package com.finmate.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Primary = Color(0xFF006A60)
private val OnPrimary = Color(0xFFFFFFFF)
private val PrimaryContainer = Color(0xFF9FF2E5)
private val Surface = Color(0xFFF4FBF8)
private val Income = Color(0xFF146C2E)
private val Expense = Color(0xFFBA1A1A)

private val LightColors = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    surface = Surface,
    background = Surface,
    error = Expense
)

val IncomeColor = Income
val ExpenseColor = Expense

@Composable
fun FinMateTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}
