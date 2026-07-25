package com.hfm.tv

import android.app.Application
import com.hfm.tv.data.AppDatabase

class HFMApplication : Application() {
    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AppDatabase.getInstance(this)
    }

    companion object {
        lateinit var instance: HFMApplication
            private set
    }
}