package com.dummbroke.profitpath.ui.navigation

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dummbroke.profitpath.R
import com.dummbroke.profitpath.ui.theme.ProfitPathTheme
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

// Define placeholder drawable resource names. 
// You will need to create/rename your PNGs in res/drawable to match these.
object NavDrawables {
    // These are Int values (resource IDs) after R generation, not actual file names here.
    // The actual files should be e.g. ic_nav_home.png, ic_nav_trade_entry.png etc.
    val ic_nav_home = R.drawable.ic_nav_home_placeholder // Replace with actual R.drawable.your_icon
    val ic_nav_trade_entry = R.drawable.ic_nav_trade_entry_placeholder
    val ic_nav_history = R.drawable.ic_nav_history_placeholder
    val ic_nav_performance = R.drawable.ic_nav_performance_placeholder
    val ic_nav_settings = R.drawable.ic_settings_placeholder // Example name, ensure this drawable exists
    val ic_nav_asset = R.drawable.ic_nav_asset_placeholder // Add this drawable for asset management
    val ic_nav_airdrops = R.drawable.ic_nav_airdrops_placeholder // Add this drawable for airdrops
}

sealed class Screen(
    val route: String, 
    val title: String, 
    @DrawableRes val iconResId: Int
) {
    object Home : Screen("home", "Home", NavDrawables.ic_nav_home)
    object TradeEntry : Screen("trade_entry", "New Trade Entry", NavDrawables.ic_nav_trade_entry)
    object TradeHistory : Screen("trade_history", "History", NavDrawables.ic_nav_history)
    object PerformanceSummary : Screen("performance_summary", "Performance", NavDrawables.ic_nav_performance)
    object Settings : Screen("settings", "Settings", NavDrawables.ic_nav_settings)
    object TradeAsset : Screen("trade_asset", "Manage Assets", NavDrawables.ic_nav_asset)
    object Airdrops : Screen("airdrops", "Airdrops", NavDrawables.ic_nav_airdrops)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.TradeEntry,
    Screen.TradeHistory
)

// Define items for the Navigation Drawer
val drawerNavItems = listOf(
    Screen.PerformanceSummary,
    Screen.Settings,
    Screen.TradeAsset,
    Screen.Airdrops
)

// Define referral links
private const val BYBIT_REFERRAL_LINK = "https://www.bybit.com/invite?ref=OKP8WAZ"
private const val EXNESS_REFERRAL_LINK = "https://one.exnesstrack.org/a/j9khljybbq"

@Composable
fun AppBottomNavigationBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        bottomNavItems.forEach { screen ->
            val selected = currentRoute == screen.route
            val scale by animateFloatAsState(
                targetValue = if (selected) 1.1f else 1.0f,
                animationSpec = tween(durationMillis = 200),
                label = "scaleAnimation"
            )
            val labelColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            val iconColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = screen.iconResId),
                        contentDescription = screen.title,
                        modifier = Modifier.scale(scale).size(25.dp)
                    )
                },
                label = {
                    if (selected) { 
                        Text(
                            screen.title,
                            modifier = Modifier.scale(scale), 
                            textAlign = TextAlign.Center, 
                            fontSize = 12.sp 
                        )
                    } 
                },
                selected = selected,
                onClick = { onNavigate(screen.route) },
                alwaysShowLabel = false, 
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = iconColor, 
                    selectedTextColor = labelColor,  
                    unselectedIconColor = iconColor, 
                    unselectedTextColor = labelColor, 
                    indicatorColor = MaterialTheme.colorScheme.surface 
                )
            )
        }
    }
}

@Composable
fun AdMobBanner(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = "ca-app-pub-3940256099942544/6300978111" // Test Ad Unit ID
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}

@Composable
fun AppDrawerContent(
    drawerItems: List<Screen>,
    currentRoute: String?,
    navigateToScreen: (String) -> Unit,
    closeDrawer: () -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.background // Match the app background
    ) {
        // Banner Ad at the top
        AdMobBanner(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        )
        Spacer(Modifier.height(8.dp))
        Spacer(Modifier.height(24.dp)) // Increased top spacer

        // Title for menu items
        Text(
            text = "Menu",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
        )

        drawerItems.forEachIndexed { index, screen ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp), // Padding around each card
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            painter = painterResource(id = screen.iconResId),
                            contentDescription = screen.title
                        )
                    },
                    label = { Text(screen.title, style = MaterialTheme.typography.labelLarge) },
                    selected = currentRoute == screen.route,
                    onClick = {
                        navigateToScreen(screen.route)
                        closeDrawer()
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding), // Standard item padding
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.onSurface,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                )
            }
            if (index < drawerItems.lastIndex) {
                // No explicit line needed, card margins create separation
            }
        }
        Spacer(Modifier.height(24.dp)) // Increased spacer between menu items and banners

        // Title for referral banners
        Text(
            text = "Explore Brokers", // Or a more convincing title
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
        )

        // Add Referral Banners below the menu items
        ReferralBanners()

        Spacer(Modifier.height(12.dp)) // Bottom spacer
    }
}

