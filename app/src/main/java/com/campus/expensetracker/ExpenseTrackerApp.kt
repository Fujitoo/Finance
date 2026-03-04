package com.campus.expensetracker

import android.app.Application

class ExpenseTrackerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: ExpenseTrackerApp
            private set
    }
}
