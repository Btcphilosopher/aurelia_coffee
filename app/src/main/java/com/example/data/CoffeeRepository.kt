package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CoffeeRepository(private val coffeeDao: CoffeeDao) {

    val allStores: Flow<List<StoreEntity>> = coffeeDao.getAllStores()
    val allMenuItems: Flow<List<MenuItemEntity>> = coffeeDao.getAllMenuItems()
    val userSession: Flow<UserSessionEntity?> = coffeeDao.getUserSessionFlow()
    val allOrders: Flow<List<OrderEntity>> = coffeeDao.getAllOrders()
    val activeOrders: Flow<List<OrderEntity>> = coffeeDao.getActiveOrders()
    val allTransactions: Flow<List<TransactionEntity>> = coffeeDao.getAllTransactions()
    val allAchievements: Flow<List<AchievementEntity>> = coffeeDao.getAllAchievements()

    suspend fun prepareInitialDataIfNeeded() {
        val currentStores = allStores.first()
        if (currentStores.isEmpty()) {
            // Seed stores
            val initialStores = listOf(
                StoreEntity(
                    id = "st_strand",
                    name = "Aurelia Strand",
                    address = "32 Strand, WC2N London",
                    latitude = 51.5080,
                    longitude = -0.1246,
                    queueTimeMinutes = 3,
                    seatingAvailable = 14,
                    totalCapacity = 20,
                    ambienceTags = "Cozy, Work-Friendly, Fireplace",
                    shelfLevelPercent = 85,
                    isPremium = true,
                    openingHours = "7:00 AM - 9:00 PM"
                ),
                StoreEntity(
                    id = "st_broadgate",
                    name = "Aurelia Broadgate",
                    address = "15 Primrose St, EC2A London",
                    latitude = 51.5205,
                    longitude = -0.0784,
                    queueTimeMinutes = 9,
                    seatingAvailable = 2,
                    totalCapacity = 30,
                    ambienceTags = "Vibrant, Fast-Paced, Modern",
                    shelfLevelPercent = 40,
                    isPremium = true,
                    openingHours = "6:30 AM - 7:00 PM"
                ),
                StoreEntity(
                    id = "st_soho",
                    name = "Aurelia Soho",
                    address = "12 Wardour St, W1D London",
                    latitude = 51.5113,
                    longitude = -0.1319,
                    queueTimeMinutes = 5,
                    seatingAvailable = 8,
                    totalCapacity = 12,
                    ambienceTags = "Acoustic, Warm, Aesthetic",
                    shelfLevelPercent = 90,
                    isPremium = false,
                    openingHours = "8:00 AM - 10:00 PM"
                ),
                StoreEntity(
                    id = "st_chelsea",
                    name = "Aurelia Chelsea",
                    address = "42 King's Rd, SW3 London",
                    latitude = 51.4902,
                    longitude = -0.1610,
                    queueTimeMinutes = 1,
                    seatingAvailable = 15,
                    totalCapacity = 15,
                    ambienceTags = "Spacious Terrace, Bright, Elegant",
                    shelfLevelPercent = 95,
                    isPremium = true,
                    openingHours = "7:30 AM - 8:30 PM"
                )
            )
            coffeeDao.insertStores(initialStores)
        }

        val currentItems = allMenuItems.first()
        if (currentItems.isEmpty()) {
            // Seed menu items
            val initialItems = listOf(
                MenuItemEntity(
                    id = "msg_rose_honey",
                    name = "Rose Honey Macchiato",
                    description = "A silky micro-foam macchiato infused with organic Provence rose bud honey and single-origin Ethiopian espresso.",
                    basePrice = 5.45,
                    category = "Seasonal",
                    isSeasonal = true,
                    caffeineMg = 120,
                    imageType = "latte"
                ),
                MenuItemEntity(
                    id = "msg_pistachio_matcha",
                    name = "Pistachio Creme Iced Matcha",
                    description = "Vibrant ceremonial Uji matcha whipped with silky cold pistachio cream and cold-pressed oat milk.",
                    basePrice = 5.95,
                    category = "Seasonal",
                    isSeasonal = true,
                    caffeineMg = 80,
                    imageType = "matcha"
                ),
                MenuItemEntity(
                    id = "sig_flat_white",
                    name = "Signature Flat White",
                    description = "A robust ristretto double shot blanketed with micro-textured velvety milk.",
                    basePrice = 4.15,
                    category = "Hot Drinks",
                    isSeasonal = false,
                    caffeineMg = 140,
                    imageType = "flat_white"
                ),
                MenuItemEntity(
                    id = "sig_espresso",
                    name = "Double Shot Espresso",
                    description = "Our bold hand-pulled espresso with notes of caramelized dark chocolate and orange zest.",
                    basePrice = 3.20,
                    category = "Hot Drinks",
                    isSeasonal = false,
                    caffeineMg = 150,
                    imageType = "espresso"
                ),
                MenuItemEntity(
                    id = "sig_cold_brew",
                    name = "Cold Brew Nitro",
                    description = "Single-origin Peruvian cold brew charged with nitrogen for a creamy Guinness-like head.",
                    basePrice = 4.50,
                    category = "Cold Drinks",
                    isSeasonal = false,
                    caffeineMg = 200,
                    imageType = "cold_brew"
                ),
                MenuItemEntity(
                    id = "sig_caramel_cortado",
                    name = "Golden Caramel Cortado",
                    description = "Balanced equal parts espresso and steamed milk drizzled with house caramelized syrup.",
                    basePrice = 3.85,
                    category = "Hot Drinks",
                    isSeasonal = false,
                    caffeineMg = 120,
                    imageType = "espresso"
                ),
                MenuItemEntity(
                    id = "bak_croissant",
                    name = "Artisanal Butter Croissant",
                    description = "A 24-layer folded premium Normandy butter croissant, baked fresh hourly to a crispy gold finish.",
                    basePrice = 3.50,
                    category = "Bakery",
                    isSeasonal = false,
                    caffeineMg = 0,
                    imageType = "croissant"
                ),
                MenuItemEntity(
                    id = "bak_cookie",
                    name = "Dark Velvet Sea-Salt Cookie",
                    description = "Chunky dark chocolate cookie finished with generous flakes of Maldon sea salt.",
                    basePrice = 3.25,
                    category = "Bakery",
                    isSeasonal = false,
                    caffeineMg = 10,
                    imageType = "croissant"
                )
            )
            coffeeDao.insertMenuItems(initialItems)
        }

        val session = coffeeDao.getUserSession()
        if (session == null) {
            val dateFormater = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateStr = dateFormater.format(Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000L)) // Seed as yesterday so checking today will tick streak

            val initialSession = UserSessionEntity(
                id = 1,
                username = "Tom",
                currentBalance = 45.00,
                currentPoints = 12,
                streakCount = 4,
                longestStreakCount = 12,
                lastVisitedDateString = dateStr, // "yesterday"
                membershipType = "Regular",
                membershipExpiresAt = 0L,
                totalCaffeineThisMonthMg = 1420,
                totalSpendingThisMonth = 124.50,
                totalOrdersCount = 22,
                usualDrinkId = "sig_flat_white",
                usualDrinkCustomization = "Regular · Whole Milk · Double Shot · Caramel"
            )
            coffeeDao.insertUserSession(initialSession)
        }

        val achievements = allAchievements.first()
        if (achievements.isEmpty()) {
            val initialAchievements = listOf(
                AchievementEntity("early_morning_ritual", "Sunrise Connoisseur", "Place an order before 8:00 AM.", true, "early_bird", 5),
                AchievementEntity("streak_5_days", "The Habitual Daily", "Reach a consecutive 5-day coffee streak.", false, "streak", 10),
                AchievementEntity("gold_rank", "Golden Aura", "Join our elite Aurelia Gold Membership circle.", false, "gold_status", 15),
                AchievementEntity("caffeine_king", "Overcharged", "Log more than 400mg of premium caffeine in one day.", false, "caffeine", 8),
                AchievementEntity("first_sip", "Initiation Ceremony", "Place your very first mobile pickup order.", true, "first_order", 3)
            )
            coffeeDao.insertAchievements(initialAchievements)
        }
    }

    suspend fun topUpWallet(amount: Double): Boolean {
        if (amount <= 0.0) return false
        val session = coffeeDao.getUserSession() ?: return false
        val newBalance = session.currentBalance + amount
        
        // Log transaction
        coffeeDao.insertTransaction(
            TransactionEntity(
                amount = amount,
                type = "TOP_UP",
                description = "Funds preloaded into digital wallet"
            )
        )
        // Update session
        coffeeDao.insertUserSession(session.copy(currentBalance = newBalance))
        return true
    }

    suspend fun checkoutOrder(
        storeId: String,
        menuItemId: String,
        customization: String,
        isFreeWithPoints: Boolean,
        scheduledPickupTime: String
    ): Result<OrderEntity> {
        val session = coffeeDao.getUserSession() ?: return Result.failure(Exception("No user logged in"))
        val store = coffeeDao.getStoreById(storeId) ?: return Result.failure(Exception("Select a pick up store"))
        val menuItem = coffeeDao.getMenuItemById(menuItemId) ?: return Result.failure(Exception("Invalid drink selected"))

        // Calculate customization markup if any
        var extPrice = menuItem.basePrice
        if (customization.contains("Large")) extPrice += 0.75
        if (customization.contains("Oat Milk") || customization.contains("Almond") || customization.contains("Coconut")) extPrice += 0.50
        if (customization.contains("Triple Shot")) extPrice += 1.20
        else if (customization.contains("Double Shot") && !menuItem.name.contains("Double Shot")) extPrice += 0.60
        if (customization.contains("Vanilla") || customization.contains("Caramel") || customization.contains("Hazelnut")) extPrice += 0.40

        // If gold member, give 10% discount on coffee purchased with cash/wallet
        if (session.membershipType == "Aurelia Gold" && !isFreeWithPoints) {
            extPrice *= 0.90
            // Round to 2 decimal places
            extPrice = Math.round(extPrice * 100.0) / 100.0
        }

        if (isFreeWithPoints) {
            val costInPoints = if (menuItem.isSeasonal) 15 else 10
            if (session.currentPoints < costInPoints) {
                return Result.failure(Exception("Insufficient loyalty tokens (Requires $costInPoints beans)"))
            }
            // Deduct points
            val updatedPoints = session.currentPoints - costInPoints
            
            // Generate Order
            val order = OrderEntity(
                storeId = store.id,
                storeName = store.name,
                itemName = menuItem.name,
                itemCustomization = customization + " (Reward Claimed)",
                totalPrice = 0.0,
                pointsEarned = 0,
                status = "PREPARING",
                scheduledPickupTime = scheduledPickupTime
            )
            val orderId = coffeeDao.insertOrder(order)
            
            // Update session
            coffeeDao.insertUserSession(session.copy(
                currentPoints = updatedPoints,
                totalOrdersCount = session.totalOrdersCount + 1,
                totalCaffeineThisMonthMg = session.totalCaffeineThisMonthMg + menuItem.caffeineMg
            ))

            // Log Transaction
            coffeeDao.insertTransaction(
                TransactionEntity(
                    amount = 0.0,
                    type = "PURCHASE",
                    description = "Claimed ${menuItem.name} using loyalty tokens"
                )
            )

            // Trigger streak evaluation
            evaluateStreaksAfterPurchase(session)

            return Result.success(order.copy(id = orderId))
        } else {
            if (session.currentBalance < extPrice) {
                return Result.failure(Exception("Insufficient digital wallet balance. Please top up."))
            }

            val updatedBalance = session.currentBalance - extPrice
            // 1 loyalty point (bean) per drink purchased
            val updatedPoints = session.currentPoints + 1

            val order = OrderEntity(
                storeId = store.id,
                storeName = store.name,
                itemName = menuItem.name,
                itemCustomization = customization,
                totalPrice = extPrice,
                pointsEarned = 1,
                status = "PREPARING",
                scheduledPickupTime = scheduledPickupTime
            )
            val orderId = coffeeDao.insertOrder(order)

            coffeeDao.insertUserSession(session.copy(
                currentBalance = updatedBalance,
                currentPoints = updatedPoints,
                totalOrdersCount = session.totalOrdersCount + 1,
                totalSpendingThisMonth = session.totalSpendingThisMonth + extPrice,
                totalCaffeineThisMonthMg = session.totalCaffeineThisMonthMg + menuItem.caffeineMg
            ))

            coffeeDao.insertTransaction(
                TransactionEntity(
                    amount = -extPrice,
                    type = "PURCHASE",
                    description = "Purchased ${menuItem.name} at ${store.name}"
                )
            )

            // Trigger streak evaluation
            evaluateStreaksAfterPurchase(session)

            return Result.success(order.copy(id = orderId))
        }
    }

    suspend fun buyAureliaGoldSubscription(): Boolean {
        val session = coffeeDao.getUserSession() ?: return false
        
        // Subscription can be bought with either 10 tokens OR $15.00
        val costCash = 15.00
        val costTokens = 15

        if (session.currentPoints >= costTokens) {
            val updatedPoints = session.currentPoints - costTokens
            val updatedSession = session.copy(
                membershipType = "Aurelia Gold",
                membershipExpiresAt = System.currentTimeMillis() + 30L * 24L * 60L * 60L * 1000L, // 30 days
                currentPoints = updatedPoints
            )
            coffeeDao.insertUserSession(updatedSession)
            coffeeDao.insertTransaction(
                TransactionEntity(
                    amount = 0.0,
                    type = "SUBSCRIPTION",
                    description = "Aurelia Gold 30-Day Pass (Redeemed 15 beans)"
                )
            )
            coffeeDao.unlockAchievement("gold_rank")
            return true
        } else if (session.currentBalance >= costCash) {
            val updatedBalance = session.currentBalance - costCash
            val updatedSession = session.copy(
                membershipType = "Aurelia Gold",
                membershipExpiresAt = System.currentTimeMillis() + 30L * 24L * 60L * 60L * 1000L, // 30 days
                currentBalance = updatedBalance
            )
            coffeeDao.insertUserSession(updatedSession)
            coffeeDao.insertTransaction(
                TransactionEntity(
                    amount = -costCash,
                    type = "SUBSCRIPTION",
                    description = "Aurelia Gold 30-Day Subscription"
                )
            )
            coffeeDao.unlockAchievement("gold_rank")
            return true
        }
        return false
    }

    suspend fun saveUsualDrink(menuItemId: String, customization: String) {
        val session = coffeeDao.getUserSession() ?: return
        coffeeDao.insertUserSession(session.copy(
            usualDrinkId = menuItemId,
            usualDrinkCustomization = customization
        ))
    }

    private suspend fun evaluateStreaksAfterPurchase(currentSession: UserSessionEntity) {
        val dateFormater = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = dateFormater.format(Date())
        val yesterdayStr = dateFormater.format(Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000L))

        var newStreak = currentSession.streakCount
        var newLongest = currentSession.longestStreakCount

        if (currentSession.lastVisitedDateString != todayStr) {
            if (currentSession.lastVisitedDateString == yesterdayStr) {
                // Streak continues!
                newStreak += 1
            } else {
                // Streak broken, or first visit
                newStreak = 1
            }
            if (newStreak > newLongest) {
                newLongest = newStreak
            }
            
            // Update session streak data
            val updatedSession = coffeeDao.getUserSession() // Reload latest
            if (updatedSession != null) {
                coffeeDao.insertUserSession(updatedSession.copy(
                    streakCount = newStreak,
                    longestStreakCount = newLongest,
                    lastVisitedDateString = todayStr
                ))
            }

            // Check if streak unlocks achievement
            if (newStreak >= 5) {
                coffeeDao.unlockAchievement("streak_5_days")
            }
        }

        // Check if caffeine targets met
        val latestSession = coffeeDao.getUserSession()
        if (latestSession != null && latestSession.totalCaffeineThisMonthMg >= 1600) {
            coffeeDao.unlockAchievement("caffeine_king")
        }
    }

    suspend fun updateOrderStatusByBarista(orderId: Long, status: String) {
        coffeeDao.updateOrderStatus(orderId, status)
    }

    suspend fun deleteCompletedOrders() {
        coffeeDao.clearAllOrders()
    }
}
