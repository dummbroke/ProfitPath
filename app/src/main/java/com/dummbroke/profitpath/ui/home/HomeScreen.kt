package com.dummbroke.profitpath.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dummbroke.profitpath.R
import com.dummbroke.profitpath.ui.theme.ProfitPathTheme // Assuming you have a theme defined

// --- Data Classes (Placeholders) ---
data class UserProfile(
    val name: String,
    val tradingStyle: String,
    val avatarUrl: String? = null // Placeholder for image loading
)

data class AccountBalanceInfo(
    val balance: Double,
    val dailyChangePercentage: Double
)

data class TradeStatItem(
    val label: String,
    val value: String
)

data class TradeInsights(
    val biggestWin: Double,
    val worstLoss: Double,
    val mostTradedAsset: String
)

data class RecentTradeItem(
    val id: String,
    val date: String,
    val description: String,
    val isWin: Boolean,
    val amount: Double
)

// --- TradingView Inspired Colors (Dark Theme) ---
val TradingViewDarkBackground = Color(0xFF131722)
val TradingViewDarkSurface = Color(0xFF1E222D)
val TradingViewDarkTextPrimary = Color(0xFFD1D4DC)
val TradingViewDarkTextSecondary = Color(0xFF8A93A6)
val TradingViewGreen = Color(0xFF26A69A)
val TradingViewRed = Color(0xFFEF5350)


@Composable
fun HomeScreen(
    // Normally, you'd pass a ViewModel or state holders here
    userProfile: UserProfile = UserProfile("Trader Name", "Scalping"),
    accountBalanceInfo: AccountBalanceInfo = AccountBalanceInfo(10250.75, 1.2),
    tradeStats: List<TradeStatItem> = listOf(
        TradeStatItem("Total Trades", "152"),
        TradeStatItem("Win Rate", "68%"),
        TradeStatItem("Best Trade", "+$250.00"),
        TradeStatItem("Worst Trade", "-$120.00")
    ),
    tradeInsights: TradeInsights = TradeInsights(250.00, -120.00, "EUR/USD"),
    recentTrades: List<RecentTradeItem> = listOf(
        RecentTradeItem("1", "2024-07-28", "Long BTCUSDT", true, 150.0),
        RecentTradeItem("2", "2024-07-27", "Short ETHUSDT", false, -75.5),
        RecentTradeItem("3", "2024-07-27", "Long SOLUSDT", true, 90.25),
        RecentTradeItem("4", "2024-07-26", "Short ADAUSDT", true, 60.0),
        RecentTradeItem("5", "2024-07-25", "Long XRPUSDT", false, -30.0)
    )
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = TradingViewDarkBackground
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { HeaderSection(userProfile, accountBalanceInfo) }
            item { StatsSection(tradeStats) }
            item { InsightsSection(tradeInsights) }
            item { RecentTradesSection(recentTrades) }
        }
    }
}

@Composable
fun HeaderSection(userProfile: UserProfile, accountBalanceInfo: AccountBalanceInfo) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ProfileInfo(userProfile)
        BalanceOverview(accountBalanceInfo)
    }
}

@Composable
fun ProfileInfo(userProfile: UserProfile) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Placeholder for Profile Image
        Image(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Profile Avatar",
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(TradingViewDarkSurface),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = userProfile.name,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TradingViewDarkTextPrimary
            )
            Text(
                text = userProfile.tradingStyle,
                fontSize = 14.sp,
                color = TradingViewDarkTextSecondary
            )
        }
    }
}

@Composable
fun BalanceOverview(accountBalanceInfo: AccountBalanceInfo) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = TradingViewDarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                "Account Balance",
                fontSize = 12.sp,
                color = TradingViewDarkTextSecondary
            )
            Text(
                text = "$${"%.2f".format(accountBalanceInfo.balance)}",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = TradingViewDarkTextPrimary
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                val changeColor = if (accountBalanceInfo.dailyChangePercentage >= 0) TradingViewGreen else TradingViewRed
                val arrowResource = if (accountBalanceInfo.dailyChangePercentage >= 0) R.drawable.ic_upward else R.drawable.ic_downward

                Image(
                    painter = painterResource(id = arrowResource),
                    contentDescription = "Daily Change",
                    colorFilter = ColorFilter.tint(changeColor),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${"%.1f".format(accountBalanceInfo.dailyChangePercentage)}%",
                    fontSize = 14.sp,
                    color = changeColor
                )
            }
        }
    }
}

