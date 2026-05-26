package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stores")
data class StoreEntity(
    @PrimaryKey val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val queueTimeMinutes: Int,
    val seatingAvailable: Int,
    val totalCapacity: Int,
    val ambienceTags: String, // Comma separated, e.g. "Quiet, Jazz, Fireplace"
    val shelfLevelPercent: Int, // Pickup shelf status
    val isPremium: Boolean,
    val openingHours: String
)

@Entity(tableName = "menu_items")
data class MenuItemEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val basePrice: Double,
    val category: String, // "Seasonal", "Hot Drinks", "Cold Drinks", "Bakery"
    val isSeasonal: Boolean,
    val caffeineMg: Int,
    val imageType: String // "latte", "espresso", "cold_brew", "croissant", "flat_white", "matcha"
)

@Entity(tableName = "user_sessions")
data class UserSessionEntity(
    @PrimaryKey val id: Int = 1, // Single row constraint
    val username: String,
    val currentBalance: Double,
    val currentPoints: Int, // Loyalty beans/tokens
    val streakCount: Int,
    val longestStreakCount: Int,
    val lastVisitedDateString: String, // "YYYY-MM-DD"
    val membershipType: String, // "Regular", "Aurelia Gold"
    val membershipExpiresAt: Long, // Epoch timestamp or 0
    val totalCaffeineThisMonthMg: Int,
    val totalSpendingThisMonth: Double,
    val totalOrdersCount: Int,
    val usualDrinkId: String,
    val usualDrinkCustomization: String // JSON string represent size/milk/shots
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val storeId: String,
    val storeName: String,
    val itemName: String,
    val itemCustomization: String, // e.g. "Large · Oat Milk · Double Shot · Vanilla"
    val totalPrice: Double,
    val pointsEarned: Int,
    val status: String, // "PREPARING", "READY", "COLLECTED"
    val scheduledPickupTime: String, // "As soon as possible", "In 10 mins", "In 20 mins"
    val isUsual: Boolean = false
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val amount: Double, // positive for top-up, negative for purchase
    val type: String, // "TOP_UP", "PURCHASE", "GIFT_LOAD", "SUBSCRIPTION"
    val description: String
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val isUnlocked: Boolean,
    val iconType: String, // "streak", "caffeine", "first_order", "gold_status", "early_bird"
    val pointsBonus: Int
)
