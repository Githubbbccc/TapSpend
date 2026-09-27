package com.tapspend.app.domain

import java.util.Calendar

data class SalaryBudgetMetrics(
    val salary: Double,
    val totalMonthSpent: Double,
    val remainingSalary: Double,
    val todaySpent: Double,
    val weekSpent: Double,
    val dailyAverage: Double,
    val safeDailyBudget: Double,
    val daysRemainingInMonth: Int,
    val daysPassedInMonth: Int,
    val totalDaysInMonth: Int
)

object SalaryCalculator {

    fun calculate(
        salary: Double,
        monthSpent: Double,
        todaySpent: Double,
        weekSpent: Double
    ): SalaryBudgetMetrics {
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val totalDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val remainingDays = (totalDays - currentDay + 1).coerceAtLeast(1)

        val remainingMoney = (salary - monthSpent).coerceAtLeast(0.0)

        // Daily average spent so far
        val dailyAverage = if (currentDay > 0) monthSpent / currentDay else 0.0

        // Safe daily budget for remaining days
        val safeDaily = remainingMoney / remainingDays

        return SalaryBudgetMetrics(
            salary = salary,
            totalMonthSpent = monthSpent,
            remainingSalary = remainingMoney,
            todaySpent = todaySpent,
            weekSpent = weekSpent,
            dailyAverage = dailyAverage,
            safeDailyBudget = safeDaily,
            daysRemainingInMonth = remainingDays,
            daysPassedInMonth = currentDay,
            totalDaysInMonth = totalDays
        )
    }
}
