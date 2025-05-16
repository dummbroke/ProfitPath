package com.dummbroke.profitpath.ui.performance

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dummbroke.profitpath.ui.theme.ProfitPathTheme
import java.text.DecimalFormat
import androidx.lifecycle.viewmodel.compose.viewModel

// --- Data Classes for Performance Metrics ---
data class PerformanceOverview(
    val currentBalance: Double,
    val netPnL: Double,
    val pnlPercentage: Double,
    val totalTrades: Int,
    val winRate: Double, // 0.0 to 1.0
    val bestTrade: Double,
    val worstTrade: Double,
    val profitFactor: Double,
    val expectancy: Double, // Monetary value per trade
    val avgWin: Double,
    val avgLoss: Double,
    val avgHoldingTime: String, // e.g., "4h 30m"
    val longestWinStreak: Int,
    val longestLossStreak: Int,
    val maxDrawdown: Double, // Percentage or monetary
    val bestDayPnl: Double,
    val worstDayPnl: Double
)

data class TimePeriodSummary(
    val period: String, // e.g., "July 2024", "Week 30"
    val tradesCount: Int,
    val netGainLoss: Double
)

data class PerformanceByStrategy(
    val strategy: String,
    val totalTrades: Int,
    val winCount: Int,
    val lossCount: Int,
    val netPnL: Double
)

// --- Helper Function ---
fun formatCurrency(value: Double, withSign: Boolean = false): String {
    val plusSign = if (value > 0 && withSign) "+" else ""
    return "$plusSign${DecimalFormat("#,##0.00").format(value)}"
}

// --- Composable Functions ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceScreen(
    performanceViewModel: PerformanceViewModel = viewModel()
) {
    val uiState by performanceViewModel.uiState.collectAsState()
    val strategies by performanceViewModel.strategies.collectAsState()
    // Default strategy options with descriptions
    val defaultStrategies = listOf(
        "Trend Following",
        "Scalping",
        "Day Trading",
        "Swing Trading",
        "Position Trading",
        "Momentum Trading",
        "Arbitrage Trading",
        "Algorithmic Trading",
        "Range Trading",
        "News-Based Trading"
    )
    val selectedStrategy by performanceViewModel.selectedStrategy.collectAsState()
    val selectedDateRange by performanceViewModel.selectedDateRange.collectAsState()
    val dateRangeOptions = performanceViewModel.getDateRangeOptions()
    // Always use these 17 strategies for the dropdown (plus 'All')
    val strategyFilterOptions = listOf(
        "All",
        "Bullish Trading",
        "Bearish Trading",
        "Trend Trading",
        "Momentum Trading",
        "Mean Reversion",
        "Breakout Trading",
        "Reversal Trading",
        "Range Trading",
        "Gap Trading",
        "Price Action Trading",
        "News Trading",
        "Earnings Trading",
        "Merger & Acquisition Trading",
        "Central Bank Policy Trading",
        "Algorithmic Trading",
        "Arbitrage Trading",
        "High-Frequency Trading (HFT)",
        "Pairs Trading"
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        when (uiState) {
            is PerformanceUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is PerformanceUiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = (uiState as PerformanceUiState.Error).message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            is PerformanceUiState.Success -> {
                val performanceData = (uiState as PerformanceUiState.Success).overview
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        PerformanceFiltersAndExport(
                            selectedDateRange = dateRangeOptions.firstOrNull { it.second == selectedDateRange }?.first ?: "Overall",
                            dateRangeOptions = dateRangeOptions.map { it.first },
                            onDateRangeSelected = { label ->
                                val range = dateRangeOptions.firstOrNull { it.first == label }?.second ?: DateRange.Overall
                                performanceViewModel.updateDateRange(range)
                            },
                            selectedStrategyFilter = selectedStrategy,
                            strategyFilterOptions = strategyFilterOptions,
                            onStrategyFilterSelected = { performanceViewModel.updateStrategy(it) },
                            onExportClick = { /* TODO: Implement Export */ }
                        )
                    }
                    item { BalanceAndPnlOverviewCard(performanceData) }
                    item { KeyStatsGrid(performanceData) }
                    item { AverageMetricsCard(performanceData) }
                    item { StreaksAndDrawdownCard(performanceData) }
                    // TODO: Add monthly summary, strategy performance, and other analytics using real data
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceFiltersAndExport(
    selectedDateRange: String,
    dateRangeOptions: List<String>,
    onDateRangeSelected: (String) -> Unit,
    selectedStrategyFilter: String,
    strategyFilterOptions: List<String>,
    onStrategyFilterSelected: (String) -> Unit,
    onExportClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterDropdownPerformance(label = "Date Range", selectedOption = selectedDateRange, options = dateRangeOptions, onOptionSelected = onDateRangeSelected, modifier = Modifier.weight(1f))
            FilterDropdownPerformance(label = "Strategy", selectedOption = selectedStrategyFilter, options = strategyFilterOptions, onOptionSelected = onStrategyFilterSelected, modifier = Modifier.weight(1f))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDropdownPerformance(
    label: String,
    selectedOption: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, fontSize = 12.sp) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
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
fun BalanceAndPnlOverviewCard(data: PerformanceOverview) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Account Overview", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("Current Balance", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(formatCurrency(data.currentBalance), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Net P&L (${data.pnlPercentage.toInt()}%) ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = formatCurrency(data.netPnL, withSign = true),
                        style = MaterialTheme.typography.titleLarge,
                        color = if (data.netPnL >= 0) Color(0xFF26A69A) else Color(0xFFEF5350)
                    )
                }
            }
        }
    }
}

