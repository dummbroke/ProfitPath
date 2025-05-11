package com.dummbroke.profitpath.ui.trade_detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource // For placeholder image
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dummbroke.profitpath.R // Assuming a placeholder image like R.drawable.placeholder_image
import com.dummbroke.profitpath.ui.theme.ProfitPathTheme

// --- Data Class for Detailed Trade View (mirroring what's captured in TradeEntry) ---
data class TradeDetailData(
    val id: String,
    val assetPair: String,
    val date: String,
    val strategy: String,
    val marketCondition: String,
    val positionType: String, // "Long" or "Short"
    val entryPrice: Double?,
    val stopLossPrice: Double?,
    val takeProfitPrice: Double?,
    val outcome: String, // "Win", "Loss", "Break-Even"
    val pnlAmount: Double?,
    val balanceImpact: Double?,
    val preTradeRationale: String,
    val executionNotes: String,
    val postTradeReview: String,
    val tags: List<String>,
    val screenshotUri: String? // Path or URI to the screenshot
)

// --- Helper to format currency ---
fun formatCurrencyDetail(value: Double?, defaultText: String = "N/A"): String {
    return value?.let { String.format("$%.2f", it) } ?: defaultText
}

// --- Composable Functions ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradeDetailScreen(tradeId: String? = null) {
    var tradeDetail by remember { mutableStateOf<TradeDetailData?>(null) }

    // Simulate fetching trade data based on tradeId
    LaunchedEffect(tradeId) {
        if (tradeId != null) {
            // In a real app, you would fetch this from a ViewModel/Repository
            tradeDetail = TradeDetailData(
                id = tradeId,
                assetPair = "EUR/USD",
                date = "2024-07-30",
                strategy = "Scalping",
                marketCondition = "Ranging",
                positionType = "Long",
                entryPrice = 1.0850,
                stopLossPrice = 1.0830,
                takeProfitPrice = 1.0890,
                outcome = "Win",
                pnlAmount = 80.00,
                balanceImpact = 80.00,
                preTradeRationale = "Price respected the support level at 1.0840. RSI showed bullish divergence. Entered on a pullback after a small consolidation.",
                executionNotes = "Entry was smooth. Market moved quickly in my favor. Didn\'t get a chance to move SL to break-even as TP was hit first.",
                postTradeReview = "Good trade according to plan. Could have aimed for a slightly higher TP based on daily resistance, but happy with the result. Discipline was key.",
                tags = listOf("RSI Divergence", "Support Level", "Quick Win", "Good Discipline"),
                screenshotUri = null // Placeholder, replace with actual URI or path
            )
        } else {
            // Handle case where no ID is provided, or show an error/empty state
            tradeDetail = null
        }
    }

    Scaffold(
        // TopAppBar is handled by AppNavigation, which should show trade date/pair and back arrow
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            if (tradeDetail != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FloatingActionButton(
                        onClick = { /* TODO: Handle Delete */ },
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ) {
                        Icon(Icons.Filled.Delete, "Delete Trade")
                    }
                    FloatingActionButton(
                        onClick = { /* TODO: Handle Edit -> Navigate to TradeEntry with this data */ }
                    ) {
                        Icon(Icons.Filled.Edit, "Edit Trade")
                    }
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        if (tradeDetail == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                if (tradeId == null) {
                    Text("No trade ID provided.", style = MaterialTheme.typography.bodyLarge)
                } else {
                    CircularProgressIndicator()
                    Text("Loading trade details...", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 70.dp))
                }
            }
            return@Scaffold
        }

        tradeDetail?.let { details ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(0.dp)) } // Initial spacer if needed after TopAppBar

                // Screenshot Display
                item {
                    TradeScreenshotDisplay(details.screenshotUri)
                }

                // Detailed Trade Information
                item {
                    TradeInfoSection(details)
                }
                
                // R:R Metrics (Placeholder for now)
                item {
                    RRMetricsSection(details.entryPrice, details.stopLossPrice, details.takeProfitPrice)
                }

                // Structured Notes
                item {
                    NotesSection(
                        title = "Pre-Trade Rationale / Setup",
                        content = details.preTradeRationale
                    )
                }
                item {
                    NotesSection(
                        title = "Execution Notes",
                        content = details.executionNotes
                    )
                }
                item {
                    NotesSection(
                        title = "Post-Trade Review / Lessons Learned",
                        content = details.postTradeReview
                    )
                }

                // Tags
                item {
                    TagsSection(details.tags)
                }
                
                item { Spacer(modifier = Modifier.height(72.dp)) } // Space for FABs
            }
        }
    }
}

