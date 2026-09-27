package com.tapspend.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tapspend.app.data.DefaultCategory

sealed class FastEntryStep {
    object Amount : FastEntryStep()
    object Category : FastEntryStep()
    data class Confirmed(val amount: Double, val category: DefaultCategory) : FastEntryStep()
}

@Composable
fun FastExpenseEntrySheet(
    onDismiss: () -> Unit,
    onSaveExpense: (amount: Double, category: DefaultCategory, note: String) -> Unit
) {
    var step by remember { mutableStateOf<FastEntryStep>(FastEntryStep.Amount) }
    var amountString by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<DefaultCategory?>(null) }
    var noteText by remember { mutableStateOf("") }
    var isAddingNote by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Drag handle
            Box(
                modifier = Modifier
                    .width(44.dp)
                    .height(5.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (val currentStep = step) {
                is FastEntryStep.Amount -> {
                    // STEP 1: AMOUNT
                    Text(
                        text = "💵 How much did you spend?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = if (amountString.isEmpty()) "Rs 0" else "Rs $amountString",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Large Numeric Keypad for fast one-handed entry
                    KeypadGrid(
                        onDigitClick = { digit ->
                            if (amountString.length < 9) {
                                if (digit == "." && amountString.contains(".")) return@KeypadGrid
                                amountString += digit
                            }
                        },
                        onBackspaceClick = {
                            if (amountString.isNotEmpty()) {
                                amountString = amountString.dropLast(1)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            val amt = amountString.toDoubleOrNull()
                            if (amt != null && amt > 0) {
                                step = FastEntryStep.Category
                            }
                        },
                        enabled = (amountString.toDoubleOrNull() ?: 0.0) > 0,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Continue →", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }

                is FastEntryStep.Category -> {
                    // STEP 2: CATEGORY SELECTION
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { step = FastEntryStep.Amount }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                        Text(
                            text = "🏷️ What was it for?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "Rs $amountString",
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.heightIn(max = 380.dp)
                    ) {
                        items(DefaultCategory.values().toList()) { category ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(84.dp)
                                    .clickable {
                                        selectedCategory = category
                                        val amt = amountString.toDoubleOrNull() ?: 0.0
                                        // Auto-transition to Step 3
                                        step = FastEntryStep.Confirmed(amt, category)
                                    },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(category.icon, fontSize = 28.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        category.label,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                is FastEntryStep.Confirmed -> {
                    // STEP 3: OPTIONAL NOTE & IMMEDIATE SAVE
                    Text(
                        text = "Expense added ✓",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF059669)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Rs ${amountString} · ${currentStep.category.icon} ${currentStep.category.label}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isAddingNote) {
                        OutlinedTextField(
                            value = noteText,
                            onValueChange = { noteText = it },
                            label = { Text("Note (few words)") },
                            placeholder = { Text("e.g. Karak chai, fuel station") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (!isAddingNote) {
                            OutlinedButton(
                                onClick = { isAddingNote = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Add Note")
                            }
                        }

                        Button(
                            onClick = {
                                onSaveExpense(currentStep.amount, currentStep.category, noteText.trim())
                                onDismiss()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF059669)
                            )
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Done", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KeypadGrid(
    onDigitClick: (String) -> Unit,
    onBackspaceClick: () -> Unit
) {
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(".", "0", "⌫")
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (row in keys) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (key in row) {
                    FilledTonalButton(
                        onClick = {
                            if (key == "⌫") onBackspaceClick() else onDigitClick(key)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = key,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
