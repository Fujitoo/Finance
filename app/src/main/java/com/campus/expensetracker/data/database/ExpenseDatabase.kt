package com.campus.expensetracker.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.campus.expensetracker.data.dao.*
import com.campus.expensetracker.data.entity.*

@Database(
    entities = [
        Category::class,
        Trip::class,
        Friend::class,
        Expense::class,
        Split::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ExpenseDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun tripDao(): TripDao
    abstract fun friendDao(): FriendDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun splitDao(): SplitDao

    companion object {
        @Volatile
        private var INSTANCE: ExpenseDatabase? = null

        fun getDatabase(context: Context): ExpenseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ExpenseDatabase::class.java,
                    "expense_tracker_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
