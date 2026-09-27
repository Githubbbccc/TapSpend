package com.tapspend.app.domain

import com.tapspend.app.data.DefaultCategory
import java.util.regex.Pattern

data class ParsedSmartExpense(
    val amount: Double,
    val inferredCategory: DefaultCategory?,
    val title: String,
    val rawText: String
)

object SmartExpenseParser {
    // Regex matches leading or embedded amount: e.g. "250 biryani", "petrol 500", "80 chai", "1200 groceries"
    private val AMOUNT_PATTERN = Pattern.compile("(\\d+(\\.\\d{1,2})?)")

    fun parse(input: String): ParsedSmartExpense? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return null

        val matcher = AMOUNT_PATTERN.matcher(trimmed)
        if (!matcher.find()) return null

        val amountStr = matcher.group(1)
        val amount = amountStr?.toDoubleOrNull() ?: return null

        // Extract remaining text as description/category cue
        val remainder = trimmed.replaceFirst(amountStr, "").trim()
            .replace(Regex("^(rs\\.?|pkr)\\s*", RegexOption.IGNORE_CASE), "")
            .trim()

        val lowerRemainder = remainder.lowercase()

        // Match against category keywords
        var matchedCategory: DefaultCategory? = null
        for (cat in DefaultCategory.values()) {
            if (cat.keywords.any { lowerRemainder.contains(it) }) {
                matchedCategory = cat
                break
            }
        }

        // Title defaults to remainder or category label
        val title = if (remainder.isNotBlank()) {
            remainder.replaceFirstChar { it.uppercase() }
        } else {
            matchedCategory?.label ?: "Expense"
        }

        return ParsedSmartExpense(
            amount = amount,
            inferredCategory = matchedCategory,
            title = title,
            rawText = trimmed
        )
    }
}
