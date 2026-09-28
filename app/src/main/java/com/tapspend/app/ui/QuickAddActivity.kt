package com.tapspend.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.lifecycleScope
import com.tapspend.app.TapSpendApplication
import com.tapspend.app.data.DefaultCategory
import com.tapspend.app.data.ExpenseEntity
import kotlinx.coroutines.launch

/**
 * Instant floating expense entry, opened straight from the notification's
 * "+ Add Expense" action without leaving whatever the user is currently doing.
 */
class QuickAddActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as TapSpendApplication

        setContent {
            TapSpendTheme {
                QuickAddOverlay(
                    onDismiss = { finish() },
                    onSaveExpense = { amount, category, note ->
                        lifecycleScope.launch {
                            app.database.expenseDao().insertExpense(
                                ExpenseEntity(
                                    amount = amount,
                                    category = category.label,
                                    categoryIcon = category.icon,
                                    title = category.label,
                                    note = note
                                )
                            )
                            finish()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun QuickAddOverlay(
    onDismiss: () -> Unit,
    onSaveExpense: (amount: Double, category: DefaultCategory, note: String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        // Swallow taps on the sheet itself so it is only dismissed from the outside.
        Box(modifier = Modifier.pointerInput(Unit) { detectTapGestures { } }) {
            FastExpenseEntrySheet(
                onDismiss = onDismiss,
                onSaveExpense = onSaveExpense
            )
        }
    }
}