@Composable
fun KeyStatsGrid(data: PerformanceOverview) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Key Statistics", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCardPerformance("Total Trades", data.totalTrades.toString(), Modifier.weight(1f))
            StatCardPerformance("Win Rate", "${(data.winRate * 100).toInt()}%", Modifier.weight(1f), isPercentage = true, percentage = data.winRate.toFloat())
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCardPerformance("Best Trade", formatCurrency(data.bestTrade, true), Modifier.weight(1f), valueColor = Color(0xFF26A69A))
            StatCardPerformance("Worst Trade", formatCurrency(data.worstTrade, true), Modifier.weight(1f), valueColor = Color(0xFFEF5350))
        }
         Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCardPerformance("Profit Factor", String.format("%.2f", data.profitFactor), Modifier.weight(1f))
            StatCardPerformance("Expectancy", formatCurrency(data.expectancy), Modifier.weight(1f))
        }
    }
}

@Composable
fun StatCardPerformance(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    isPercentage: Boolean = false,
    percentage: Float = 0f
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = valueColor)
            if (isPercentage) {
                LinearProgressIndicator(
                    progress = percentage,
                    modifier = Modifier.height(6.dp).fillMaxWidth().clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFF26A69A),
                    trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            }
        }
    }
}

@Composable
fun AverageMetricsCard(data: PerformanceOverview) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Average Metrics", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            MetricRow("Avg. Win", formatCurrency(data.avgWin, true), Color(0xFF26A69A))
            MetricRow("Avg. Loss", formatCurrency(data.avgLoss, true), Color(0xFFEF5350))
            MetricRow("Avg. Holding Time", data.avgHoldingTime)
        }
    }
}

@Composable
fun StreaksAndDrawdownCard(data: PerformanceOverview) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Streaks & Drawdown", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            MetricRow("Longest Win Streak", "${data.longestWinStreak} Trades")
            MetricRow("Longest Loss Streak", "${data.longestLossStreak} Trades")
            MetricRow("Max Drawdown", formatCurrency(data.maxDrawdown), Color(0xFFEF5350))
        }
    }
}

@Composable
fun MetricRow(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}

@Composable
fun PerformanceTrendsSection(monthlySummary: List<TimePeriodSummary>, bestDayPnl: Double, worstDayPnl: Double) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Performance Trends", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Monthly Summary", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                monthlySummary.forEach {
                    SummaryRow(it.period, "Trades: ${it.tradesCount}", "P&L: ${formatCurrency(it.netGainLoss, true)}", if(it.netGainLoss >= 0) Color(0xFF26A69A) else Color(0xFFEF5350))
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Text("Daily Extremes", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                MetricRow("Best Day P&L", formatCurrency(bestDayPnl, true), Color(0xFF26A69A))
                MetricRow("Worst Day P&L", formatCurrency(worstDayPnl, true), Color(0xFFEF5350))
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, detail1: String, detail2: String, detail2Color: Color) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(0.4f))
        Text(detail1, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(0.3f))
        Text(detail2, style = MaterialTheme.typography.bodyMedium, color = detail2Color, textAlign = TextAlign.End, modifier = Modifier.weight(0.3f))
    }
}

