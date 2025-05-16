package com.dummbroke.profitpath.ui.trade_history

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dummbroke.profitpath.R
import com.dummbroke.profitpath.ui.theme.ProfitPathTheme
import androidx.navigation.NavHostController
import androidx.compose.ui.platform.LocalContext
import com.dummbroke.profitpath.ui.navigation.Screen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.window.Dialog
import com.dummbroke.profitpath.core.models.Trade
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dummbroke.profitpath.ui.trade_history.TradeHistoryUiState
import com.dummbroke.profitpath.ui.trade_history.TradeHistoryViewModel
import androidx.compose.foundation.Image
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState

// --- Data Class for Trade History Item ---
data class TradeHistoryItem(
    val id: String,
    val date: String,
    val assetPair: String,
    val outcome: String, // "Win", "Loss", "Breakeven"
    val pnl: Double,
    val riskRewardRatio: String?, // e.g., "2.5:1"
    val strategy: String?,
    val screenshotUri: String? = null, // Added for future use with dialog
    // Add all other fields from TradeDetailData that you need for the dialog
    val assetClass: String = "Forex",
    val marketCondition: String = "Trending",
    val positionType: String = "Long",
    val entryPrice: Double? = 1.2345,
    val stopLossPrice: Double? = 1.2300,
    val takeProfitPrice: Double? = 1.2400,
    val entryAmountUSD: Double? = 1000.0,
    val balanceBeforeTrade: Double? = 10000.0,
    val accountBalanceAfterTrade: Double? = 10050.0,
    val preTradeRationale: String = "Price broke above resistance.",
    val executionNotes: String = "Slight slippage on entry.",
    val postTradeReview: String = "Should have held longer.",
    val tags: List<String> = listOf("Breakout", "EURUSD"),
    val percentagePnl: Double? = 5.0,
    val exitTimestamp: String? = "2023-10-27 10:30",
    val entryClientTimestamp: String? = "2023-10-27 09:00",
    val balanceUpdated: Boolean = true,
    val leverage: Double? = 50.0
)

enum class TradeOutcome {
    WIN, LOSS, BREAK_EVEN
}

const val DEFAULT_FILTER_ALL = "All"
const val DEFAULT_FILTER_ANY = "Any"


data class TradeDetailData(
    val id: String,
    val assetPair: String,
    val assetClass: String,
    val date: String,
    val strategy: String,
    val marketCondition: String,
    val positionType: String,
    val entryPrice: Double?,
    val stopLossPrice: Double?,
    val takeProfitPrice: Double?,
    val outcome: String,
    val pnlAmount: Double?,
    val entryAmountUSD: Double?,
    val preTradeRationale: String,
    val executionNotes: String,
    val postTradeReview: String,
    val tags: List<String>,
    val screenshotUri: String?,
    val percentagePnl: Double?,
    val exitTimestamp: String?,
    val entryClientTimestamp: String?,
    val balanceUpdated: Boolean,
    val leverage: Double? = null
)

fun formatCurrencyDetail(value: Double?, defaultText: String = "N/A"): String {
    return value?.let { String.format(Locale.US, "$%.2f", it) } ?: defaultText
}

fun formatPercentage(value: Double?): String {
    return value?.let { String.format(Locale.US, "%.2f%%", it) } ?: "N/A"
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun InfoRow(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium, modifier = Modifier.weight(0.45f))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = valueColor, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(0.55f), textAlign = TextAlign.End)
    }
}
// --- End of copied code ---

// Helper to convert TradeHistoryItem to TradeDetailData for the dialog
fun TradeHistoryItem.toTradeDetailData(): TradeDetailData {
    return TradeDetailData(
        id = this.id,
        assetPair = this.assetPair,
        assetClass = this.assetClass,
        date = this.date, // This should be the primary date display for the trade
        strategy = this.strategy ?: "N/A",
        marketCondition = this.marketCondition,
        positionType = this.positionType,
        entryPrice = this.entryPrice,
        stopLossPrice = this.stopLossPrice,
        takeProfitPrice = this.takeProfitPrice,
        outcome = this.outcome,
        pnlAmount = this.pnl,
        entryAmountUSD = this.entryAmountUSD,
        preTradeRationale = this.preTradeRationale,
        executionNotes = this.executionNotes,
        postTradeReview = this.postTradeReview,
        tags = this.tags,
        screenshotUri = this.screenshotUri,
        percentagePnl = this.percentagePnl,
        exitTimestamp = this.exitTimestamp,
        entryClientTimestamp = this.entryClientTimestamp,
        balanceUpdated = this.balanceUpdated,
        leverage = this.leverage
    )
}

