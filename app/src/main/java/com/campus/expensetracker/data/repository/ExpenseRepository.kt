package com.campus.expensetracker.data.repository

import com.campus.expensetracker.data.dao.*
import com.campus.expensetracker.data.entity.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExpenseRepository(
    private val categoryDao: CategoryDao,
    private val tripDao: TripDao,
    private val friendDao: FriendDao,
    private val expenseDao: ExpenseDao,
    private val splitDao: SplitDao
) {

    // Category operations
    fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories()

    suspend fun getCategoryById(id: Long): Category? = withContext(Dispatchers.IO) {
        categoryDao.getCategoryById(id)
    }

    suspend fun insertCategory(category: Category): Long = withContext(Dispatchers.IO) {
        categoryDao.insertCategory(category)
    }

    suspend fun updateCategory(category: Category) = withContext(Dispatchers.IO) {
        categoryDao.updateCategory(category)
    }

    suspend fun deleteCategory(category: Category) = withContext(Dispatchers.IO) {
        categoryDao.deleteCategory(category)
    }

    // Trip operations
    fun getAllTrips(): Flow<List<Trip>> = tripDao.getAllTrips()

    suspend fun getTripById(id: Long): Trip? = withContext(Dispatchers.IO) {
        tripDao.getTripById(id)
    }

    suspend fun insertTrip(trip: Trip): Long = withContext(Dispatchers.IO) {
        tripDao.insertTrip(trip)
    }

    suspend fun updateTrip(trip: Trip) = withContext(Dispatchers.IO) {
        tripDao.updateTrip(trip)
    }

    suspend fun deleteTrip(trip: Trip) = withContext(Dispatchers.IO) {
        tripDao.deleteTrip(trip)
    }

    // Friend operations
    fun getFriendsByTripId(tripId: Long): Flow<List<Friend>> = friendDao.getFriendsByTripId(tripId)

    suspend fun getFriendById(id: Long): Friend? = withContext(Dispatchers.IO) {
        friendDao.getFriendById(id)
    }

    suspend fun insertFriend(friend: Friend): Long = withContext(Dispatchers.IO) {
        friendDao.insertFriend(friend)
    }

    suspend fun updateFriend(friend: Friend) = withContext(Dispatchers.IO) {
        friendDao.updateFriend(friend)
    }

    suspend fun deleteFriend(friend: Friend) = withContext(Dispatchers.IO) {
        friendDao.deleteFriend(friend)
    }

    // Expense operations
    fun getAllExpenses(): Flow<List<Expense>> = expenseDao.getAllExpenses()

    suspend fun getExpenseById(id: Long): Expense? = withContext(Dispatchers.IO) {
        expenseDao.getExpenseById(id)
    }

    fun getExpensesByTripId(tripId: Long): Flow<List<Expense>> = expenseDao.getExpensesByTripId(tripId)

    fun getExpensesByPaidByFriendId(friendId: Long): Flow<List<Expense>> = expenseDao.getExpensesByPaidByFriendId(friendId)

    suspend fun getTotalMonthlyExpenses(startOfMonth: Long, endOfMonth: Long): Double = withContext(Dispatchers.IO) {
        expenseDao.getTotalMonthlyExpenses(startOfMonth, endOfMonth) ?: 0.0
    }

    fun getRecentExpenses(startOfMonth: Long, endOfMonth: Long): Flow<List<Expense>> = expenseDao.getRecentExpenses(startOfMonth, endOfMonth)

    suspend fun insertExpense(expense: Expense): Long = withContext(Dispatchers.IO) {
        expenseDao.insertExpense(expense)
    }

    suspend fun updateExpense(expense: Expense) = withContext(Dispatchers.IO) {
        expenseDao.updateExpense(expense)
    }

    suspend fun deleteExpense(expense: Expense) = withContext(Dispatchers.IO) {
        expenseDao.deleteExpense(expense)
    }

    suspend fun getTotalTripExpenses(tripId: Long): Double = withContext(Dispatchers.IO) {
        expenseDao.getTotalTripExpenses(tripId) ?: 0.0
    }

    // Split operations
    fun getSplitsByExpenseId(expenseId: Long): Flow<List<Split>> = splitDao.getSplitsByExpenseId(expenseId)

    fun getSplitsByFriendId(friendId: Long): Flow<List<Split>> = splitDao.getSplitsByFriendId(friendId)

    suspend fun insertSplit(split: Split): Long = withContext(Dispatchers.IO) {
        splitDao.insertSplit(split)
    }

    suspend fun insertSplits(splits: List<Split>) = withContext(Dispatchers.IO) {
        splitDao.insertSplits(splits)
    }

    suspend fun updateSplit(split: Split) = withContext(Dispatchers.IO) {
        splitDao.updateSplit(split)
    }

    suspend fun deleteSplit(split: Split) = withContext(Dispatchers.IO) {
        splitDao.deleteSplit(split)
    }

    suspend fun deleteSplitsByExpenseId(expenseId: Long) = withContext(Dispatchers.IO) {
        splitDao.deleteSplitsByExpenseId(expenseId)
    }

    suspend fun getTotalOwedByFriend(friendId: Long): Double = withContext(Dispatchers.IO) {
        splitDao.getTotalOwedByFriend(friendId) ?: 0.0
    }

    // Complex operations for trip balance calculation
    suspend fun calculateTripBalances(tripId: Long): Map<Long, Double> = withContext(Dispatchers.IO) {
        val friends = friendDao.getFriendsByTripId(tripId)
        val expenses = expenseDao.getExpensesByTripId(tripId)
        
        val balances = mutableMapOf<Long, Double>()
        
        // Initialize balances to 0
        friends.forEach { friend ->
            balances[friend.id] = 0.0
        }
        
        // Calculate what each friend paid
        expenses.forEach { expense ->
            expense.paidByFriendId?.let { paidBy ->
                balances[paidBy] = (balances[paidBy] ?: 0.0) + expense.amount
            }
        }
        
        // Calculate total and equal share
        val total = expenses.sumOf { it.amount }
        val equalShare = if (friends.isNotEmpty()) total / friends.size else 0.0
        
        // Subtract equal share from each friend's balance
        friends.forEach { friend ->
            balances[friend.id] = (balances[friend.id] ?: 0.0) - equalShare
        }
        
        balances
    }
}