@Composable
fun ReferralBanners() {
    val uriHandler = LocalUriHandler.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp), // Add padding around banners
        horizontalAlignment = Alignment.CenterHorizontally // Center the banners
    ) {
        // Bybit Banner
        ReferralBannerItem(
            drawableResId = R.drawable.bybit_banner, // Use the drawable resource ID
            onClick = { uriHandler.openUri(BYBIT_REFERRAL_LINK) }
        )
        Spacer(Modifier.height(8.dp)) // Space between banners

        // Exness Banner
        ReferralBannerItem(
            drawableResId = R.drawable.exness_banner, // Use the drawable resource ID
            onClick = { uriHandler.openUri(EXNESS_REFERRAL_LINK) }
        )
    }
}

@Composable
fun ReferralBannerItem(
    @DrawableRes drawableResId: Int,
    onClick: () -> Unit
) {
    Image(
        painter = painterResource(id = drawableResId),
        contentDescription = "Referral Banner", // Generic content description, can be improved
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp) // Adjust height as needed
            .clip(RoundedCornerShape(8.dp)) // Add rounded corners to the image
            .clickable { onClick() } // Make the image clickable
    )
}

// For previews to work, you'll need placeholder drawable resources in your res/drawable folder
// with the names: ic_nav_home_placeholder, ic_nav_trade_entry_placeholder, etc.
// You can create simple XML vector drawables or copy any PNG for now.
// Example placeholder (ic_nav_home_placeholder.xml):
// <vector xmlns:android="http://schemas.android.com/apk/res/android"
//     android:width="24dp"
//     android:height="24dp"
//     android:viewportWidth="24"
//     android:viewportHeight="24">
//   <path
//       android:fillColor="@android:color/white"
//       android:pathData="M10,20v-6h4v6h5v-8h3L12,3 2,12h3v8z"/>
// </vector>

@Preview(showBackground = true)
@Composable
fun AppBottomNavigationBarPreview() {
    ProfitPathTheme {
        // Ensure placeholder drawables (e.g., ic_nav_home_placeholder) exist in res/drawable for this preview to work.
        var currentRoute by remember { mutableStateOf(Screen.Home.route) }
        AppBottomNavigationBar(
            currentRoute = currentRoute,
            onNavigate = { route -> currentRoute = route }
        )
    }
}

@Preview(showBackground = true, name = "Bottom Nav Light")
@Composable
fun AppBottomNavigationBarPreviewLight() {
    ProfitPathTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.surface) { // Use Surface for better preview
            var currentRoute by remember { mutableStateOf(Screen.Home.route) }
            AppBottomNavigationBar(
                currentRoute = currentRoute,
                onNavigate = { route -> currentRoute = route }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppBottomNavigationBarPreviewTradeHistorySelected() {
    ProfitPathTheme {
        // Ensure placeholder drawables (e.g., ic_nav_history_placeholder) exist in res/drawable for this preview to work.
        var currentRoute by remember { mutableStateOf(Screen.TradeHistory.route) }
        AppBottomNavigationBar(
            currentRoute = currentRoute,
            onNavigate = { route -> currentRoute = route }
        )
    }
}

@Preview(showBackground = true, name = "Bottom Nav History Light")
@Composable
fun AppBottomNavigationBarPreviewTradeHistorySelectedLight() {
    ProfitPathTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            var currentRoute by remember { mutableStateOf(Screen.TradeHistory.route) }
            AppBottomNavigationBar(
                currentRoute = currentRoute,
                onNavigate = { route -> currentRoute = route }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppDrawerContentPreview() {
    ProfitPathTheme {
        AppDrawerContent(
            drawerItems = drawerNavItems,
            currentRoute = Screen.PerformanceSummary.route, 
            navigateToScreen = {}, 
            closeDrawer = {}
        )
    }
}

@Preview(showBackground = true, name = "Drawer Content Light")
@Composable
fun AppDrawerContentPreviewLight() {
    ProfitPathTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background) { // Use Surface for better preview
            AppDrawerContent(
                drawerItems = drawerNavItems,
                currentRoute = Screen.PerformanceSummary.route, 
                navigateToScreen = {}, 
                closeDrawer = {}
            )
        }
    }
}
