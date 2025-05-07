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
fun ProfileInfo(userProfile: UserProfile){
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Profile Image",
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = userProfile.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TradingViewDarkTextPrimary
            )
            Text(
                text = userProfile.tradingStyle,
                fontSize = 14.sp,
                color = TradingViewDarkTextPrimary
            )
        }
    }
}

@Preview(showBackground = true, name = "Profile Info Preview")
@Composable
fun ProfileInfoPreview() {
    Surface(
        color = TradingViewDarkBackground
    ) {
        ProfileInfo(UserProfile("Keen Thomas", "Swing Trader"))
    }
}
// Component Code:
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
                // Ensure you have ic_upward and ic_downward in your res/drawable
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

// Preview Code (copied from the original file):
@Preview(showBackground = true)
@Composable
fun BalanceOverviewPreview() {
    Surface(color = TradingViewDarkBackground, modifier = Modifier.padding(8.dp)) {
        BalanceOverview(AccountBalanceInfo(25000.0, -0.5))
    }
}