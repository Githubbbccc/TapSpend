package com.tapspend.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.tapspend.app.TapSpendApplication
import com.tapspend.app.data.DefaultCategory
import com.tapspend.app.data.ExpenseEntity
import com.tapspend.app.notification.NotificationHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as TapSpendApplication
        val expenseDao = app.database.expenseDao()

        setContent {
            val isDark = isSystemInDarkTheme()
            var showFastEntry by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()

            // Triggered if launched from Notification '+ Add Expense' action
            LaunchedEffect(intent) {
                if (intent?.action == "com.tapspend.app.ACTION_QUICK_ADD") {
                    showFastEntry = true
                }
            }

            MaterialTheme(
                colorScheme = if (isDark) darkColorScheme(
                    primary = Color(0xFF00E676),
                    background = Color(0xFF080D1A),
                    surface = Color(0xFF0F172A)
                ) else lightColorScheme(
                    primary = Color(0xFF059669),
                    background = Color(0xFFF8FAFC),
                    surface = Color(0xFFFFFFFF)
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Main Dashboard / Expenses flow
                        DashboardScreen(
                            onAddExpenseClick = { showFastEntry = true }
                        )

                        // 3-Step Fast Entry Bottom Sheet
                        if (showFastEntry) {
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
                                        // Update notification with today's spending
                                        val startOfDay = System.currentTimeMillis() - (System.currentTimeMillis() % 86400000)
                                        val todayTotal = expenseDao.getTodayTotal(startOfDay).first()
                                        NotificationHelper.showPersistentNotification(this@MainActivity, todayTotal)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(onAddExpenseClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Top App Bar
        TopAppBarHeader()
        // Universal Metrics, Breakdown, and Floating Action
        Box(modifier = Modifier.weight(1f)) {
            // Dashboard Content
        }
    }
}

@Composable
fun TopAppBarHeader() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "TapSpend",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
