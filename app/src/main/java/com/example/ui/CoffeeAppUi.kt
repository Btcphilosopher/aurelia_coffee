package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoffeeAppUi(viewModel: CoffeeViewModel) {
    var activeTab by remember { mutableStateOf("home") }
    val userSession by viewModel.userSession.collectAsState()
    val activeOrders by viewModel.activeOrders.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearToast()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    Brush.linearGradient(listOf(GoldAurelia, RichCaramel)),
                                    CircleShape
                                )
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Coffee,
                                contentDescription = "Logo",
                                tint = SoftWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "AURELIA COFFEE",
                                letterSpacing = 1.2.sp,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            val tierStr = userSession?.membershipType ?: "Regular Member"
                            Text(
                                text = if (tierStr == "Aurelia Gold") "✦ Aurelia Gold Member ✦" else "Loyalty Level: Standard",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (tierStr == "Aurelia Gold") GoldAurelia else CharcoalMuted
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.secondary
                ),
                actions = {
                    IconButton(
                        onClick = { activeTab = "barista" },
                        modifier = Modifier
                            .testTag("barista_panel_shortcut")
                            .clip(CircleShape)
                            .background(
                                if (activeTab == "barista") RichCaramel.copy(alpha = 0.15f) else Color.Transparent
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Kitchen,
                            contentDescription = "Barista operations",
                            tint = if (activeTab == "barista") RichCaramel else MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                val navItems = listOf(
                    NavItem("Home", "home", Icons.Default.Home, Icons.Outlined.Home),
                    NavItem("Menu", "menu", Icons.Default.Coffee, Icons.Outlined.Coffee),
                    NavItem("Wallet", "wallet", Icons.Default.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
                    NavItem("Discover", "discover", Icons.Default.Map, Icons.Outlined.Map),
                    NavItem("Rituals", "analytics", Icons.Default.Spa, Icons.Outlined.Spa)
                )

                navItems.forEach { item ->
                    val isSelected = activeTab == item.id
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { activeTab = item.id },
                        label = { Text(item.label, fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SoftWhite,
                            unselectedIconColor = CharcoalMuted,
                            selectedTextColor = RichCaramel,
                            unselectedTextColor = CharcoalMuted,
                            indicatorColor = RichCaramel
                        ),
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        modifier = Modifier.testTag("nav_item_${item.id}")
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = RichCaramel.copy(alpha = 0.03f),
                    radius = 450.dp.toPx(),
                    center = Offset(size.width, 0f)
                )
            }

            Column(modifier = Modifier.fillMaxSize()) {
                val ticketToShow = activeOrders.firstOrNull()
                if (ticketToShow != null && activeTab != "barista") {
                    ActiveTicketBanner(order = ticketToShow, onCollectClick = {
                        viewModel.updateOrderStatus(ticketToShow.id, "COLLECTED")
                    })
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    when (activeTab) {
                        "home" -> HomeScreen(viewModel, onNavigateToMenu = { activeTab = "menu" })
                        "menu" -> OrderMenuScreen(viewModel)
                        "wallet" -> WalletScreen(viewModel)
                        "discover" -> DiscoverScreen(viewModel)
                        "analytics" -> AnalyticsScreen(viewModel)
                        "barista" -> BaristaPanelScreen(viewModel)
                    }
                }
            }
        }
    }

    val customizingItem by viewModel.customizingItem.collectAsState()
    if (customizingItem != null) {
        CustomizationBottomSheet(
            viewModel = viewModel,
            item = customizingItem!!,
            onDismiss = { viewModel.cancelCustomizing() }
        )
    }
}

// ==================== TICKET BANNER ====================
@Composable
fun ActiveTicketBanner(order: OrderEntity, onCollectClick: () -> Unit) {
    val isReady = order.status == "READY"
    val backgroundColor = if (isReady) ForestGreen else EspressoDark

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp)
            .testTag("live_order_ticket"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            Brush.linearGradient(listOf(RichCaramel, GoldAurelia)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isReady) Icons.Default.CheckCircle else Icons.Default.Loop,
                        contentDescription = "Status",
                        tint = SoftWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (isReady) "READY ON THE COUNTER!" else "BREWING YOUR RITUAL...",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = GoldAurelia,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = order.itemName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SoftWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${order.storeName} · ${order.scheduledPickupTime}",
                        fontSize = 11.sp,
                        color = SoftSand.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isReady) {
                Button(
                    onClick = onCollectClick,
                    modifier = Modifier.testTag("collect_order_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAurelia, contentColor = EspressoDark),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("Collect", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            } else {
                Text(
                    text = "3m away",
                    fontSize = 10.sp,
                    color = SoftSand.copy(alpha = 0.8f),
                    modifier = Modifier
                        .background(SoftWhite.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ==================== HOME SCREEN ====================
@Composable
fun HomeScreen(viewModel: CoffeeViewModel, onNavigateToMenu: () -> Unit) {
    val userSession by viewModel.userSession.collectAsState()
    val achievements by viewModel.achievements.collectAsState()
    val selectedStore by viewModel.selectedStore.collectAsState()

    var showQrPaySheet by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    val username = userSession?.username ?: "Julian"
                    Text(
                        text = "Morning, $username".uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.2.sp,
                        color = CharcoalMuted
                    )
                    Text(
                        text = "Your Daily Ritual",
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = (-0.5).sp
                    )
                }
                // Avatar representation JP
                Box(
                    modifier = Modifier
                        .testTag("qp_header_button")
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MinimalistAccentBorder)
                        .border(1.dp, MinimalistTertiary, CircleShape)
                        .clickable { showQrPaySheet = true }
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (userSession?.username?.take(2) ?: "JP").uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftWhite
                        )
                    }
                }
            }
        }

        // LOYALTY CARD (Beans Tracker & Streak)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .drawBehind {
                        // Drawing top-right background circle decoration similar to HTML/Tailwind
                        drawCircle(
                            color = MinimalistTertiary.copy(alpha = 0.1f),
                            radius = 64.dp.toPx(),
                            center = Offset(size.width, 0f)
                        )
                    }
                    .padding(24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = "Loyalty Tokens",
                                fontSize = 13.sp,
                                color = SoftWhite.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${userSession?.currentPoints ?: 0}",
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftWhite,
                                    letterSpacing = (-1).sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Beans",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Light,
                                    color = SoftWhite,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }

                        // Gold Tier or Membership Type Badge
                        val tierStr = userSession?.membershipType ?: "Regular Member"
                        Box(
                            modifier = Modifier
                                .background(SoftWhite.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (tierStr == "Aurelia Gold") "GOLD TIER" else "STANDARD MEMBER",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoftWhite,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Progress bar track
                    val progress = (((userSession?.currentPoints ?: 0) % 10) / 10f).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(SoftWhite.copy(alpha = 0.2f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progress)
                                .background(MinimalistTertiary)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val remaining = 10 - ((userSession?.currentPoints ?: 0) % 10)
                        Text(
                            text = "$remaining more beans until your next free espresso",
                            fontSize = 11.sp,
                            color = SoftWhite.copy(alpha = 0.7f)
                        )

                        // Nice streak miniature info to ensure streak functionality is beautifully preserved inside the loyalty card
                        val streakDays = userSession?.streakCount ?: 1
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Streak",
                                tint = MinimalistTertiary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${streakDays}D Streak",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoftWhite
                            )
                        }
                    }
                }
            }
        }

        // TWO-COLUMN CORE GRID (Wallet Pass on left, Tap to Pay on right)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(112.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // WALLET CARD
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, SoftMutedGray)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = "Wallet",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "WALLET",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = GoldenBronze,
                                letterSpacing = 1.sp
                            )
                        }
                        Column {
                            Text(
                                text = "$${String.format("%.2f", userSession?.currentBalance ?: 0.0)}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkCharcoal
                            )
                            Text(
                                text = "Auto-reload: On",
                                fontSize = 10.sp,
                                color = CharcoalMuted
                            )
                        }
                    }
                }

                // TAP TO PAY CARD
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { showQrPaySheet = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MinimalistTertiary),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = "Scan",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Tap to Pay\nIn-Store",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // YOUR USUAL (Quick Reorder Shortcut) card
        item {
            val session = userSession
            if (session != null && session.usualDrinkId.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("usual_order_container"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, SoftMutedGray)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "YOUR USUAL",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CharcoalMuted,
                                letterSpacing = 1.2.sp
                            )
                            Text(
                                text = "Quick Reorder",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(MinimalistBg, RoundedCornerShape(12.dp))
                                    .border(1.dp, MinimalistAccentBorder, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalCafe,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = session.usualDrinkCustomization.substringBefore(" (").ifEmpty { "Oat Milk Cortado" },
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = session.usualDrinkCustomization,
                                    fontSize = 12.sp,
                                    color = CharcoalMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            IconButton(
                                onClick = { viewModel.orderUsualShortcut() },
                                modifier = Modifier
                                    .testTag("order_usual_button")
                                    .size(40.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = "Quick Reorder",
                                    tint = SoftWhite,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // NEARBY STATUS / MAP AMBIENCE CARD (With animatePulse status light)
        item {
            val storeName = selectedStore?.name ?: "SoHo Central"
            val distance = "3 min walk"
            val seats = "12 seats free"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MinimalistCardBg, RoundedCornerShape(16.dp))
                    .border(1.dp, MinimalistAccentBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Green pulsing dot matching animate-pulse in standard Tailwind HTML
                    val infiniteTransition = rememberInfiniteTransition()
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1.0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        )
                    )
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .graphicsLayer(alpha = alpha)
                            .background(Color(0xFF4CAF50), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Nearby: $storeName".uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "$distance · Quiet ambiance · $seats",
                            fontSize = 11.sp,
                            color = CharcoalMuted
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Explore Stores",
                    tint = MinimalistSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // MENU QUICK ACCESS BUTTON
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToMenu() },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, SoftMutedGray)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(MinimalistAccentBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = "Menu Book",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Browse Aurelia Menu",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Hot Single-Origins, Matchas & Sourdough Croissants",
                                fontSize = 11.sp,
                                color = CharcoalMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Proceed",
                        tint = CharcoalMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // HABIT ACHIEVEMENTS HIERARCHICAL LIST
        item {
            Column {
                Text(
                    text = "Habit Achievements",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    achievements.forEach { badge ->
                        AchievementBadgeCard(badge = badge)
                    }
                }
            }
        }
    }

    // QR QUICK PAY MODE POPUP
    if (showQrPaySheet) {
        AlertDialog(
            onDismissRequest = { showQrPaySheet = false },
            confirmButton = {
                TextButton(onClick = { showQrPaySheet = false }) {
                    Text("Close Panel", color = RichCaramel, fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Text(
                    "INSTANT DIGITAL WALLET PASS",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = RichCaramel,
                    letterSpacing = 1.2.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Scan this barcode at the pickup counter to sync transactions and redeem your claims.",
                        fontSize = 12.sp,
                        color = EspressoDark,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Canvas(
                        modifier = Modifier
                            .size(150.dp)
                            .background(SoftWhite, RoundedCornerShape(12.dp))
                            .border(1.dp, CreamBeige, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        val stroke = 3.dp.toPx()
                        drawRect(Color.Black, Offset(0f, 0f), size = Size(30f, 30f), style = Stroke(stroke))
                        drawRect(Color.Black, Offset(10f, 10f), size = Size(10f, 10f))

                        drawRect(Color.Black, Offset(size.width - 30f, 0f), size = Size(30f, 30f), style = Stroke(stroke))
                        drawRect(Color.Black, Offset(size.width - 20f, 10f), size = Size(10f, 10f))

                        drawRect(Color.Black, Offset(0f, size.height - 30f), size = Size(30f, 30f), style = Stroke(stroke))
                        drawRect(Color.Black, Offset(10f, size.height - 20f), size = Size(10f, 10f))

                        // central pixel blocks
                        for (i in 0..4) {
                            for (j in 0..4) {
                                if ((i + j) % 2 == 0) {
                                    drawRect(
                                        Color.Black,
                                        Offset(size.width * 0.3f + i * 14f, size.height * 0.3f + j * 14f),
                                        Size(8f, 8f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Loyalty Account ID: A-9482-1200",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalMuted
                    )
                    Text(
                        text = "Current Balance: $${String.format("%.2f", userSession?.currentBalance ?: 0.0)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = EspressoDark
                    )
                }
            },
            containerColor = SoftSand,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

// ==================== ORDER MENU SCREEN ====================
@Composable
fun OrderMenuScreen(viewModel: CoffeeViewModel) {
    val items by viewModel.menuItems.collectAsState()
    val stores by viewModel.stores.collectAsState()
    val selectedStore by viewModel.selectedStore.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    var showStoreSelectorSheet by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Store",
                        tint = RichCaramel,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = selectedStore?.name ?: "Search Collection Point",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = EspressoDark
                        )
                        Text(
                            text = selectedStore?.address ?: "Aurelia Strand Road",
                            fontSize = 11.sp,
                            color = CharcoalMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = CreamBeige)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Queue",
                                tint = RichCaramel,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Queue status: ${selectedStore?.queueTimeMinutes ?: 3} mins prep time",
                                fontSize = 10.sp,
                                color = RichCaramel,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { showStoreSelectorSheet = true },
                    modifier = Modifier.testTag("change_store_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CreamBeige, contentColor = EspressoDark),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Change", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }

        val categories = listOf("Seasonal", "Hot Drinks", "Cold Drinks", "Bakery")
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { cat ->
                val active = selectedCategory == cat
                FilterChip(
                    selected = active,
                    onClick = { viewModel.selectCategory(cat) },
                    label = { Text(cat, fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = RichCaramel,
                        selectedLabelColor = SoftWhite,
                        containerColor = CreamBeige,
                        labelColor = EspressoDark
                    ),
                    modifier = Modifier.testTag("category_chip_$cat")
                )
            }
        }

        val filteredItems = items.filter { it.category == selectedCategory }
        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading menu items...", color = CharcoalMuted, fontSize = 12.sp)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredItems) { menu ->
                    MenuItemCard(item = menu, onAddClick = { viewModel.startCustomizing(menu) })
                }
            }
        }
    }

    if (showStoreSelectorSheet) {
        AlertDialog(
            onDismissRequest = { showStoreSelectorSheet = false },
            confirmButton = {
                TextButton(onClick = { showStoreSelectorSheet = false }) {
                    Text("OK", color = RichCaramel, fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Text(
                    "Select Collection Point",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = EspressoDark
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(stores) { store ->
                        val isChosen = selectedStore?.id == store.id
                        val borderMod = if (isChosen) Modifier.border(1.5.dp, RichCaramel, RoundedCornerShape(12.dp)) else Modifier
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(borderMod)
                                .background(CreamBeige, RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.selectStore(store)
                                    showStoreSelectorSheet = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        if (isChosen) RichCaramel else CharcoalMuted.copy(alpha = 0.2f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Pin",
                                    tint = if (isChosen) SoftWhite else EspressoDark,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    store.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EspressoDark
                                )
                                Text(
                                    "${store.queueTimeMinutes}m wait · ${store.seatingAvailable}/${store.totalCapacity} Seats",
                                    fontSize = 11.sp,
                                    color = CharcoalMuted
                                )
                            }
                        }
                    }
                }
            },
            containerColor = SoftSand,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

// ==================== DISCOVER SCREEN ====================
@Composable
fun DiscoverScreen(viewModel: CoffeeViewModel) {
    val stores by viewModel.stores.collectAsState()
    val selectedStore by viewModel.selectedStore.collectAsState()

    var activeMapPinId by remember { mutableStateOf("st_strand") }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Aurelia Café Discovery Locator",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(CreamBeige)
                .border(1.dp, SoftMutedGray)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // map lanes background
                drawLine(color = SoftWhite, start = Offset(0f, 0f), end = Offset(size.width, size.height), strokeWidth = 24f)
                drawCircle(color = SoftWhite, radius = 50.dp.toPx(), center = Offset(size.width * 0.7f, size.height * 0.35f), style = Stroke(width = 16f))
                drawLine(color = SoftWhite, start = Offset(size.width * 0.35f, 0f), end = Offset(size.width * 0.35f, size.height), strokeWidth = 20f)
                drawLine(color = SoftWhite, start = Offset(0f, size.height * 0.7f), end = Offset(size.width, size.height * 0.7f), strokeWidth = 22f)

                val riverPath = Path().apply {
                    moveTo(0f, size.height * 0.9f)
                    quadraticTo(size.width * 0.5f, size.height * 0.82f, size.width, size.height * 0.95f)
                }
                drawPath(riverPath, color = Color(0xFFAAD2EB), style = Stroke(width = 32f))
            }

            MapPinIndicator(
                id = "st_strand",
                offsetY = 90.dp,
                offsetX = 160.dp,
                activeId = activeMapPinId,
                onClick = {
                    activeMapPinId = "st_strand"
                    stores.find { it.id == "st_strand" }?.let { viewModel.selectStore(it) }
                }
            )

            MapPinIndicator(
                id = "st_broadgate",
                offsetY = 60.dp,
                offsetX = 280.dp,
                activeId = activeMapPinId,
                onClick = {
                    activeMapPinId = "st_broadgate"
                    stores.find { it.id == "st_broadgate" }?.let { viewModel.selectStore(it) }
                }
            )

            MapPinIndicator(
                id = "st_soho",
                offsetY = 110.dp,
                offsetX = 80.dp,
                activeId = activeMapPinId,
                onClick = {
                    activeMapPinId = "st_soho"
                    stores.find { it.id == "st_soho" }?.let { viewModel.selectStore(it) }
                }
            )

            MapPinIndicator(
                id = "st_chelsea",
                offsetY = 180.dp,
                offsetX = 40.dp,
                activeId = activeMapPinId,
                onClick = {
                    activeMapPinId = "st_chelsea"
                    stores.find { it.id == "st_chelsea" }?.let { viewModel.selectStore(it) }
                }
            )
        }

        val highlightedStore = stores.find { it.id == activeMapPinId } ?: stores.firstOrNull()
        if (highlightedStore != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    highlightedStore.name,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = EspressoDark
                                )
                                if (highlightedStore.isPremium) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "PREMIUM ✦",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldAurelia,
                                        modifier = Modifier
                                            .background(EspressoDark, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(highlightedStore.address, fontSize = 12.sp, color = CharcoalMuted)
                        }

                        Button(
                            onClick = { viewModel.selectStore(highlightedStore) },
                            modifier = Modifier.testTag("set_active_store_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedStore?.id == highlightedStore.id) ForestGreen else RichCaramel
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (selectedStore?.id == highlightedStore.id) "Active Pick Up" else "Select For Order",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                item {
                    HorizontalDivider(color = SoftMutedGray)
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            label = "Prep Wait Time",
                            value = "${highlightedStore.queueTimeMinutes} MINS",
                            icon = Icons.Default.Timer,
                            color = RichCaramel,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            label = "Seating Available",
                            value = "${highlightedStore.seatingAvailable}/${highlightedStore.totalCapacity} Seats",
                            icon = Icons.Default.Chair,
                            color = ForestGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            label = "Pickup Shelf status",
                            value = "${highlightedStore.shelfLevelPercent}% Capacity",
                            icon = Icons.Default.LocalDrink,
                            color = GoldAurelia,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            label = "Opening Hours",
                            value = highlightedStore.openingHours,
                            icon = Icons.Default.Schedule,
                            color = CharcoalMuted,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Column {
                        Text("Ambience & Ritual Tags", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EspressoDark)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            highlightedStore.ambienceTags.split(",").forEach { tag ->
                                Text(
                                    text = tag.trim(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EspressoDark,
                                    modifier = Modifier
                                        .background(CreamBeige, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MapPinIndicator(
    id: String,
    offsetY: androidx.compose.ui.unit.Dp,
    offsetX: androidx.compose.ui.unit.Dp,
    activeId: String,
    onClick: () -> Unit
) {
    val isActive = activeId == id
    val scale by animateFloatAsState(if (isActive) 1.25f else 1.0f)
    val color = if (isActive) RichCaramel else EspressoLatte

    Box(
        modifier = Modifier
            .offset(x = offsetX, y = offsetY)
            .size(24.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .background(color, CircleShape)
            .border(2.dp, SoftWhite, CircleShape)
            .clickable(onClick = onClick)
            .testTag("map_pin_$id"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Pin icon",
            tint = SoftWhite,
            modifier = Modifier.size(12.dp)
        )
    }
}

// ==================== WALLET SCREEN ====================
@Composable
fun WalletScreen(viewModel: CoffeeViewModel) {
    val userSession by viewModel.userSession.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    var preloadingSum by remember { mutableStateOf(10.0) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        item {
            Text(
                text = "Preloaded Digital Wallet",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = EspressoDark),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AURELIA WALLET ACCOUNT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldAurelia,
                            letterSpacing = 1.sp
                        )
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "Wallet",
                            tint = SoftSand.copy(alpha = 0.6f)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text("Total preloaded cash balance", fontSize = 11.sp, color = SoftSand.copy(alpha = 0.7f))
                    Text(
                        text = "$${String.format("%.2f", userSession?.currentBalance ?: 0.0)}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = SoftWhite
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = SoftSand.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("LOYAL BEANS", fontSize = 9.sp, color = SoftSand.copy(alpha = 0.6f))
                            Text("${userSession?.currentPoints ?: 0} Tokens", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GoldAurelia)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("PREMIUM CODE", fontSize = 9.sp, color = SoftSand.copy(alpha = 0.6f))
                            Text("A-9482-1200", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SoftWhite)
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, SoftMutedGray)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Preload Wallet Balance",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = EspressoDark
                    )
                    Text(
                        "Charge cash into balance to activate 1-tap fast courier speeds.",
                        fontSize = 11.sp,
                        color = CharcoalMuted
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val preValues = listOf(10.0, 20.0, 50.0, 100.0)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        preValues.forEach { sum ->
                            val isSelected = preloadingSum == sum
                            Button(
                                onClick = { preloadingSum = sum },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("top_up_amount_$sum"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) RichCaramel else CreamBeige,
                                    contentColor = if (isSelected) SoftWhite else EspressoDark
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(vertical = 10.dp)
                            ) {
                                Text("$${sum.toInt()}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.topUpWallet(preloadingSum) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("preload_submit_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = EspressoDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = SoftWhite)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add $${String.format("%.2f", preloadingSum)} to Wallet", fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Feed,
                    contentDescription = "Ledger",
                    tint = RichCaramel,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Wallet Transaction Ledger",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = EspressoDark
                )
            }
        }

        if (transactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No transactions logged yet.", color = CharcoalMuted, fontSize = 11.sp)
                }
            }
        } else {
            items(transactions) { tx ->
                val isPurchase = tx.amount < 0.0
                val accentSign = if (isPurchase) "-" else "+"
                val colorAccent = if (isPurchase) AlertRed else ForestGreen

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CreamBeige.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    if (isPurchase) RichCaramel.copy(alpha = 0.12f) else ForestGreen.copy(alpha = 0.12f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPurchase) Icons.Default.LocalCafe else Icons.Default.AccountBalance,
                                contentDescription = "tx",
                                tint = if (isPurchase) RichCaramel else ForestGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                tx.description,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = EspressoDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val tFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
                            Text(
                                text = tFormat.format(Date(tx.timestamp)),
                                fontSize = 10.sp,
                                color = CharcoalMuted
                            )
                        }
                    }

                    Text(
                        text = "$accentSign$${String.format("%.2f", Math.abs(tx.amount))}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = colorAccent
                    )
                }
            }
        }
    }
}

// ==================== ANALYTICS SCREEN ====================
@Composable
fun AnalyticsScreen(viewModel: CoffeeViewModel) {
    val userSession by viewModel.userSession.collectAsState()

    var showMembershipDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        item {
            Text(
                text = "My Digital Coffee Rituals & Statistics",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        // SUBSCRIPTION BLOCK
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = EspressoDark),
                border = BorderStroke(1.5.dp, GoldAurelia)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = "Gold status",
                                tint = GoldAurelia,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "AURELIA GOLD MEMBERSHIP",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldAurelia,
                                letterSpacing = 1.2.sp
                            )
                        }

                        val isActive = userSession?.membershipType == "Aurelia Gold"
                        Text(
                            text = if (isActive) "ACTIVE" else "JOIN CLUB",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = EspressoDark,
                            modifier = Modifier
                                .background(if (isActive) LightSageGlow else SoftSand, RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Aurelia Gold Subscription Pass",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SoftWhite
                    )
                    Text(
                        text = "Claim 10% cash discount, immediate collection priority counters, and reserved fireplace club seating.",
                        fontSize = 12.sp,
                        color = SoftSand.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { showMembershipDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("subscribe_pass_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAurelia, contentColor = EspressoDark),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (userSession?.membershipType == "Aurelia Gold") "View Active Status Perks" else "Unlock Pass (15 Beans OR $15.00)",
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        // CAFFEINE CHART DRAWINGS
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, SoftMutedGray)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Caffeine Intake Tracker (Weekly Log)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = EspressoDark
                    )
                    Text(
                        "Your daily coffee stimulation index this week.",
                        fontSize = 11.sp,
                        color = CharcoalMuted
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    val weekData = listOf(140f, 280f, 150f, 320f, 120f, 80f, 210f)
                    val maxVal = 400f
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    ) {
                        val width = size.width
                        val height = size.height
                        val stepX = width / (weekData.size - 1)

                        val points = weekData.mapIndexed { index, caffeine ->
                            val x = index * stepX
                            val y = height - (caffeine / maxVal * height * 0.85f)
                            Offset(x, y)
                        }

                        val fillPath = Path().apply {
                            moveTo(0f, height)
                            points.forEach { lineTo(it.x, it.y) }
                            lineTo(width, height)
                            close()
                        }
                        drawPath(fillPath, brush = Brush.verticalGradient(listOf(RichCaramel.copy(alpha = 0.2f), Color.Transparent)))

                        val curvePath = Path().apply {
                            moveTo(points[0].x, points[0].y)
                            for (i in 1 until points.size) {
                                val prev = points[i - 1]
                                val cur = points[i]
                                cubicTo(
                                    (prev.x + cur.x) / 2f, prev.y,
                                    (prev.x + cur.x) / 2f, cur.y,
                                    cur.x, cur.y
                                )
                            }
                        }
                        drawPath(curvePath, color = RichCaramel, style = Stroke(width = 6f, cap = StrokeCap.Round))

                        points.forEach { pt ->
                            drawCircle(color = GoldAurelia, radius = 8f, center = pt)
                            drawCircle(color = SoftWhite, radius = 4f, center = pt)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                        days.forEach { day ->
                            Text(day, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CharcoalMuted)
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnalyticsStatBox(
                    title = "Monthly Spend",
                    value = "$${String.format("%.2f", userSession?.totalSpendingThisMonth ?: 124.50)}",
                    modifier = Modifier.weight(1f)
                )
                AnalyticsStatBox(
                    title = "Ritual Cups",
                    value = "${userSession?.totalOrdersCount ?: 22} Cups",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnalyticsStatBox(
                    title = "Avg Monthly Caffeine",
                    value = "${userSession?.totalCaffeineThisMonthMg ?: 1420} mg",
                    modifier = Modifier.weight(1f)
                )
                AnalyticsStatBox(
                    title = "Active Habit Streak",
                    value = "${userSession?.streakCount ?: 4} Days",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    if (showMembershipDialog) {
        val isGold = userSession?.membershipType == "Aurelia Gold"
        AlertDialog(
            onDismissRequest = { showMembershipDialog = false },
            confirmButton = {
                if (!isGold) {
                    Button(
                        onClick = {
                            viewModel.buyGoldSubscription()
                            showMembershipDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAurelia, contentColor = EspressoDark)
                    ) {
                        Text("Subscribe Now", fontWeight = FontWeight.Bold)
                    }
                } else {
                    TextButton(onClick = { showMembershipDialog = false }) {
                        Text("Awesome", color = RichCaramel)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showMembershipDialog = false }) {
                    Text("Close Panel")
                }
            },
            title = {
                Text(
                    text = if (isGold) "✦ Aurelia Gold Membership Active ✦" else "Unlock Aurelia Gold Elite",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = EspressoDark
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Gold benefits ensure direct customer priority paths across any store:",
                        fontSize = 12.sp,
                        color = EspressoDark
                    )
                    BulletPoint("10% instant checkout discount off cash orders")
                    BulletPoint("Redeem seasonal single-origins using standard Beans")
                    BulletPoint("Reserved elite leather booths & fireplace kitchen tables")
                    BulletPoint("Immediate pickup counter slot queue priority")

                    if (!isGold) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Account Price: 15 Beans or $15.00 for 30 Days.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = RichCaramel
                        )
                    }
                }
            },
            containerColor = SoftSand,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

// ==================== BARISTA OPERATIONS PANEL ====================
@Composable
fun BaristaPanelScreen(viewModel: CoffeeViewModel) {
    val orders by viewModel.orders.collectAsState()
    val userSession by viewModel.userSession.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Aurelia Kitchen Preparation Desk",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "Active mobile ordering streams.",
                    fontSize = 11.sp,
                    color = CharcoalMuted
                )
            }

            IconButton(
                onClick = { viewModel.clearBaristaCompletedOrders() },
                modifier = Modifier
                    .testTag("barista_clear_orders")
                    .size(36.dp)
                    .background(CreamBeige, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.CleaningServices,
                    contentDescription = "Clear Queue",
                    tint = RichCaramel,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        val activeQueue = orders.filter { it.status != "COLLECTED" }
        if (activeQueue.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.SoupKitchen,
                        contentDescription = "Empty",
                        tint = CharcoalMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Prep queue is empty.", fontWeight = FontWeight.Bold, color = EspressoDark)
                    Text("Place an order in the Menu tab to queue prep here!", fontSize = 11.sp, color = CharcoalMuted)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(activeQueue) { ord ->
                    BaristaOrderQueueCard(order = ord, viewModel = viewModel, customerSession = userSession)
                }
            }
        }
    }
}

@Composable
fun BaristaOrderQueueCard(order: OrderEntity, viewModel: CoffeeViewModel, customerSession: UserSessionEntity?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("barista_order_ticket_${order.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, if (order.status == "READY") ForestGreen else CreamBeige)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Ticket: #${order.id}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = RichCaramel,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = order.scheduledPickupTime,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = EspressoDark,
                            modifier = Modifier
                                .background(CreamBeige, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = order.itemName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = EspressoDark
                    )
                    Text(
                        text = order.itemCustomization,
                        fontSize = 11.sp,
                        color = CharcoalMuted
                    )
                }

                val isGold = customerSession?.membershipType == "Aurelia Gold"
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isGold) "✦ GOLD PASS ✦" else "STANDARD MEMBER",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isGold) GoldAurelia else CharcoalMuted
                    )
                    Text(
                        text = order.storeName,
                        fontSize = 10.sp,
                        color = CharcoalMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = SoftMutedGray)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Status: ${order.status}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (order.status == "READY") ForestGreen else RichCaramel
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (order.status == "PREPARING") {
                        Button(
                            onClick = { viewModel.updateOrderStatus(order.id, "READY") },
                            modifier = Modifier.testTag("barista_btn_ready_${order.id}"),
                            colors = ButtonDefaults.buttonColors(containerColor = RichCaramel, contentColor = SoftWhite),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                        ) {
                            Text("Mark Ready", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    } else if (order.status == "READY") {
                        Button(
                            onClick = { viewModel.updateOrderStatus(order.id, "COLLECTED") },
                            modifier = Modifier.testTag("barista_btn_collect_${order.id}"),
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen, contentColor = SoftWhite),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                        ) {
                            Text("Mark Collected", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ==================== CUSTOMIZATION BOTTOM SHEET ====================
@Composable
fun CustomizationBottomSheet(
    viewModel: CoffeeViewModel,
    item: MenuItemEntity,
    onDismiss: () -> Unit
) {
    val size by viewModel.selectedSize.collectAsState()
    val milk by viewModel.selectedMilk.collectAsState()
    val shots by viewModel.selectedShots.collectAsState()
    val syrup by viewModel.selectedSyrup.collectAsState()
    val schedTime by viewModel.scheduledTime.collectAsState()

    val userSession by viewModel.userSession.collectAsState()

    val adjustedPrice = remember(item, size, milk, shots, syrup, userSession) {
        var p = item.basePrice
        if (size == "Large") p += 0.75
        if (milk == "Oat Milk" || milk == "Almond Milk" || milk == "Coconut Milk") p += 0.50
        if (shots == "Triple Shot") p += 1.20
        else if (shots == "Double Shot" && !item.name.contains("Double Shot")) p += 0.60
        if (syrup != "None") p += 0.40

        if (userSession?.membershipType == "Aurelia Gold") {
            p *= 0.90
            p = Math.round(p * 100.0) / 100.0
        }
        p
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { viewModel.checkoutCurrentOrder(isFreeWithPoints = false) },
                modifier = Modifier.testTag("pay_checkout_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = EspressoDark, contentColor = SoftWhite)
            ) {
                Text("Prepay $${String.format("%.2f", adjustedPrice)}", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            val costInPoints = if (item.isSeasonal) 15 else 10
            val hasEnoughPoints = (userSession?.currentPoints ?: 0) >= costInPoints

            Button(
                onClick = { viewModel.checkoutCurrentOrder(isFreeWithPoints = true) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (hasEnoughPoints) GoldAurelia else SoftMutedGray,
                    contentColor = if (hasEnoughPoints) EspressoDark else CharcoalMuted
                ),
                enabled = hasEnoughPoints,
                modifier = Modifier.testTag("redeem_points_btn")
            ) {
                Text("Redeem: $costInPoints Beans", fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(item.name, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = EspressoDark)
                    Text("Customize your custom batch", fontSize = 11.sp, color = CharcoalMuted)
                }

                IconButton(
                    onClick = { viewModel.saveAsUsual() },
                    modifier = Modifier
                        .testTag("favorite_usual_heart")
                        .size(36.dp)
                        .background(CreamBeige, CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.Favorite, contentDescription = "Add Usual", tint = RichCaramel, modifier = Modifier.size(18.dp))
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Cup Size", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EspressoDark)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Small", "Regular", "Large").forEach { sz ->
                        val active = size == sz
                        CustomOptionChip(
                            label = sz + if (sz == "Large") " (+$0.75)" else "",
                            isActive = active,
                            onClick = { viewModel.setSize(sz) },
                            modifier = Modifier.testTag("size_chip_$sz")
                        )
                    }
                }

                Text("Milk Selection", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EspressoDark)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Whole Milk", "Oat Milk", "Almond Milk", "Coconut Milk", "Skimmed Milk").forEach { mk ->
                        val active = milk == mk
                        val costLabel = if (mk == "Whole Milk" || mk == "Skimmed Milk") "" else " (+$0.50)"
                        CustomOptionChip(
                            label = mk + costLabel,
                            isActive = active,
                            onClick = { viewModel.setMilk(mk) },
                            modifier = Modifier.testTag("milk_chip_$mk")
                        )
                    }
                }

                Text("Espresso Shots", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EspressoDark)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Single", "Double Shot", "Triple Shot").forEach { sh ->
                        val active = shots == sh
                        val costMark = when (sh) {
                            "Double Shot" -> " (+$0.60)"
                            "Triple Shot" -> " (+$1.20)"
                            else -> ""
                        }
                        CustomOptionChip(
                            label = sh + costMark,
                            isActive = active,
                            onClick = { viewModel.setShots(sh) },
                            modifier = Modifier.testTag("shot_chip_$sh")
                        )
                    }
                }

                Text("Syrup Flavour", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EspressoDark)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("None", "Vanilla", "Caramel", "Hazelnut").forEach { sy ->
                        val active = syrup == sy
                        CustomOptionChip(
                            label = sy + if (sy != "None") " (+$0.40)" else "",
                            isActive = active,
                            onClick = { viewModel.setSyrup(sy) },
                            modifier = Modifier.testTag("syrup_chip_$sy")
                        )
                    }
                }

                Text("Scheduled Pickup Time", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EspressoDark)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("As soon as possible", "In 10 mins", "In 20 mins", "In 30 mins").forEach { pickup ->
                        val active = schedTime == pickup
                        CustomOptionChip(
                            label = pickup,
                            isActive = active,
                            onClick = { viewModel.setScheduledTime(pickup) },
                            modifier = Modifier.testTag("pickup_time_$pickup")
                        )
                    }
                }
            }
        },
        containerColor = SoftSand,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun MenuItemCard(item: MenuItemEntity, onAddClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("menu_item_${item.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(Color(0xFFEFE9E2))
                    .padding(8.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f + 10f)
                    if (item.imageType == "matcha") {
                        drawCircle(color = ForestGreen.copy(alpha = 0.8f), radius = 35.dp.toPx(), center = center)
                        drawCircle(color = LightSageGlow, radius = 25.dp.toPx(), center = center)
                    } else if (item.imageType == "croissant") {
                        val crescentPath = Path().apply {
                            moveTo(center.x - 40f, center.y + 10f)
                            quadraticTo(center.x, center.y - 40f, center.x + 40f, center.y + 10f)
                            quadraticTo(center.x, center.y - 10f, center.x - 40f, center.y + 10f)
                        }
                        drawPath(crescentPath, color = GoldenBronze)
                    } else if (item.imageType == "flat_white" || item.imageType == "latte") {
                        drawCircle(color = EspressoLatte, radius = 35.dp.toPx(), center = center)
                        val artPath = Path().apply {
                            moveTo(center.x, center.y + 20f)
                            cubicTo(center.x - 20f, center.y, center.x - 10f, center.y - 20f, center.x, center.y - 10f)
                            cubicTo(center.x + 10f, center.y - 20f, center.x + 20f, center.y, center.x, center.y + 20f)
                        }
                        drawPath(artPath, color = SoftWhite.copy(alpha = 0.9f))
                    } else {
                        drawCircle(color = EspressoDark, radius = 30.dp.toPx(), center = center)
                        drawCircle(color = RichCaramel, radius = 22.dp.toPx(), center = center, style = Stroke(width = 4f))
                    }
                }

                if (item.isSeasonal) {
                    Box(
                        modifier = Modifier
                            .background(GoldAurelia, RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .align(Alignment.TopStart)
                    ) {
                        Text(
                            text = "SEASONAL ✦",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = EspressoDark
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = item.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = EspressoDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.description,
                    fontSize = 11.sp,
                    color = CharcoalMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.height(34.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$${String.format("%.2f", item.basePrice)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = RichCaramel
                    )

                    IconButton(
                        onClick = onAddClick,
                        modifier = Modifier
                            .testTag("add_item_btn_${item.id}")
                            .size(34.dp)
                            .background(RichCaramel, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Customize",
                            tint = SoftWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// ==================== HELPER STRUCTS & COMPOSABLES ====================

data class NavItem(
    val label: String,
    val id: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun AchievementBadgeCard(badge: AchievementEntity) {
    Card(
        modifier = Modifier
            .width(130.dp)
            .testTag("achievement_badge_${badge.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, SoftMutedGray)
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (badge.isUnlocked) RichCaramel.copy(alpha = 0.15f) else CreamBeige,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (badge.iconType) {
                        "early_bird" -> Icons.Default.WbSunny
                        "streak" -> Icons.Default.LocalFireDepartment
                        "gold_status" -> Icons.Default.Verified
                        "caffeine" -> Icons.Default.Bolt
                        else -> Icons.Default.StarOutline
                    },
                    contentDescription = badge.title,
                    tint = if (badge.isUnlocked) RichCaramel else CharcoalMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = badge.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = EspressoDark,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = badge.description,
                fontSize = 10.sp,
                color = CharcoalMuted,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 11.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            if (badge.isUnlocked) {
                Text(
                    text = "UNLOCKED",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    color = ForestGreen
                )
            } else {
                Text(
                    text = "LOCKED",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = CharcoalMuted
                )
            }
        }
    }
}

@Composable
fun AnalyticsStatBox(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, SoftMutedGray)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(text = title, fontSize = 11.sp, color = CharcoalMuted)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = EspressoDark)
        }
    }
}

@Composable
fun BulletPoint(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text("•", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = RichCaramel)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, fontSize = 12.sp, color = EspressoDark)
    }
}

@Composable
fun MetricCard(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, SoftMutedGray)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(color.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = label, fontSize = 10.sp, color = CharcoalMuted)
                Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EspressoDark)
            }
        }
    }
}

@Composable
fun CustomOptionChip(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                if (isActive) RichCaramel else CreamBeige,
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isActive) SoftWhite else EspressoDark
        )
    }
}