@Composable
fun TradeScreenshotDisplay(screenshotUri: String?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (screenshotUri != null) {
                // TODO: Load image using Coil or similar library
                // Image(painter = rememberAsyncImagePainter(model = screenshotUri), contentDescription = "Trade Screenshot", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                Text("Screenshot Preview (URI: $screenshotUri)", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_placeholder_image), // Ensure you have a placeholder drawable
                    contentDescription = "No Screenshot Available",
                    modifier = Modifier.size(100.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Composable
fun TradeInfoSection(details: TradeDetailData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Trade Overview", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            InfoRow("Asset Pair:", details.assetPair)
            InfoRow("Date:", details.date)
            InfoRow("Strategy:", details.strategy)
            InfoRow("Market Condition:", details.marketCondition)
            InfoRow("Position Type:", details.positionType)
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
            
            Text("Financials", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            InfoRow("Entry Price:", formatCurrencyDetail(details.entryPrice))
            InfoRow("Stop-Loss:", formatCurrencyDetail(details.stopLossPrice))
            InfoRow("Take-Profit:", formatCurrencyDetail(details.takeProfitPrice, defaultText = "Not Set"))
            InfoRow("Outcome:", details.outcome, valueColor = when(details.outcome) {
                "Win" -> Color(0xFF26A69A)
                "Loss" -> Color(0xFFEF5350)
                else -> MaterialTheme.colorScheme.onSurface
            })
            InfoRow("P&L Amount:", formatCurrencyDetail(details.pnlAmount), valueColor = if((details.pnlAmount ?: 0.0) >= 0) Color(0xFF26A69A) else Color(0xFFEF5350))
            InfoRow("Balance Impact:", formatCurrencyDetail(details.balanceImpact, defaultText = "Not Updated"))
        }
    }
}

@Composable
fun RRMetricsSection(entry: Double?, sl: Double?, tp: Double?) {
    // Placeholder for R:R calculation and display
    // Actual calculation would be: (TP - Entry) / (Entry - SL) for long, etc.
    val plannedRR = if (entry != null && sl != null && tp != null && (entry - sl) != 0.0) {
        String.format("%.2f : 1", kotlin.math.abs((tp - entry) / (entry - sl)))
    } else {
        "N/A"
    }
    // Actual R:R would need the exit price, which isn't in TradeDetailData yet explicitly
    val actualRR = "N/A (Requires Exit Price)"

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Risk/Reward Metrics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            InfoRow("Planned R:R Ratio:", plannedRR)
            InfoRow("Actual R:R Achieved:", actualRR) 
        }
    }
}

@Composable
fun NotesSection(title: String, content: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(bottom = 8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Text(
                text = content.ifBlank { "No notes provided for this section." },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TagsSection(tags: List<String>) {
    if (tags.isNotEmpty()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Tags", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(bottom = 8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Using simple Text for now, could be Chips later
                tags.take(5).forEach { tag -> // Limit displayed tags for brevity
                    Text(
                        text = "#$tag", 
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium, modifier = Modifier.weight(0.4f))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = valueColor, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(0.6f), textAlign = TextAlign.End)
    }
}

// --- Previews ---
@Preview(showBackground = true, name = "Trade Detail Screen - Light")
@Composable
fun TradeDetailScreenPreviewLight() {
    ProfitPathTheme(darkTheme = false) {
        TradeDetailScreen(tradeId = "previewId123")
    }
}

@Preview(showBackground = true, name = "Trade Detail Screen - Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TradeDetailScreenPreviewDark() {
    ProfitPathTheme(darkTheme = true) {
        TradeDetailScreen(tradeId = "previewId123")
    }
}

@Preview(showBackground = true, name = "Trade Detail - No ID")
@Composable
fun TradeDetailScreenNoIdPreview() {
    ProfitPathTheme {
        TradeDetailScreen(tradeId = null)
    }
}

@Preview(showBackground = true)
@Composable
fun TradeInfoSectionPreview() {
    ProfitPathTheme {
        TradeInfoSection(TradeDetailData("1", "GBP/JPY", "2024-08-01", "Breakout", "High Volatility", "Short", 190.55, 190.85, 189.95, "Loss", -60.0, -60.0, "Rationale", "Exec Notes", "Review Notes", listOf("Volatile", "News"), null))
    }
}

@Preview(showBackground = true)
@Composable
fun NotesSectionPreview() {
    ProfitPathTheme {
        NotesSection(title = "Sample Notes Title", content = "These are some detailed notes about the trade, discussing various aspects and observations made during the process.")
    }
} 