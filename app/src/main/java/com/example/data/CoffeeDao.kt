package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CoffeeDao {

    // ========== Stores ==========
    @Query("SELECT * FROM stores")
    fun getAllStores(): Flow<List<StoreEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStores(stores: List<StoreEntity>)

    @Query("SELECT * FROM stores WHERE id = :id LIMIT 1")
    suspend fun getStoreById(id: String): StoreEntity?

    @Query("UPDATE stores SET queueTimeMinutes = :newQueue, shelfLevelPercent = :newShelf, seatingAvailable = :newSeats WHERE id = :id")
    suspend fun updateStoreStatus(id: String, newQueue: Int, newShelf: Int, newSeats: Int)


    // ========== Menu Items ==========
    @Query("SELECT * FROM menu_items")
    fun getAllMenuItems(): Flow<List<MenuItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItems(items: List<MenuItemEntity>)

    @Query("SELECT * FROM menu_items WHERE id = :id LIMIT 1")
    suspend fun getMenuItemById(id: String): MenuItemEntity?


    // ========== User Session ==========
    @Query("SELECT * FROM user_sessions WHERE id = 1 LIMIT 1")
    fun getUserSessionFlow(): Flow<UserSessionEntity?>

    @Query("SELECT * FROM user_sessions WHERE id = 1 LIMIT 1")
    suspend fun getUserSession(): UserSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserSession(session: UserSessionEntity)

    @Query("UPDATE user_sessions SET currentBalance = :newBalance WHERE id = 1")
    suspend fun updateUserBalance(newBalance: Double)

    @Query("UPDATE user_sessions SET currentPoints = :newPoints WHERE id = 1")
    suspend fun updateUserPoints(newPoints: Int)


    // ========== Orders ==========
    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE status != 'COLLECTED' ORDER BY timestamp DESC")
    fun getActiveOrders(): Flow<List<OrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Query("UPDATE orders SET status = :newStatus WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Long, newStatus: String)

    @Query("DELETE FROM orders")
    suspend fun clearAllOrders()


    // ========== Transactions ==========
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long


    // ========== Achievements ==========
    @Query("SELECT * FROM achievements")
    fun getAllAchievements(): Flow<List<AchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<AchievementEntity>)

    @Query("UPDATE achievements SET isUnlocked = 1 WHERE id = :achievementId")
    suspend fun unlockAchievement(achievementId: String)
}
