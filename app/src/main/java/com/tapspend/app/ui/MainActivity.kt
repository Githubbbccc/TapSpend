package com.tapspend.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.tapspend.app.TapSpendApplication
import com.tapspend.app.data.ExpenseEntity
import com.tapspend.app.domain.DateRanges
import com.tapspend.app.domain.SalaryBudgetMetrics
import com.tapspend.app.domain.SalaryCalculator
import com.tapspend.app.domain.formatPkr
import com.tapspend.app.notification.NotificationHelper
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as TapSpendApplication

        setContent {
            TapSpendTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DashboardScreen(app = app)
                }
            }
        }
    }
}

@Composable
private fun DashboardScreen(app: TapSpendApplication) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val expenseDao = remember { app.database.expenseDao() }

    // Live data
    val expensesFlow = remember { expenseDao.getAllExpenses() }
    val allExpenses by expensesFlow.collectAsState(initial = emptyList<ExpenseEntity>())

    val startOfToday = remember { DateRanges.startOfToday() }
    val startOfWeek = remember { DateRanges.startOfWeek() }
    val startOfMonth = remember { DateRanges.startOfMonth() }

    val todayFlow = remember(startOfToday) { expenseDao.getTodayTotal(startOfToday) }
    val weekFlow = remember(startOfWeek) { expenseDao.getWeekTotal(startOfWeek) }
    val monthFlow = remember(startOfMonth) { expenseDao.getMonthTotal(startOfMonth) }

    val todayTotal by todayFlow.collectAsState(initial = 0.0)
    val weekTotal by weekFlow.collectAsState(initial = 0.0)
    val monthTotal by monthFlow.collectAsState(initial = 0.0)

    val monthlySalary by app.preferences.monthlySalaryFlow.collectAsState(initial = 0.0)
    val notificationEnabled by app.preferences.notificationEnabledFlow.collectAsState(initial = true)

    // UI state
    var showFastEntry by remember { mutableStateOf(false) }
    var showIncomeDialog by remember { mutableStateOf(false) }
    var expensePendingDelete by remember { mutableStateOf<ExpenseEntity?>(null) }

    // Android 13+ requires a runtime permission before the ongoing notification can appear.
    var notificationsAllowed by remember {
        mutableStateOf(NotificationHelper.canPostNotifications(context))
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationsAllowed = granted
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Keep the always-available quick-add notification in sync with today's spending.
    LaunchedEffect(todayTotal, notificationEnabled, notificationsAllowed) {
        if (notificationEnabled) {
            NotificationHelper.showPersistentNotification(context, todayTotal)
        } else {
            NotificationHelper.cancelNotification(context)
        }
    }

    val metrics: SalaryBudgetMetrics? = if (monthlySalary > 0.0) {
        SalaryCalculator.calculate(
            salary = monthlySalary,
            monthSpent = monthTotal,
            todaySpent = todayTotal,
            weekSpent = weekTotal
        )
    } else {
        null
    }
    val totalSpent = allExpenses.sumOf { it.amount }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBarHeader(
                notificationEnabled = notificationEnabled,
                onToggleNotification = { enabled ->
                    scope.launch { app.preferences.setNotificationEnabled(enabled) }
                }
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                IncomeCard(
                    metrics = metrics,
                    onEditClick = { showIncomeDialog = true }
                )

                Spacer(modifier = Modifier.height(12.dp))

                MetricsGrid(
                    metrics = metrics,
                    todayTotal = todayTotal,
                    weekTotal = weekTotal,
                    monthTotal = monthTotal,
                    totalSpent = totalSpent
                )

                Spacer(modifier = Modifier.height(12.dp))

                SpendingBreakdownCard(
                    expenses = allExpenses,
                    startOfMonth = startOfMonth,
                    monthTotal = monthTotal
                )

                Spacer(modifier = Modifier.height(12.dp))

                RecentExpensesCard(
                    expenses = allExpenses,
                    onDeleteClick = { expensePendingDelete = it }
                )

                Spacer(modifier = Modifier.height(96.dp))
            }
        }

        FloatingActionButton(
            onClick = { showFastEntry = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add expense")
        }

        if (showFastEntry) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable { showFastEntry = false },
                contentAlignment = Alignment.BottomCenter
            ) {
                // Swallow taps on the sheet itself so it is not dismissed by accident.
                Box(modifier = Modifier.pointerInput(Unit) { detectTapGestures { } }) {
                    FastExpenseEntrySheet(
                        onDismiss = { showFastEntry = false },
                        onSaveExpense = { amount, category, note ->
                            scope.launch {
                                expenseDao.insertExpense(
                                    ExpenseEntity(
                                        amount = amount,
                                        category = category.label,
                                        categoryIcon = category.icon,
                                        title = category.label,
                                        note = note
                                    )
                                )
                            }
                        }
                    )
                }
            }
        }
    }

    expensePendingDelete?.let { expense ->
        AlertDialog(
            onDismissRequest = { expensePendingDelete = null },
            title = { Text("Delete this expense?") },
            text = {
                Text("${expense.categoryIcon} ${expense.title} · ${formatPkr(expense.amount)}")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        expensePendingDelete = null
                        scope.launch { expenseDao.deleteExpense(expense) }
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { expensePendingDelete = null }) { Text("Cancel") }
            }
        )
    }

    if (showIncomeDialog) {
        IncomeDialog(
            monthlySalary = monthlySalary,
            onDismiss = { showIncomeDialog = false },
            onSave = { salary ->
                scope.launch { app.preferences.updateSalary(salary) }
                showIncomeDialog = false
            }
        )
    }
}

