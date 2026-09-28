package com.tapspend.app.domain

/** Formats an amount as Pakistani Rupees, e.g. 1900.0 -> "Rs 1,900", 250.5 -> "Rs 250.50". */
fun formatPkr(value: Double): String {
    val safeValue = if (value.isFinite()) value else 0.0
    return if (safeValue % 1.0 == 0.0) {
        "Rs %,d".format(safeValue.toLong())
    } else {
        "Rs %,.2f".format(safeValue)
    }
}