@Composable
fun PerformanceByStrategyTable(performance: List<PerformanceByStrategy>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Performance by Strategy", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header Row
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Strategy", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.35f))
                    Text("Trades", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.weight(0.2f))
                    Text("Win %", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.weight(0.2f))
                    Text("Net P&L", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(0.25f))
                }
                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

                performance.forEach {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(it.strategy, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(0.35f))
                        Text(it.totalTrades.toString(), style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, modifier = Modifier.weight(0.2f))
                        val winRate = if (it.totalTrades > 0) (it.winCount.toDouble() / it.totalTrades * 100).toInt() else 0
                        Text("$winRate%", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, modifier = Modifier.weight(0.2f))
                        Text(
                            formatCurrency(it.netPnL, true),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (it.netPnL >= 0) Color(0xFF26A69A) else Color(0xFFEF5350),
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(0.25f)
                        )
                    }
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
fun IncomePercentageAnalysisPlaceholder() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Income Source Analysis", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Card(
            modifier = Modifier.fillMaxWidth().height(200.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                // Donut Chart Placeholder - using Canvas for a simple static representation
                DonutChartStatic(listOf(0.4f, 0.3f, 0.2f, 0.1f), listOf(Color.Blue, Color.Green, Color.Yellow, Color.Magenta))
                Text("Income Distribution (e.g., by Strategy/Asset)", textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
         // Legend could go here
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            LegendItem("Strategy A (40%)", Color.Blue)
            LegendItem("Strategy B (30%)", Color.Green)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            LegendItem("Strategy C (20%)", Color.Yellow)
            LegendItem("Strategy D (10%)", Color.Magenta)
        }
    }
}

@Composable
fun DonutChartStatic(proportions: List<Float>, colors: List<Color>) {
    Canvas(modifier = Modifier.size(150.dp)) { // Fixed size for simplicity
        val total = proportions.sum()
        if (total == 0f) return@Canvas

        var startAngle = -90f
        val strokeWidth = 30.dp.toPx()

        proportions.forEachIndexed { index, proportion ->
            val sweepAngle = (proportion / total) * 360f
            drawArc(
                color = colors.getOrElse(index) { Color.Gray },
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth),
                size = Size(size.width - strokeWidth, size.height - strokeWidth),
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
            )
            startAngle += sweepAngle
        }
    }
}

@Composable
fun LegendItem(text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
        Text(text, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}


// --- Previews ---
@Preview(showBackground = true, name = "Performance Screen Light")
@Composable
fun PerformanceScreenPreviewLight() {
    ProfitPathTheme(darkTheme = false) {
        PerformanceScreen()
    }
}

@Preview(showBackground = true, name = "Performance Screen Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PerformanceScreenPreviewDark() {
    ProfitPathTheme(darkTheme = true) {
        PerformanceScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun BalanceAndPnlOverviewCardPreview() {
    ProfitPathTheme {
        BalanceAndPnlOverviewCard(PerformanceOverview(10000.0, 1500.0, 15.0, 50, 0.7, 200.0, -100.0, 2.5, 30.0, 100.0, -50.0, "4h", 5, 2, -500.0, 150.0, -80.0))
    }
}

@Preview(showBackground = true)
@Composable
fun KeyStatsGridPreview() {
    ProfitPathTheme {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.padding(16.dp)) {
            KeyStatsGrid(PerformanceOverview(10000.0, 1500.0, 15.0, 50, 0.7, 200.0, -100.0, 2.5, 30.0, 100.0, -50.0, "4h", 5, 2, -500.0, 150.0, -80.0))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PerformanceByStrategyTablePreview() {
    ProfitPathTheme {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.padding(16.dp)) {
            PerformanceByStrategyTable(listOf(
                PerformanceByStrategy("Scalping", 70, 50, 20, 1500.0),
                PerformanceByStrategy("Swing Trading", 50, 30, 20, 800.0)
            ))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun IncomePercentageAnalysisPlaceholderPreview() {
    ProfitPathTheme {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.padding(16.dp)) {
            IncomePercentageAnalysisPlaceholder()
        }
    }
} 