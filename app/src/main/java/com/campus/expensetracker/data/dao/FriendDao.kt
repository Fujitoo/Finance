package com.campus.expensetracker.data.dao

import androidx.room.*
import com.campus.expensetracker.data.entity.Friend
import kotlinx.coroutines.flow.Flow

@Dao
interface FriendDao {

    @Query("SELECT * FROM friends WHERE tripId = :tripId ORDER BY name")
    fun getFriendsByTripId(tripId: Long): Flow<List<Friend>>

    @Query("SELECT * FROM friends WHERE id = :id")
    suspend fun getFriendById(id: Long): Friend?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriend(friend: Friend): Long

    @Update
    suspend fun updateFriend(friend: Friend)

    @Delete
    suspend fun deleteFriend(friend: Friend)

    @Query("DELETE FROM friends WHERE id = :id")
    suspend fun deleteFriendById(id: Long)

    @Query("DELETE FROM friends WHERE tripId = :tripId")
    suspend fun deleteFriendsByTripId(tripId: Long)
}
