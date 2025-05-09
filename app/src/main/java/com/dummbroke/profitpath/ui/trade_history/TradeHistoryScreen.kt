package com.dummbroke.profitpath.ui.trade_history

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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dummbroke.profitpath.ui.theme.ProfitPathTheme

// --- Data Class for Trade History Item ---
data class TradeHistoryItem(
    val id: String,
    val pair: String,
    val date: String,
    val outcome: TradeOutcome, // Win, Loss, BreakEven
    val descriptionSnippet: String,
    val balanceChange: Double? = null, // Optional: positive for profit, negative for loss
    val strategy: String // Added strategy for filtering
)

enum class TradeOutcome {
    WIN, LOSS, BREAK_EVEN
}

const val DEFAULT_FILTER_ALL = "All"
const val DEFAULT_FILTER_ANY = "Any"

// --- Composable Functions ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradeHistoryScreen(onTradeClick: (tradeId: String) -> Unit) {
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var showFilters by remember { mutableStateOf(false) }

    // Filter States
    var selectedDateRange by remember { mutableStateOf(DEFAULT_FILTER_ALL) }
    var selectedStrategy by remember { mutableStateOf(DEFAULT_FILTER_ALL) }
    var selectedWinLoss by remember { mutableStateOf(DEFAULT_FILTER_ALL) } // "All", "Win", "Loss", "Break-Even"
    var selectedBalanceImpact by remember { mutableStateOf(DEFAULT_FILTER_ANY) } // "Any", "Positive", "Negative"

    // Dummy data for preview
    val allTrades = remember { listOf(
        TradeHistoryItem("1", "EUR/USD", "2024-07-29", TradeOutcome.WIN, "Good entry based on RSI divergence and trendline support...", 150.75, "Scalping"),
        TradeHistoryItem("2", "BTC/USD", "2024-07-28", TradeOutcome.LOSS, "Stop loss hit due to unexpected news spike. Market volatility was high.", -75.20, "Swing Trading"),
        TradeHistoryItem("3", "AAPL", "2024-07-27", TradeOutcome.BREAK_EVEN, "Exited at entry, setup didn\'t play out as expected.", strategy = "Position Trading"),
        TradeHistoryItem("4", "GBP/JPY", "2024-07-26", TradeOutcome.WIN, "Scalped a quick 20 pips on London open volatility.", 55.00, "Scalping"),
        TradeHistoryItem("5", "ETH/USD", "2024-07-25", TradeOutcome.LOSS, "Market reversal, took a small loss.", -30.00, "Breakout")
    )}

    var filteredTrades by remember { mutableStateOf(allTrades) }

    fun applyFilters() {
        filteredTrades = allTrades.filter { trade ->
            val matchesSearch = searchQuery.text.isBlank() ||
                    trade.pair.contains(searchQuery.text, ignoreCase = true) ||
                    trade.descriptionSnippet.contains(searchQuery.text, ignoreCase = true)

            val matchesDateRange = when (selectedDateRange) {
                // TODO: Implement actual date range logic
                "Today" -> false // Placeholder
                "Last 7 Days" -> false // Placeholder
                "Last 30 Days" -> false // Placeholder
                else -> true // "All" or "Custom" (not yet handled for custom)
            }

            val matchesStrategy = selectedStrategy == DEFAULT_FILTER_ALL || trade.strategy == selectedStrategy

            val matchesWinLoss = when (selectedWinLoss) {
                "Win" -> trade.outcome == TradeOutcome.WIN
                "Loss" -> trade.outcome == TradeOutcome.LOSS
                "Break-Even" -> trade.outcome == TradeOutcome.BREAK_EVEN
                else -> true // "All"
            }

            val matchesBalanceImpact = when (selectedBalanceImpact) {
                "Positive" -> (trade.balanceChange ?: 0.0) > 0
                "Negative" -> (trade.balanceChange ?: 0.0) < 0
                else -> true // "Any"
            }

            matchesSearch && matchesDateRange && matchesStrategy && matchesWinLoss && matchesBalanceImpact
        }
    }

    // Apply filters whenever searchQuery or filter selections change (via Apply button)
    // Initial application of filters
    LaunchedEffect(Unit) { // Apply once on initial composition
        applyFilters()
    }

    val dateRangeOptions = listOf(DEFAULT_FILTER_ALL, "Today", "Last 7 Days", "Last 30 Days", "Custom")
    val strategyOptions = remember { listOf(DEFAULT_FILTER_ALL) + allTrades.map { it.strategy }.distinct().sorted() }
    val winLossOptions = listOf(DEFAULT_FILTER_ALL, "Win", "Loss", "Break-Even")
    val balanceImpactOptions = listOf(DEFAULT_FILTER_ANY, "Positive", "Negative")

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
            SearchBar(searchQuery) { 
                searchQuery = it 
                // applyFilters() // Optionally apply filters on search query change immediately, or wait for Apply button
            }
            Spacer(modifier = Modifier.height(8.dp))
            FilterSection(
                showFilters = showFilters,
                onShowFiltersToggle = { showFilters = !showFilters },
                selectedDateRange = selectedDateRange,
                dateRangeOptions = dateRangeOptions,
                onDateRangeSelected = { selectedDateRange = it },
                selectedStrategy = selectedStrategy,
                strategyOptions = strategyOptions,
                onStrategySelected = { selectedStrategy = it },
                selectedWinLoss = selectedWinLoss,
                winLossOptions = winLossOptions,
                onWinLossSelected = { selectedWinLoss = it },
                selectedBalanceImpact = selectedBalanceImpact,
                balanceImpactOptions = balanceImpactOptions,
                onBalanceImpactSelected = { selectedBalanceImpact = it },
                onResetFilters = {
                    selectedDateRange = DEFAULT_FILTER_ALL
                    selectedStrategy = DEFAULT_FILTER_ALL
                    selectedWinLoss = DEFAULT_FILTER_ALL
                    selectedBalanceImpact = DEFAULT_FILTER_ANY
                    searchQuery = TextFieldValue("") // Also reset search query
                    applyFilters()
                },
                onApplyFilters = {
                    applyFilters()
                    showFilters = false // Optionally close filter section after applying
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
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
                    items(filteredTrades, key = { it.id }) { trade ->
                        TradeCard(tradeItem = trade, onClick = { onTradeClick(trade.id) })
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
        label = { Text("Search Trades (e.g., EUR/USD, keyword)") },
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
fun FilterSection(
    showFilters: Boolean,
    onShowFiltersToggle: () -> Unit,
    selectedDateRange: String,
    dateRangeOptions: List<String>,
    onDateRangeSelected: (String) -> Unit,
    selectedStrategy: String,
    strategyOptions: List<String>,
    onStrategySelected: (String) -> Unit,
    selectedWinLoss: String,
    winLossOptions: List<String>,
    onWinLossSelected: (String) -> Unit,
    selectedBalanceImpact: String,
    balanceImpactOptions: List<String>,
    onBalanceImpactSelected: (String) -> Unit,
    onResetFilters: () -> Unit,
    onApplyFilters: () -> Unit
) {
    Column {
        Button(
            onClick = onShowFiltersToggle,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Icon(Icons.Filled.FilterList, contentDescription = "Filter Options", tint = MaterialTheme.colorScheme.onSecondaryContainer)
            Spacer(Modifier.width(8.dp))
            Text("Filter Options", color = MaterialTheme.colorScheme.onSecondaryContainer)
        }

        if (showFilters) {
            Spacer(Modifier.height(12.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterDropdown(
                    label = "Date Range",
                    selectedOption = selectedDateRange,
                    options = dateRangeOptions,
                    onOptionSelected = onDateRangeSelected
                )
                FilterDropdown(
                    label = "Strategy",
                    selectedOption = selectedStrategy,
                    options = strategyOptions,
                    onOptionSelected = onStrategySelected
                )
                FilterDropdown(
                    label = "Win/Loss/Break-Even",
                    selectedOption = selectedWinLoss,
                    options = winLossOptions,
                    onOptionSelected = onWinLossSelected
                )
                FilterDropdown(
                    label = "Balance Impact",
                    selectedOption = selectedBalanceImpact,
                    options = balanceImpactOptions,
                    onOptionSelected = onBalanceImpactSelected
                )
                Spacer(Modifier.height(8.dp)) // Space before buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onResetFilters,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Reset")
                    }
                    Button(
                        onClick = onApplyFilters,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Apply")
                    }
                }
            }
        }
    }
}

@Composable
fun TradeCard(tradeItem: TradeHistoryItem, onClick: () -> Unit) {
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
                        when (tradeItem.outcome) {
                            TradeOutcome.WIN -> Color(0xFF26A69A) // TradingView Green
                            TradeOutcome.LOSS -> Color(0xFFEF5350) // TradingView Red
                            TradeOutcome.BREAK_EVEN -> MaterialTheme.colorScheme.outline
                        }
                    )
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Trade Info Column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${tradeItem.pair} - ${tradeItem.date}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tradeItem.descriptionSnippet,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Balance Change Icon (Optional)
            tradeItem.balanceChange?.let { balance ->
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    imageVector = if (balance >= 0) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                    contentDescription = if (balance >= 0) "Positive Balance Change" else "Negative Balance Change",
                    tint = if (balance >= 0) Color(0xFF26A69A) else Color(0xFFEF5350),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// --- Previews ---
@Preview(showBackground = true, name = "Trade History Screen - Light")
@Composable
fun TradeHistoryScreenPreviewLight() {
    ProfitPathTheme(darkTheme = false) {
        TradeHistoryScreen(onTradeClick = {})
    }
}

@Preview(showBackground = true, name = "Trade History Screen - Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TradeHistoryScreenPreviewDark() {
    ProfitPathTheme(darkTheme = true) {
        TradeHistoryScreen(onTradeClick = {})
    }
}

@Preview(showBackground = true)
@Composable
fun FilterSectionPreviewExpanded() {
    ProfitPathTheme {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.padding(16.dp)) {
            // Dummy state for preview
            var selectedDateRange by remember { mutableStateOf(DEFAULT_FILTER_ALL) }
            var selectedStrategy by remember { mutableStateOf(DEFAULT_FILTER_ALL) }
            var selectedWinLoss by remember { mutableStateOf(DEFAULT_FILTER_ALL) }
            var selectedBalanceImpact by remember { mutableStateOf(DEFAULT_FILTER_ANY) }

            FilterSection(
                showFilters = true,
                onShowFiltersToggle = {},
                selectedDateRange = selectedDateRange,
                dateRangeOptions = listOf(DEFAULT_FILTER_ALL, "Today", "Last 7 Days"),
                onDateRangeSelected = {selectedDateRange = it},
                selectedStrategy = selectedStrategy,
                strategyOptions = listOf(DEFAULT_FILTER_ALL, "Scalping", "Swing Trading"),
                onStrategySelected = {selectedStrategy = it},
                selectedWinLoss = selectedWinLoss,
                winLossOptions = listOf(DEFAULT_FILTER_ALL, "Win", "Loss"),
                onWinLossSelected = {selectedWinLoss = it},
                selectedBalanceImpact = selectedBalanceImpact,
                balanceImpactOptions = listOf(DEFAULT_FILTER_ANY, "Positive", "Negative"),
                onBalanceImpactSelected = {selectedBalanceImpact = it},
                onResetFilters = { // Simulate reset for preview
                    selectedDateRange = DEFAULT_FILTER_ALL
                    selectedStrategy = DEFAULT_FILTER_ALL
                    selectedWinLoss = DEFAULT_FILTER_ALL
                    selectedBalanceImpact = DEFAULT_FILTER_ANY
                },
                onApplyFilters = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TradeCardWinPreview() {
    ProfitPathTheme {
        TradeCard(
            tradeItem = TradeHistoryItem("1", "EUR/USD", "2024-07-29", TradeOutcome.WIN, "Good entry based on RSI.", 150.0, "Scalping"),
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TradeCardLossPreview() {
    ProfitPathTheme {
        TradeCard(
            tradeItem = TradeHistoryItem("2", "BTC/USD", "2024-07-28", TradeOutcome.LOSS, "Stop loss hit due to news.", -75.0, "Swing Trading"),
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TradeCardBreakEvenNoBalancePreview() {
    ProfitPathTheme {
        TradeCard(
            tradeItem = TradeHistoryItem("3", "AAPL", "2024-07-27", TradeOutcome.BREAK_EVEN, "Exited at entry, setup didn\'t play out as expected.", strategy = "News Trading"),
            onClick = {}
        )
    }
} 