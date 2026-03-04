package com.campus.expensetracker.data.dao

import androidx.room.*
import com.campus.expensetracker.data.entity.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: Long): Expense?

    @Query("SELECT * FROM expenses WHERE tripId = :tripId ORDER BY date DESC")
    fun getExpensesByTripId(tripId: Long): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE paidByFriendId = :friendId ORDER BY date DESC")
    fun getExpensesByPaidByFriendId(friendId: Long): Flow<List<Expense>>

    @Query("SELECT SUM(amount) FROM expenses WHERE date >= :startOfMonth AND date <= :endOfMonth")
    suspend fun getTotalMonthlyExpenses(startOfMonth: Long, endOfMonth: Long): Double?

    @Query("SELECT * FROM expenses WHERE date >= :startOfMonth AND date <= :endOfMonth ORDER BY date DESC LIMIT 10")
    fun getRecentExpenses(startOfMonth: Long, endOfMonth: Long): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: Long)

    @Query("SELECT SUM(amount) FROM expenses WHERE tripId = :tripId")
    suspend fun getTotalTripExpenses(tripId: Long): Double?
}