@Composable
fun StatsSection(tradeStats: List<TradeStatItem>) {
    Column {
        Text(
            "Trade Summary",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = TradingViewDarkTextPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(tradeStats) { stat ->
                StatCard(stat)
            }
        }
    }
}

@Composable
fun StatCard(stat: TradeStatItem) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = TradingViewDarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .widthIn(min = 100.dp), // Ensure cards have some minimum width
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stat.value,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TradingViewDarkTextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stat.label,
                fontSize = 12.sp,
                color = TradingViewDarkTextSecondary
            )
        }
    }
}

@Composable
fun InsightsSection(tradeInsights: TradeInsights) {
    Column {
        Text(
            "Trade Insights",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = TradingViewDarkTextPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = TradingViewDarkSurface),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                InsightRow("Biggest Win:", "$+${"%.2f".format(tradeInsights.biggestWin)}", TradingViewGreen)
                InsightRow("Worst Loss:", "$-${"%.2f".format(kotlin.math.abs(tradeInsights.worstLoss))}", TradingViewRed)
                InsightRow("Most Traded Asset:", tradeInsights.mostTradedAsset)
            }
        }
    }
}

@Composable
fun InsightRow(label: String, value: String, valueColor: Color = TradingViewDarkTextPrimary) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = TradingViewDarkTextSecondary)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = valueColor)
    }
}

@Composable
fun RecentTradesSection(recentTrades: List<RecentTradeItem>) {
    Column {
        Text(
            "Recent Trades",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = TradingViewDarkTextPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        if (recentTrades.isEmpty()) {
            Text(
                "No recent trades to show.",
                color = TradingViewDarkTextSecondary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            // Using a simple Column as LazyColumn inside LazyColumn is not ideal without fixed height.
            // For a long list, this section might need its own scroll or a fixed height.
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                recentTrades.take(5).forEach { trade -> // Display up to 5 recent trades
                    RecentTradeCard(trade)
                }
            }
        }
    }
}

@Composable
fun RecentTradeCard(trade: RecentTradeItem) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = TradingViewDarkSurface),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = trade.description,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = TradingViewDarkTextPrimary
                )
                Text(
                    text = trade.date,
                    fontSize = 12.sp,
                    color = TradingViewDarkTextSecondary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = (if (trade.isWin) "+" else "-") + "$${"%.2f".format(kotlin.math.abs(trade.amount))}",
                color = if (trade.isWin) TradingViewGreen else TradingViewRed,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(
                        color = if (trade.isWin) TradingViewGreen else TradingViewRed,
                        shape = CircleShape
                    )
            )
        }
    }
}


@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomeScreenPreview() {
    //ProfitPathTheme { // Assuming your theme handles dark mode
        HomeScreen()
    //}
}

@Preview(showBackground = true)
@Composable
fun StatCardPreview() {
    //ProfitPathTheme {
        Surface(color = TradingViewDarkBackground) {
            StatCard(TradeStatItem("Win Rate", "75%"))
        }
    //}
}

@Preview(showBackground = true)
@Composable
fun RecentTradeCardPreview() {
    //ProfitPathTheme {
         Surface(color = TradingViewDarkBackground, modifier = Modifier.padding(8.dp)) {
            RecentTradeCard(RecentTradeItem("1", "2023-10-26", "Long ETH/USD", true, 150.75))
        }
    //}
}

@Preview(showBackground = true)
@Composable
fun BalanceOverviewPreview() {
    //ProfitPathTheme {
        Surface(color = TradingViewDarkBackground, modifier = Modifier.padding(8.dp)) {
            BalanceOverview(AccountBalanceInfo(25000.0, -0.5))
        }
    //}
}

// Note: The Floating Action Button (FAB) for new trade entry and the
// Bottom Navigation Bar / Burger Menu Icon for navigation are typically part of a Scaffold
// in your main activity or a higher-level navigation composable, wrapping this HomeScreen. 