fun Trade.toTradeDetailData(id: String): TradeDetailData {
    return TradeDetailData(
        id = id,
        assetPair = this.specificAsset ?: "N/A",
        assetClass = this.assetClass ?: "N/A",
        date = this.tradeDate?.toDate()?.let { java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(it) } ?: "N/A",
        strategy = this.strategyUsed ?: "N/A",
        marketCondition = this.marketCondition ?: "N/A",
        positionType = this.positionType ?: "N/A",
        entryPrice = this.entryPrice,
        stopLossPrice = this.stopLossPrice,
        takeProfitPrice = this.takeProfitPrice,
        outcome = this.outcome ?: "N/A",
        pnlAmount = this.pnlAmount,
        entryAmountUSD = this.entryAmountUSD,
        preTradeRationale = this.preTradeRationale ?: "",
        executionNotes = this.executionNotes ?: "",
        postTradeReview = this.postTradeReview ?: "",
        tags = this.tags ?: emptyList(),
        screenshotUri = this.screenshotPath,
        percentagePnl = this.percentagePnl,
        exitTimestamp = this.exitTimestamp?.toDate()?.let { java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(it) },
        entryClientTimestamp = this.entryClientTimestamp?.toDate()?.let { java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(it) },
        balanceUpdated = this.balanceUpdated,
        leverage = this.leverage
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradeHistoryScreen(navController: NavHostController, viewModel: TradeHistoryViewModel = viewModel()) {
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var showFilters by remember { mutableStateOf(false) }
    var showDetailDialog by remember { mutableStateOf(false) }
    var selectedTradeIdForDialog by remember { mutableStateOf<String?>(null) }

    // --- Filter State ---
    var selectedAssetClass by remember { mutableStateOf("All") }
    var selectedStrategy by remember { mutableStateOf("All") }
    var selectedOutcome by remember { mutableStateOf("All") }
    var selectedMarketCondition by remember { mutableStateOf("All") }
    var selectedDateRange by remember { mutableStateOf("All") }

    val assetClassOptions = listOf("All", "Forex", "Stocks", "Crypto")
    val strategyOptions = listOf("All", "Bullish Trading", "Bearish Trading", "Trend Trading", "Momentum Trading", "Mean Reversion", "Breakout Trading", "Reversal Trading", "Range Trading", "Gap Trading", "Price Action Trading", "News Trading", "Earnings Trading", "Merger & Acquisition Trading", "Central Bank Policy Trading", "Algorithmic Trading", "Arbitrage Trading", "High-Frequency Trading (HFT)", "Pairs Trading")
    val outcomeOptions = listOf("All", "Win", "Loss", "Breakeven")
    val marketConditionOptions = listOf("All", "Bullish Trend", "Bearish Trend", "Ranging", "High Volatility", "Low Volatility", "News Event")
    val dateRangeOptions = listOf("All", "Today", "Last 7 Days", "This Month")

    val uiState by viewModel.uiState.collectAsState()

    // Filtering and mapping
    val trades: List<Pair<String, Trade>> = when (uiState) {
        is TradeHistoryUiState.Success -> (uiState as TradeHistoryUiState.Success).trades
        else -> emptyList()
    }
    val filteredTrades = trades.filter { (id, trade) ->
        val asset = trade.specificAsset ?: ""
        val strategy = trade.strategyUsed ?: ""
        val outcome = trade.outcome ?: ""
        val marketCondition = trade.marketCondition ?: ""
        val tradeDate = trade.tradeDate?.toDate()
        val tags = trade.tags ?: emptyList()
        val matchesSearch = searchQuery.text.isBlank() ||
            asset.contains(searchQuery.text, ignoreCase = true) ||
            strategy.contains(searchQuery.text, ignoreCase = true) ||
            outcome.contains(searchQuery.text, ignoreCase = true) ||
            tags.any { it.contains(searchQuery.text, ignoreCase = true) }
        val matchesAssetClass = selectedAssetClass == "All" || (trade.assetClass ?: "") == selectedAssetClass
        val matchesStrategy = selectedStrategy == "All" || strategy == selectedStrategy
        val matchesOutcome = selectedOutcome == "All" || outcome.equals(selectedOutcome, ignoreCase = true)
        val matchesMarketCondition = selectedMarketCondition == "All" || marketCondition == selectedMarketCondition
        val matchesDate = when (selectedDateRange) {
            "All" -> true
            "Today" -> tradeDate != null && android.text.format.DateUtils.isToday(tradeDate.time)
            "Last 7 Days" -> tradeDate != null && tradeDate.after(java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, -7) }.time)
            "This Month" -> tradeDate != null && tradeDate.month == java.util.Calendar.getInstance().time.month && tradeDate.year == java.util.Calendar.getInstance().time.year
            else -> true
        }
        matchesSearch && matchesAssetClass && matchesStrategy && matchesOutcome && matchesMarketCondition && matchesDate
    }

    if (showDetailDialog && selectedTradeIdForDialog != null) {
        val trade = trades.find { it.first == selectedTradeIdForDialog }?.second
        if (trade != null) {
            TradeDetailDialog(
                tradeDetail = trade.toTradeDetailData(selectedTradeIdForDialog!!),
                onDismissRequest = {
                    showDetailDialog = false
                    selectedTradeIdForDialog = null
                },
                onEdit = {
                    navController.navigate("edit_trade?tradeId=${selectedTradeIdForDialog}")
                    showDetailDialog = false
                    selectedTradeIdForDialog = null
                },
                onDelete = {
                    viewModel.deleteTrade(selectedTradeIdForDialog!!)
                    showDetailDialog = false
                    selectedTradeIdForDialog = null
                }
            )
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            SearchBar(searchQuery) { searchQuery = it }
            Spacer(modifier = Modifier.height(8.dp))
            // --- Filter Button and Section ---
            Button(
                onClick = { showFilters = !showFilters },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Icon(Icons.Filled.FilterList, contentDescription = "Filter Options", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                Spacer(Modifier.width(8.dp))
                Text("Filter Options", color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
            if (showFilters) {
                Spacer(Modifier.height(12.dp))
                androidx.compose.foundation.rememberScrollState().let { scrollState ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .padding(16.dp)
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilterDropdown(
                            label = "Asset Class",
                            selectedOption = selectedAssetClass,
                            options = assetClassOptions,
                            onOptionSelected = { selectedAssetClass = it }
                        )
                        FilterDropdown(
                            label = "Strategy",
                            selectedOption = selectedStrategy,
                            options = strategyOptions,
                            onOptionSelected = { selectedStrategy = it }
                        )
                        FilterDropdown(
                            label = "Outcome",
                            selectedOption = selectedOutcome,
                            options = outcomeOptions,
                            onOptionSelected = { selectedOutcome = it }
                        )
                        FilterDropdown(
                            label = "Market Condition",
                            selectedOption = selectedMarketCondition,
                            options = marketConditionOptions,
                            onOptionSelected = { selectedMarketCondition = it }
                        )
                        FilterDropdown(
                            label = "Date Range",
                            selectedOption = selectedDateRange,
                            options = dateRangeOptions,
                            onOptionSelected = { selectedDateRange = it }
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    selectedAssetClass = "All"
                                    selectedStrategy = "All"
                                    selectedOutcome = "All"
                                    selectedMarketCondition = "All"
                                    selectedDateRange = "All"
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Reset")
                            }
                            Button(
                                onClick = { showFilters = false },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Apply")
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            when (uiState) {
                is TradeHistoryUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                        Text("Loading trades...", modifier = Modifier.padding(top = 60.dp))
                    }
                }
                is TradeHistoryUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text((uiState as TradeHistoryUiState.Error).message, color = MaterialTheme.colorScheme.error)
                    }
                }
                is TradeHistoryUiState.Success -> {
                    if (filteredTrades.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No trades match your criteria.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredTrades, key = { it.first }) { (tradeId, trade) ->
                                TradeCard(tradeId = tradeId, trade = trade, onClick = {
                                    selectedTradeIdForDialog = tradeId
                                    showDetailDialog = true
                                })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar(query: TextFieldValue, onQueryChange: (TextFieldValue) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Search Trades (e.g., EUR/USD, Win, Scalping)") },
        leadingIcon = {
            Icon(Icons.Filled.Search, contentDescription = "Search Icon")
        },
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            disabledContainerColor = MaterialTheme.colorScheme.surface // Adding for consistency
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDropdown(
    label: String,
    selectedOption: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun TradeCard(tradeId: String, trade: Trade, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Outcome Indicator
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(
                        when (trade.outcome) {
                            "Win" -> Color(0xFF26A69A)
                            "Loss" -> Color(0xFFEF5350)
                            "Breakeven" -> MaterialTheme.colorScheme.outline
                            else -> MaterialTheme.colorScheme.outline
                        }
                    )
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Trade Info Column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${trade.specificAsset ?: "N/A"} - " + (trade.tradeDate?.toDate()?.let { java.text.SimpleDateFormat("yyyy-MM-dd").format(it) } ?: "N/A"),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = trade.strategyUsed ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Balance Change Icon (Optional)
            val pnl = trade.pnlAmount ?: 0.0
            Spacer(modifier = Modifier.width(12.dp))
            Icon(
                imageVector = if (pnl >= 0) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                contentDescription = if (pnl >= 0) "Positive Balance Change" else "Negative Balance Change",
                tint = if (pnl >= 0) Color(0xFF26A69A) else Color(0xFFEF5350),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun TradeScreenshotDisplay(screenshotUri: String?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (screenshotUri != null && screenshotUri.isNotBlank()) {
                Image(
                    painter = rememberAsyncImagePainter(screenshotUri),
                    contentDescription = "Trade Screenshot",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.MoreVert, // Placeholder icon, replace with image loader if needed
                    contentDescription = "No Screenshot Available",
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TradeInfoSection(details: TradeDetailData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            InfoRow("Asset Pair:", details.assetPair)
            InfoRow("Asset Class:", details.assetClass)
            InfoRow("Date & Time:", details.date)
            InfoRow("Strategy:", details.strategy)
            InfoRow("Market Condition:", details.marketCondition)
            InfoRow("Position Type:", details.positionType, valueColor = if(details.positionType.equals("Long", true)) Color(0xFF26A69A) else Color(0xFFEF5350))
            Divider(modifier = Modifier.padding(vertical = 6.dp))
            Text("Financials", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            InfoRow("Entry Price:", formatCurrencyDetail(details.entryPrice))
            InfoRow("Stop-Loss:", formatCurrencyDetail(details.stopLossPrice))
            InfoRow("Take-Profit:", formatCurrencyDetail(details.takeProfitPrice, defaultText = "Not Set"))
            InfoRow("Entry Amount (USD):", formatCurrencyDetail(details.entryAmountUSD))
            details.leverage?.let { InfoRow("Leverage:", "${it}x") }
            InfoRow("P&L Amount:", formatCurrencyDetail(details.pnlAmount), valueColor = if((details.pnlAmount ?: 0.0) >= 0) Color(0xFF26A69A) else Color(0xFFEF5350))
            InfoRow("P&L Percentage:", formatPercentage(details.percentagePnl), valueColor = if((details.percentagePnl ?: 0.0) >= 0) Color(0xFF26A69A) else Color(0xFFEF5350))
            InfoRow("Outcome:", details.outcome, valueColor = when(details.outcome.lowercase()) {
                "win" -> Color(0xFF26A69A)
                "loss" -> Color(0xFFEF5350)
                else -> MaterialTheme.colorScheme.onSurface
            })
        }
    }
}

@Composable
private fun RRMetricsSection(entry: Double?, sl: Double?, tp: Double?, pnl: Double?, positionType: String) {
    val riskAmount = if (entry != null && sl != null) kotlin.math.abs(entry - sl) else null
    val plannedRR = if (tp != null && entry != null && riskAmount != null && riskAmount != 0.0) {
        val rewardAmount = kotlin.math.abs(tp - entry)
        String.format("%.2f : 1", rewardAmount / riskAmount)
    } else { "N/A" }
    val actualRR = if (pnl != null && riskAmount != null && riskAmount != 0.0) {
        String.format("%.2f : 1", (kotlin.math.abs(pnl) / riskAmount) * (if (pnl >= 0) 1 else -1))
    } else { "N/A" }
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            InfoRow("Planned R:R Ratio:", plannedRR)
            InfoRow("Actual R:R Achieved:", actualRR)
        }
    }
}

@Composable
private fun PerformanceDetailsSection(details: TradeDetailData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            InfoRow("Percentage P&L:", formatPercentage(details.percentagePnl), valueColor = if((details.percentagePnl ?: 0.0) >= 0) Color(0xFF26A69A) else Color(0xFFEF5350))
        }
    }
}

@Composable
private fun TimestampsSection(details: TradeDetailData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            InfoRow("Entry Timestamp (Client):", details.entryClientTimestamp ?: "N/A")
            InfoRow("Exit Timestamp (Approx.):", details.exitTimestamp ?: "N/A")
        }
    }
}

@Composable
private fun NotesSection(title: String, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 6.dp))
            Text(
                text = content.ifBlank { "No notes provided." },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TagsSection(tags: List<String>) {
    if (tags.isNotEmpty()) {
        Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Text("Tags", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                tags.take(5).forEach { tag ->
                    Text(
                        text = "#$tag",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
                if (tags.size > 5) {
                    Text("...", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun TradeDetailDialog(
    tradeDetail: TradeDetailData,
    onDismissRequest: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Trade?") },
            text = { Text("Are you sure you want to delete this trade entry? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDelete?.invoke()
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Trade Details: ${tradeDetail.assetPair}",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { TradeScreenshotDisplay(tradeDetail.screenshotUri) }
                    item { SectionTitle("Trade Overview") }
                    item { TradeInfoSection(tradeDetail) }
                    item { SectionTitle("Performance Metrics") }
                    item {
                        RRMetricsSection(
                            entry = tradeDetail.entryPrice,
                            sl = tradeDetail.stopLossPrice,
                            tp = tradeDetail.takeProfitPrice,
                            pnl = tradeDetail.pnlAmount,
                            positionType = tradeDetail.positionType
                        )
                    }
                    item { PerformanceDetailsSection(tradeDetail) }
                    item { SectionTitle("Trade Journal") }
                    item { NotesSection("Pre-Trade Rationale / Setup", tradeDetail.preTradeRationale) }
                    item { NotesSection("Execution Notes", tradeDetail.executionNotes) }
                    item { NotesSection("Post-Trade Review / Lessons Learned", tradeDetail.postTradeReview) }
                    item { SectionTitle("Additional Info") }
                    item { TimestampsSection(tradeDetail) }
                    item { TagsSection(tradeDetail.tags) }
                    item { Spacer(modifier = Modifier.height(72.dp)) }
                }
                Row(
                    modifier = Modifier.align(Alignment.End).padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    onEdit?.let {
                        Button(onClick = { onEdit() }) { Text("Edit") }
                    }
                    onDelete?.let {
                        Button(
                            onClick = { showDeleteConfirm = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) { Text("Delete", color = MaterialTheme.colorScheme.onError) }
                    }
                    Button(onClick = onDismissRequest) { Text("Close") }
                }
            }
        }
    }
}

// --- Previews ---
@Preview(showBackground = true, name = "Trade History Screen - Light")
@Composable
fun TradeHistoryScreenPreviewLight() {
    val context = androidx.compose.ui.platform.LocalContext.current
    ProfitPathTheme(darkTheme = false) {
        TradeHistoryScreen(navController = NavHostController(context))
    }
}

@Preview(showBackground = true, name = "Trade History Screen - Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TradeHistoryScreenPreviewDark() {
    val context = androidx.compose.ui.platform.LocalContext.current
    ProfitPathTheme(darkTheme = true) {
        TradeHistoryScreen(navController = NavHostController(context))
    }
}

@Preview(showBackground = true)
@Composable
fun TradeCardWinPreview() {
    ProfitPathTheme {
        TradeCard(
            tradeId = "1",
            trade = Trade(
                specificAsset = "EUR/USD",
                outcome = "Win",
                pnlAmount = 150.0,
                strategyUsed = "Scalping"
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TradeCardLossPreview() {
    ProfitPathTheme {
        TradeCard(
            tradeId = "2",
            trade = Trade(
                specificAsset = "GBP/JPY",
                outcome = "Loss",
                pnlAmount = -75.5,
                strategyUsed = "Trend Following"
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TradeCardBreakEvenNoBalancePreview() {
    ProfitPathTheme {
        TradeCard(
            tradeId = "3",
            trade = Trade(
                specificAsset = "AUD/CAD",
                outcome = "Breakeven",
                pnlAmount = 0.0,
                strategyUsed = "Range Trading"
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Trade Detail Dialog - Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TradeDetailDialogPreview() {
    ProfitPathTheme(darkTheme = true) {
        // Directly show the dialog with a sample item for preview
         Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)), contentAlignment = Alignment.Center){ // Simulate dialog overlay
            TradeDetailDialog(
                tradeDetail = TradeDetailData(
                    id = "1",
                    assetPair = "EUR/USD",
                    assetClass = "Forex",
                    date = "2023-10-27",
                    strategy = "Scalping",
                    marketCondition = "Trending",
                    positionType = "Long",
                    entryPrice = 1.2345,
                    stopLossPrice = 1.2300,
                    takeProfitPrice = 1.2400,
                    outcome = "Win",
                    pnlAmount = 150.0,
                    entryAmountUSD = 1000.0,
                    preTradeRationale = "Price broke above resistance.",
                    executionNotes = "Slight slippage on entry.",
                    postTradeReview = "Should have held longer.",
                    tags = listOf("Breakout", "EURUSD"),
                    screenshotUri = "content://media/external/images/media/12345",
                    percentagePnl = 5.0,
                    exitTimestamp = "2023-10-27 10:30",
                    entryClientTimestamp = "2023-10-27 09:00",
                    balanceUpdated = true,
                    leverage = 50.0
                ),
                onDismissRequest = {}
            )
        }
    }
} 