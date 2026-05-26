package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.AureliaCoffeeApp
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CoffeeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as AureliaCoffeeApp).repository

    // DB Flows
    val stores: StateFlow<List<StoreEntity>> = repository.allStores
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val menuItems: StateFlow<List<MenuItemEntity>> = repository.allMenuItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userSession: StateFlow<UserSessionEntity?> = repository.userSession
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val orders: StateFlow<List<OrderEntity>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeOrders: StateFlow<List<OrderEntity>> = repository.activeOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val achievements: StateFlow<List<AchievementEntity>> = repository.allAchievements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Interactive States
    private val _selectedStore = MutableStateFlow<StoreEntity?>(null)
    val selectedStore = _selectedStore.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Seasonal")
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _customizingItem = MutableStateFlow<MenuItemEntity?>(null)
    val customizingItem = _customizingItem.asStateFlow()

    private val _selectedSize = MutableStateFlow("Regular")
    val selectedSize = _selectedSize.asStateFlow()

    private val _selectedMilk = MutableStateFlow("Whole Milk")
    val selectedMilk = _selectedMilk.asStateFlow()

    private val _selectedShots = MutableStateFlow("Double Shot")
    val selectedShots = _selectedShots.asStateFlow()

    private val _selectedSyrup = MutableStateFlow("None")
    val selectedSyrup = _selectedSyrup.asStateFlow()

    private val _scheduledTime = MutableStateFlow("As soon as possible")
    val scheduledTime = _scheduledTime.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage = _toastMessage.asStateFlow()

    init {
        // Default to first store once stores load
        viewModelScope.launch {
            stores.collect { list ->
                if (list.isNotEmpty() && _selectedStore.value == null) {
                    _selectedStore.value = list.first()
                }
            }
        }
    }

    fun selectStore(store: StoreEntity) {
        _selectedStore.value = store
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun startCustomizing(item: MenuItemEntity) {
        _customizingItem.value = item
        // Set defaults based on item properties
        _selectedSize.value = "Regular"
        _selectedMilk.value = if (item.name.contains("Matcha")) "Oat Milk" else "Whole Milk"
        _selectedShots.value = if (item.name.contains("Espresso") || item.name.contains("Cortado")) "Double Shot" else "Regular"
        _selectedSyrup.value = "None"
    }

    fun cancelCustomizing() {
        _customizingItem.value = null
    }

    fun setSize(size: String) { _selectedSize.value = size }
    fun setMilk(milk: String) { _selectedMilk.value = milk }
    fun setShots(shots: String) { _selectedShots.value = shots }
    fun setSyrup(syrup: String) { _selectedSyrup.value = syrup }
    fun setScheduledTime(time: String) { _scheduledTime.value = time }

    fun showToast(message: String) {
        _toastMessage.value = message
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun topUpWallet(amount: Double) {
        viewModelScope.launch {
            val success = repository.topUpWallet(amount)
            if (success) {
                showToast("Preloaded $${String.format("%.2f", amount)} successfully!")
            } else {
                showToast("Top up failed.")
            }
        }
    }

    fun checkoutCurrentOrder(isFreeWithPoints: Boolean) {
        val store = _selectedStore.value
        val item = _customizingItem.value
        if (store == null) {
            showToast("Please select a pickup store first")
            return
        }
        if (item == null) {
            showToast("No active item selected")
            return
        }

        viewModelScope.launch {
            val customizationStr = "${_selectedSize.value} · ${_selectedMilk.value} · ${_selectedShots.value} · ${_selectedSyrup.value}"
            val result = repository.checkoutOrder(
                storeId = store.id,
                menuItemId = item.id,
                customization = customizationStr,
                isFreeWithPoints = isFreeWithPoints,
                scheduledPickupTime = _scheduledTime.value
            )
            result.onSuccess { order ->
                if (isFreeWithPoints) {
                    showToast("Ritual Claimed! Your free ${order.itemName} is preparing.")
                } else {
                    showToast("Receipt Synced! Total: $${String.format("%.2f", order.totalPrice)}. Enjoy your ${order.itemName}!")
                }
                _customizingItem.value = null
            }.onFailure { exception ->
                showToast(exception.message ?: "Checkout failed. Check wallet balance.")
            }
        }
    }

    // "Order my usual" button -> 1 tap checkout shortcut!
    fun orderUsualShortcut() {
        viewModelScope.launch {
            val session = userSession.value ?: return@launch
            val favDrinkId = session.usualDrinkId
            val customizationStr = session.usualDrinkCustomization // e.g. "Regular · Whole Milk · Double Shot · Caramel"
            val store = _selectedStore.value ?: stores.value.firstOrNull()

            if (store == null) {
                showToast("Please select a pickup café first.")
                return@launch
            }

            val result = repository.checkoutOrder(
                storeId = store.id,
                menuItemId = favDrinkId,
                customization = customizationStr,
                isFreeWithPoints = false,
                scheduledPickupTime = "As soon as possible"
            )
            result.onSuccess { order ->
                showToast("Your usual is ordered! Enjoy ${order.itemName} in 3 mins.")
            }.onFailure { exception ->
                showToast("Unable to order your usual: " + (exception.message ?: "Insufficient funds."))
            }
        }
    }

    // Save customized item as standard "My Usual" drink shortcut
    fun saveAsUsual() {
        val item = _customizingItem.value ?: return
        val customizationStr = "${_selectedSize.value} · ${_selectedMilk.value} · ${_selectedShots.value} · ${_selectedSyrup.value}"
        viewModelScope.launch {
            repository.saveUsualDrink(item.id, customizationStr)
            showToast("${item.name} set as index usual order shortcut successfully!")
        }
    }

    fun buyGoldSubscription() {
        viewModelScope.launch {
            val success = repository.buyAureliaGoldSubscription()
            if (success) {
                showToast("Ecosystem Level-Up! Welcome to Aurelia Gold Elite.")
            } else {
                showToast("Unable to join: Requires $15.00 cash or 15 tokens.")
            }
        }
    }

    // BARISTA AND OPERATIONS WORKSHEETS
    fun updateOrderStatus(orderId: Long, nextStatus: String) {
        viewModelScope.launch {
            repository.updateOrderStatusByBarista(orderId, nextStatus)
            showToast("Order #${orderId} marked as $nextStatus")
        }
    }

    fun clearBaristaCompletedOrders() {
        viewModelScope.launch {
            repository.deleteCompletedOrders()
            showToast("Barista operations workspace cleared!")
        }
    }
}
