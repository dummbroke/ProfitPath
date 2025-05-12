package com.dummbroke.profitpath.ui.trade_detail

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.dummbroke.profitpath.R
import com.dummbroke.profitpath.ui.theme.ProfitPathTheme
import com.google.firebase.auth.FirebaseAuth

// Updated TradeDetailData to include all fields from repository mapping
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
    val balanceBeforeTrade: Double?,
    val accountBalanceAfterTrade: Double?,
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

// --- Helper to format currency ---
fun formatCurrencyDetail(value: Double?, defaultText: String = "N/A"): String {
    return value?.let { String.format("$%.2f", it) } ?: defaultText
}

// Helper to format percentage
fun formatPercentage(value: Double?): String {
    return value?.let { String.format("%.2f%%", it) } ?: "N/A"
}

// --- Composable Functions ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradeDetailScreen(
    navController: NavController,
    tradeId: String?,
    tradeDetailViewModel: TradeDetailViewModel = viewModel()
) {
    Log.d("TradeDetailScreen", "Screen composed. Received tradeId: $tradeId")
    val uiState by tradeDetailViewModel.uiState.collectAsState()
    Log.d("TradeDetailScreen", "Current uiState: $uiState")
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(tradeId, currentUserId) {
        Log.d("TradeDetailScreen", "LaunchedEffect triggered. tradeId: '$tradeId', currentUserId: $currentUserId")
        if (currentUserId.isNullOrBlank()) {
            Log.w("TradeDetailScreen", "User ID is null or blank.")
            tradeDetailViewModel.setError("User not logged in.")
        } else {
            if (tradeId == null) {
                Log.w("TradeDetailScreen", "Trade ID is NULL in LaunchedEffect.")
                tradeDetailViewModel.setNoTradeIdError()
            } else if (tradeId.isBlank()) {
                Log.w("TradeDetailScreen", "Trade ID is BLANK (empty string) in LaunchedEffect.")
                tradeDetailViewModel.setNoTradeIdError() // Or a more specific error if desired
            } else {
                Log.i("TradeDetailScreen", "Fetching details for userId: $currentUserId, tradeId: '$tradeId'")
                tradeDetailViewModel.fetchTradeDetails(currentUserId, tradeId)
            }
        }
    }
    
    LaunchedEffect(uiState) {
        if (uiState is TradeDetailUiState.Deleted) {
            snackbarHostState.showSnackbar("Trade deleted successfully")
            navController.popBackStack()
            tradeDetailViewModel.resetUiStateToIdle()
        }
        if (uiState is TradeDetailUiState.Error && (uiState as TradeDetailUiState.Error).message != "No trade ID provided." && (uiState as TradeDetailUiState.Error).message != "User not logged in.") {
            val errorState = uiState as TradeDetailUiState.Error
            if(errorState.message.isNotEmpty() && errorState.message != "Trade not found."){
                 snackbarHostState.showSnackbar("Error: ${errorState.message}")
                 tradeDetailViewModel.resetUiStateToIdle()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (uiState is TradeDetailUiState.Success) {
                val successState = uiState as TradeDetailUiState.Success
                val details = successState.trade
                if (details.id.isBlank() || details.assetPair == "N/A") {
                    Log.w("TradeDetailScreen", "TradeDetailData is empty or missing key fields: $details")
                }
                val currentTradeIdForFab = details.id
                val currentUserIdForFab = FirebaseAuth.getInstance().currentUser?.uid

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FloatingActionButton(
                        onClick = {
                            if (!currentUserIdForFab.isNullOrBlank() && currentTradeIdForFab.isNotBlank()) {
                                tradeDetailViewModel.deleteTrade(currentUserIdForFab, currentTradeIdForFab)
                            } else {
                                // Show error, ideally via snackbar or log
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ) {
                        Icon(Icons.Filled.Delete, "Delete Trade")
                    }
                    FloatingActionButton(
                        onClick = {
                            android.widget.Toast.makeText(context, "Edit feature coming soon!", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Filled.Edit, "Edit Trade")
                    }
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        when (val currentState = uiState) {
            is TradeDetailUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                    Text("Loading trade details...", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 70.dp))
                }
            }
            is TradeDetailUiState.Success -> {
                val details = currentState.trade
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(4.dp)) }
                    item { TradeScreenshotDisplay(details.screenshotUri) }
                    item { SectionTitle("Trade Overview") }
                    item { TradeInfoSection(details) }
                    item { SectionTitle("Performance Metrics") }
                    item {
                        RRMetricsSection(
                            entry = details.entryPrice,
                            sl = details.stopLossPrice,
                            tp = details.takeProfitPrice,
                            pnl = details.pnlAmount,
                            positionType = details.positionType
                        )
                    }
                    item { PerformanceDetailsSection(details) }

                    item { SectionTitle("Trade Journal") }
                    item { NotesSection("Pre-Trade Rationale / Setup", details.preTradeRationale) }
                    item { NotesSection("Execution Notes", details.executionNotes) }
                    item { NotesSection("Post-Trade Review / Lessons Learned", details.postTradeReview) }
                    
                    item { SectionTitle("Additional Info") }
                    item { TimestampsSection(details) }
                    
                    item { TagsSection(details.tags) }
                    item { Spacer(modifier = Modifier.height(72.dp)) }
                }
            }
            is TradeDetailUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text(
                        currentState.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            TradeDetailUiState.Idle, TradeDetailUiState.Deleted -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text(if(currentState is TradeDetailUiState.Deleted) "Trade deleted." else "Initializing...", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
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
            if (screenshotUri != null && screenshotUri.isNotBlank()) {
                Text("Screenshot Preview (URI: $screenshotUri)", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(8.dp))
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_placeholder_image),
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
            InfoRow("Asset Pair:", details.assetPair)
            InfoRow("Asset Class:", details.assetClass)
            InfoRow("Date & Time:", details.date)
            InfoRow("Strategy:", details.strategy)
            InfoRow("Market Condition:", details.marketCondition)
            InfoRow("Position Type:", details.positionType, valueColor = if(details.positionType.equals("Long", true)) Color(0xFF26A69A) else Color(0xFFEF5350))
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
            
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
            InfoRow("Balance Before Trade:", formatCurrencyDetail(details.balanceBeforeTrade))
            if(details.balanceUpdated) {
                InfoRow("Updated Account Balance:", formatCurrencyDetail(details.accountBalanceAfterTrade))
            } else {
                 InfoRow("Account Balance After:", formatCurrencyDetail(details.accountBalanceAfterTrade, defaultText = "Not Updated by this trade"))
            }
        }
    }
}

@Composable
fun RRMetricsSection(entry: Double?, sl: Double?, tp: Double?, pnl: Double?, positionType: String) {
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
fun PerformanceDetailsSection(details: TradeDetailData) {
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
fun TimestampsSection(details: TradeDetailData) {
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
fun NotesSection(title: String, content: String) {
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
fun TagsSection(tags: List<String>) {
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

// --- Previews ---
@Preview(showBackground = true, name = "Trade Detail Screen - Loading")
@Composable
fun TradeDetailScreenPreviewLoading() {
    val context = LocalContext.current
    ProfitPathTheme(darkTheme = false) {
        TradeDetailScreen(navController = NavController(context), tradeId = "loadingId")
    }
}

@Preview(showBackground = true, name = "Trade Detail Screen - Error", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TradeDetailScreenPreviewError() {
    val context = LocalContext.current
    ProfitPathTheme(darkTheme = true) {
        TradeDetailScreen(navController = NavController(context), tradeId = null )
    }
}

@Preview(showBackground = true, name = "Full Trade Info Section Preview")
@Composable
fun FullTradeInfoSectionPreview() {
    ProfitPathTheme {
        TradeInfoSection(
            TradeDetailData(
                id = "preview123", assetPair = "BTC/USD", assetClass = "Crypto", date = "2024-05-11 14:30",
                strategy = "FVG Retest", marketCondition = "Bullish Trend", positionType = "Long",
                entryPrice = 68500.0, stopLossPrice = 68000.0, takeProfitPrice = 70000.0, outcome = "Win",
                pnlAmount = 150.0, entryAmountUSD = 1000.0, balanceBeforeTrade = 10000.0,
                accountBalanceAfterTrade = 10150.0, preTradeRationale = "Price retested a Fair Value Gap on the 15m chart.",
                executionNotes = "Smooth entry, good fill.", postTradeReview = "Followed plan, good risk management.",
                tags = listOf("FVG", "Crypto", "Scalp"), screenshotUri = null,
                percentagePnl = 15.0, exitTimestamp = "2024-05-11 14:45", entryClientTimestamp = "2024-05-11 14:29",
                balanceUpdated = true, leverage = 10.0
            )
        )
    }
} 