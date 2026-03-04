package com.campus.expensetracker.data.dao

import androidx.room.*
import com.campus.expensetracker.data.entity.Split
import kotlinx.coroutines.flow.Flow

@Dao
interface SplitDao {

    @Query("SELECT * FROM splits WHERE expenseId = :expenseId")
    fun getSplitsByExpenseId(expenseId: Long): Flow<List<Split>>

    @Query("SELECT * FROM splits WHERE friendId = :friendId")
    fun getSplitsByFriendId(friendId: Long): Flow<List<Split>>

    @Query("SELECT * FROM splits WHERE id = :id")
    suspend fun getSplitById(id: Long): Split?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSplit(split: Split): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSplits(splits: List<Split>)

    @Update
    suspend fun updateSplit(split: Split)

    @Delete
    suspend fun deleteSplit(split: Split)

    @Query("DELETE FROM splits WHERE expenseId = :expenseId")
    suspend fun deleteSplitsByExpenseId(expenseId: Long)

    @Query("DELETE FROM splits WHERE friendId = :friendId")
    suspend fun deleteSplitsByFriendId(friendId: Long)

    @Query("SELECT SUM(amount) FROM splits WHERE friendId = :friendId")
    suspend fun getTotalOwedByFriend(friendId: Long): Double?
}
