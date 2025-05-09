package com.dummbroke.profitpath.ui.home

import android.R.attr.shape
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
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
import com.dummbroke.profitpath.ui.theme.ProfitPathTheme

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
// These will be removed and replaced with MaterialTheme.colorScheme
// val TradingViewDarkBackground = Color(0xFF131722)
// val TradingViewDarkSurface = Color(0xFF1E222D)
// val TradingViewDarkTextPrimary = Color(0xFFD1D4DC)
// val TradingViewDarkTextSecondary = Color(0xFF8A93A6)
// val TradingViewGreen = Color(0xFF26A69A)
// val TradingViewRed = Color(0xFFEF5350)

@Composable
fun ProfileInfo(userProfile: UserProfile){
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Profile Image",
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface), // Updated color
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = userProfile.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground // Updated color
            )
            Text(
                text = userProfile.tradingStyle,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface // Updated color for secondary text
            )
        }
    }
}

@Preview(showBackground = true, name = "Profile Info Preview")
@Composable
fun ProfileInfoPreview() {
    ProfitPathTheme { // Wrapped in Theme
        Surface(
            color = MaterialTheme.colorScheme.background // Updated color
        ) {
            ProfileInfo(UserProfile("Keen Thomas", "Swing Trader"))
        }
    }
}

@Composable
fun BalanceOverview(accountBalanceInfo: AccountBalanceInfo) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), // Updated color
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                "Account Balance",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface // Updated color
            )
            Text(
                text = "$${"%.2f".format(accountBalanceInfo.balance)}",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground // Updated color
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                val changeColor = if (accountBalanceInfo.dailyChangePercentage >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error // Updated colors
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

@Preview(showBackground = true)
@Composable
fun BalanceOverviewPreview() {
    ProfitPathTheme { // Wrapped in Theme
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.padding(8.dp)) { // Updated color
            BalanceOverview(AccountBalanceInfo(25000.0, -0.5))
        }
    }
}

@Composable
fun HeaderSection(userProfile: UserProfile, accountBalanceInfo: AccountBalanceInfo) {
    Row (
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ){
        ProfileInfo(userProfile)
        BalanceOverview(accountBalanceInfo)
    }
}

@Preview(showBackground = true, name = "Header Section Preview")
@Composable
fun HeaderSectionPreview() {
    ProfitPathTheme { // Wrapped in Theme
        Surface(
            color = MaterialTheme.colorScheme.background, modifier = Modifier.padding(16.dp) // Updated color
        ) {
            HeaderSection(UserProfile("Keen Thomas", "Swing Trader"), AccountBalanceInfo(12345.67, 2.5)
            )
        }
    }
}

@Composable
fun StatCard(stat: TradeStatItem) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), // Updated color
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ){
        Column(
            modifier = Modifier
                .padding(16.dp)
                .widthIn(min = 100.dp),
            horizontalAlignment =  Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stat.value,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface // Updated color (text on card surface)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stat.label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) // Slightly dimmer for secondary label on card
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StatCardPreview() {
    ProfitPathTheme { // Wrapped in Theme
        Surface(color = MaterialTheme.colorScheme.background) { // Updated color
            StatCard(TradeStatItem("Win Rate", "75%"))
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
            color = MaterialTheme.colorScheme.onBackground, // Updated color
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

@Preview
@Composable
fun StatSectionPreview() {
    ProfitPathTheme { // Wrapped in Theme
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.padding(16.dp)) { // Updated color
            StatsSection(
                tradeStats = listOf(
                    TradeStatItem("Total Trades", "143"),
                    TradeStatItem("Win Rate", "75%"),
                    TradeStatItem("Avg Win", "$175"),
                )
            )
        }
    }
}

@Composable
fun InsightRow(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) { // Updated default color
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) // Updated color
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = valueColor)
    }
}

@Preview(showBackground = true, name = "Insight Row Preview")
@Composable
fun InsightRowPreview() {
    ProfitPathTheme { // Wrapped in Theme
        Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.padding(8.dp).width(300.dp)) { // Updated color
            Column {
                InsightRow("Biggest Win:", "$+250.00", MaterialTheme.colorScheme.primary) // Updated color
                InsightRow("Worst Loss:", "$-120.00", MaterialTheme.colorScheme.error) // Updated color
                InsightRow("Most Traded:", "BTC/USD")
            }
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
            color = MaterialTheme.colorScheme.onBackground, // Updated color
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), // Updated color
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                InsightRow("Biggest Win:", "$+${"%.2f".format(tradeInsights.biggestWin)}", MaterialTheme.colorScheme.primary) // Updated color
                InsightRow("Worst Loss:", "$-${"%.2f".format(kotlin.math.abs(tradeInsights.worstLoss))}", MaterialTheme.colorScheme.error) // Updated color
                InsightRow("Most Traded Asset:", tradeInsights.mostTradedAsset)
            }
        }
    }
}

