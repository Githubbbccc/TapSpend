package com.tapspend.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE timestamp >= :startOfDay ORDER BY timestamp DESC")
    fun getTodayExpenses(startOfDay: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE timestamp >= :startOfWeek ORDER BY timestamp DESC")
    fun getWeekExpenses(startOfWeek: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE timestamp >= :startOfMonth ORDER BY timestamp DESC")
    fun getMonthExpenses(startOfMonth: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE timestamp >= :startOfDay")
    fun getTodayTotal(startOfDay: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE timestamp >= :startOfWeek")
    fun getWeekTotal(startOfWeek: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE timestamp >= :startOfMonth")
    fun getMonthTotal(startOfMonth: Long): Flow<Double>
}