@Composable
private fun IncomeDialog(
    monthlySalary: Double,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var salaryText by remember {
        mutableStateOf(if (monthlySalary > 0.0) monthlySalary.toLong().toString() else "")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Optional cash flow") },
        text = {
            Column {
                Text(
                    text = "TapSpend works 100% without income tracking. Add a monthly income only " +
                        "if you want remaining-money and safe-daily-budget figures.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = salaryText,
                    onValueChange = { input ->
                        salaryText = input.filter { it.isDigit() || it == '.' }
                    },
                    label = { Text("Monthly income (PKR)") },
                    placeholder = { Text("e.g. 60000") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Leave empty (or 0) to keep pure expense tracking.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave((salaryText.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)) }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun TopAppBarHeader(
    notificationEnabled: Boolean,
    onToggleNotification: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "TapSpend",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Track it. Tap it. Done.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { onToggleNotification(!notificationEnabled) }) {
                Icon(
                    imageVector = if (notificationEnabled) {
                        Icons.Default.Notifications
                    } else {
                        Icons.Default.NotificationsOff
                    },
                    contentDescription = if (notificationEnabled) {
                        "Turn off quick-add notification"
                    } else {
                        "Turn on quick-add notification"
                    },
                    tint = if (notificationEnabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
private fun IncomeCard(
    metrics: SalaryBudgetMetrics?,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (metrics != null) "💰 Monthly cash flow" else "⚡ Universal expense tracker",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (metrics != null) "Edit ✎" else "Add income +",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onEditClick() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (metrics == null) {
                Text(
                    text = "No income required. Students, freelancers, business owners and families " +
                        "can track daily spending freely.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatBox(
                        label = "Income",
                        value = formatPkr(metrics.salary),
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        label = "Spent this month",
                        value = formatPkr(metrics.totalMonthSpent),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                StatBox(
                    label = "Remaining money",
                    value = formatPkr(metrics.remainingSalary),
                    highlight = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                val spentFraction = if (metrics.salary > 0.0) {
                    (metrics.totalMonthSpent / metrics.salary).toFloat().coerceIn(0f, 1f)
                } else {
                    0f
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    if (spentFraction > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(spentFraction.coerceAtLeast(0.03f))
                                .height(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (spentFraction > 0.85f) {
                                        Color(0xFFEF4444)
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    }
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Daily average ${formatPkr(metrics.dailyAverage)} · " +
                        "Safe daily budget ${formatPkr(metrics.safeDailyBudget)} · " +
                        "${metrics.daysRemainingInMonth} day(s) left",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = if (highlight) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

@Composable
private fun MetricsGrid(
    metrics: SalaryBudgetMetrics?,
    todayTotal: Double,
    weekTotal: Double,
    monthTotal: Double,
    totalSpent: Double
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MetricCard(
            title = if (metrics != null) "Today" else "Today's Spending",
            value = formatPkr(todayTotal),
            modifier = Modifier.weight(1f)
        )
        MetricCard(
            title = "This Week",
            value = formatPkr(weekTotal),
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MetricCard(
            title = "This Month",
            value = formatPkr(monthTotal),
            modifier = Modifier.weight(1f)
        )
        MetricCard(
            title = if (metrics != null) "Remaining" else "Total Spending",
            value = formatPkr(metrics?.remainingSalary ?: totalSpent),
            highlight = true,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = if (highlight) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SpendingBreakdownCard(
    expenses: List<ExpenseEntity>,
    startOfMonth: Long,
    monthTotal: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Spending Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "This month · ${formatPkr(monthTotal)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            val monthExpenses = expenses.filter { it.timestamp >= startOfMonth }

            if (monthExpenses.isEmpty()) {
                Text(
                    text = "No spending recorded this month yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val grouped = monthExpenses
                    .groupBy { it.category to it.categoryIcon }
                    .mapValues { entry -> entry.value.sumOf { it.amount } }
                    .entries
                    .sortedByDescending { it.value }

                grouped.forEach { entry ->
                    val (category, icon) = entry.key
                    BreakdownRow(
                        icon = icon,
                        label = category,
                        amount = entry.value,
                        fraction = if (monthTotal > 0.0) {
                            (entry.value / monthTotal).toFloat().coerceIn(0f, 1f)
                        } else {
                            0f
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun BreakdownRow(
    icon: String,
    label: String,
    amount: Double,
    fraction: Float
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon.ifBlank { "🏷️" }, fontSize = 15.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
            Text(
                text = formatPkr(amount),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (fraction > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction.coerceAtLeast(0.03f))
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@Composable
private fun RecentExpensesCard(
    expenses: List<ExpenseEntity>,
    onDeleteClick: (ExpenseEntity) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Recent Expenses",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (expenses.isEmpty()) {
                Text(
                    text = "No expenses logged yet. Tap the + button to record your first one.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                expenses.take(RECENT_EXPENSE_LIMIT).forEach { expense ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = expense.categoryIcon.ifBlank { "🏷️" }, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = expense.title.ifBlank { expense.category },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Text(
                                text = DateRanges.formatTime(expense.timestamp) +
                                    if (expense.note.isBlank()) "" else " · ${expense.note}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                        Text(
                            text = formatPkr(expense.amount),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(onClick = { onDeleteClick(expense) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete expense",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private const val RECENT_EXPENSE_LIMIT = 6