@Preview(showBackground = true, name = "Insights Section Preview")
@Composable
fun InsightsSectionPreview() {
    ProfitPathTheme { // Wrapped in Theme
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.padding(16.dp)) { // Updated color
            InsightsSection(TradeInsights(350.75, -150.20, "ETH/USD"))
        }
    }
}

@Composable
fun RecentTradeCard(trade: RecentTradeItem) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), // Updated color
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
                    color = MaterialTheme.colorScheme.onSurface // Updated color
                )
                Text(
                    text = trade.date,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) // Updated color
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = (if (trade.isWin) "+" else "-") + "$${"%.2f".format(kotlin.math.abs(trade.amount))}",
                color = if (trade.isWin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error, // Updated colors
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(
                        color = if (trade.isWin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error, // Updated colors
                        shape = CircleShape
                    )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecentTradeCardPreview() {
    ProfitPathTheme { // Wrapped in Theme
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.padding(8.dp)) { // Updated color
            RecentTradeCard(RecentTradeItem("1", "2023-10-26", "Long ETH/USD", true, 150.75))
        }
    }
}

@Composable
fun RecentTradesSection(recentTrades: List<RecentTradeItem>) {
    Column {
        Text(
            "Recent Trades",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground, // Updated color
            modifier = Modifier.padding(bottom = 12.dp)
        )
        if (recentTrades.isEmpty()) {
            Text(
                "No recent trades to show.",
                color = MaterialTheme.colorScheme.onSurface, // Updated color
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                recentTrades.take(5).forEach { trade ->
                    RecentTradeCard(trade)
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Recent Trades - With Data")
@Composable
fun RecentTradesSectionWithDataPreview() {
    ProfitPathTheme { // Wrapped in Theme
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.padding(16.dp)) { // Updated color
            RecentTradesSection(
                recentTrades = listOf(
                    RecentTradeItem("1", "2024-07-28", "Long BTCUSDT", true, 150.0),
                    RecentTradeItem("2", "2024-07-27", "Short ETHUSDT", false, -75.5)
                )
            )
        }
    }
}

@Preview(showBackground = true, name = "Recent Trades - Empty")
@Composable
fun RecentTradesSectionEmptyPreview() {
    ProfitPathTheme { // Wrapped in Theme
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.padding(16.dp)) { // Updated color
            RecentTradesSection(recentTrades = emptyList())
        }
    }
}

@Composable
fun HomeScreen(
    userProfile: UserProfile = UserProfile("Trader Name", "Scalping"),
    accountBalanceInfo: AccountBalanceInfo = AccountBalanceInfo(10250.75, 1.2),
    tradeStats: List<TradeStatItem> = listOf(
        TradeStatItem("Total Trades", "152"),
        TradeStatItem("Win Rate", "68%"),
        TradeStatItem("Best Trade", "+$250.00")
    ),
    tradeInsights: TradeInsights = TradeInsights(250.00, -120.00, "EUR/USD"),
    recentTrades: List<RecentTradeItem> = listOf(
        RecentTradeItem("1", "2024-07-28", "Long BTCUSDT", true, 150.0),
        RecentTradeItem("2", "2024-07-27", "Short ETHUSDT", false, -75.5)
    )
) {
    ProfitPathTheme { // Wrapped main content in Theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background // Updated color
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
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomeScreenPreview() {
    ProfitPathTheme { // Ensure preview uses the theme
        HomeScreen()
    }
}
