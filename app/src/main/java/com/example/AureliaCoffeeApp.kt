package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.CoffeeDatabase
import com.example.data.CoffeeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AureliaCoffeeApp : Application() {

    val database by lazy {
        Room.databaseBuilder(
            this,
            CoffeeDatabase::class.java,
            "aurelia_coffee.db"
        )
        .fallbackToDestructiveMigration() // Prevent crashes if models change during development
        .build()
    }

    val repository by lazy {
        CoffeeRepository(database.coffeeDao())
    }

    override fun onCreate() {
        super.onCreate()
        // Pre-populate data asynchronously on startup
        CoroutineScope(Dispatchers.IO).launch {
            repository.prepareInitialDataIfNeeded()
        }
    }
}
