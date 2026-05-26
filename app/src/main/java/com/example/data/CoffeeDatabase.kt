package com.example.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        StoreEntity::class,
        MenuItemEntity::class,
        UserSessionEntity::class,
        OrderEntity::class,
        TransactionEntity::class,
        AchievementEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CoffeeDatabase : RoomDatabase() {
    abstract fun coffeeDao(): CoffeeDao
}
