package com.dummbroke.profitpath.ui.trade_entry

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dummbroke.profitpath.core.models.Trade
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import com.google.firebase.Timestamp

// Define a UI state class
sealed interface TradeEntryUiState {
    object Idle : TradeEntryUiState
    object Loading : TradeEntryUiState
    object Success : TradeEntryUiState
    data class Error(val message: String) : TradeEntryUiState
}

class TradeEntryViewModel(application: Application) : AndroidViewModel(application) {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val repository: TradeEntryRepository = TradeEntryRepository(application, firestore, auth)

    private val _uiState = MutableStateFlow<TradeEntryUiState>(TradeEntryUiState.Idle)
    val uiState: StateFlow<TradeEntryUiState> = _uiState.asStateFlow()

    // This function will be called from TradeEntryScreen.
    // It needs all the necessary parameters from the screen's state.
    fun saveTradeEntry(
        assetClass: String,
        specificAsset: String,
        strategyUsed: String,
        marketCondition: String,
        positionType: String, // "Long" or "Short"
        entryPriceStr: String,
        stopLossPriceStr: String,
        takeProfitPriceStr: String, // Optional
        outcome: String, // "Win", "Loss", "BreakEven"
        tradeDateStr: String, // e.g., "2024-07-30" or from a date picker
        pnlAmountStr: String, // P&L amount as a string
        preTradeRationale: String,
        executionNotes: String,
        postTradeReview: String,
        tagsStr: String, // Comma-separated tags
        selectedImageUri: Uri?,
        balanceShouldBeUpdated: Boolean,
        newBalanceStr: String, // New balance if updated
        entryAmountUSDStr: String,
        balanceBeforeTradeStr: String
    ) {
        _uiState.value = TradeEntryUiState.Loading
        viewModelScope.launch {
            try {
                // --- Data Validation and Conversion ---
                if (specificAsset.isBlank()) {
                    _uiState.value = TradeEntryUiState.Error("Specific asset cannot be empty.")
                    return@launch
                }
                // Add more validation as needed (e.g., for prices)

                val entryPrice = entryPriceStr.toDoubleOrNull()
                val stopLossPrice = stopLossPriceStr.toDoubleOrNull()
                val takeProfitPrice = takeProfitPriceStr.toDoubleOrNull() // Can be null
                val pnlAmount = pnlAmountStr.toDoubleOrNull()

                // Parsing for remaining new fields
                val entryAmountUSD = entryAmountUSDStr.toDoubleOrNull()
                val balanceBeforeTrade = balanceBeforeTradeStr.toDoubleOrNull()

                if (entryPrice == null || stopLossPrice == null) {
                    _uiState.value = TradeEntryUiState.Error("Entry price and stop loss must be valid numbers.")
                    return@launch
                }

                if (entryAmountUSDStr.isNotBlank() && entryAmountUSD == null) {
                    _uiState.value = TradeEntryUiState.Error("Entry Amount (USD) must be a valid number if provided.")
                    return@launch
                }

                if (balanceBeforeTradeStr.isNotBlank() && balanceBeforeTrade == null) {
                    _uiState.value = TradeEntryUiState.Error("Balance Before Trade must be a valid number if provided.")
                    return@launch
                }

                // --- Timestamp Handling ---
                fun parseDateToTimestamp(dateStr: String): Timestamp? {
                    return try {
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val date = sdf.parse(dateStr)
                        date?.let { Timestamp(it) }
                    } catch (e: Exception) {
                        null
                    }
                }

                val tradeDateTimestamp = if (tradeDateStr != "Select Date" && tradeDateStr.isNotBlank()) {
                    parseDateToTimestamp(tradeDateStr)
                } else {
                    Timestamp.now()
                }

                if (tradeDateTimestamp == null) {
                    _uiState.value = TradeEntryUiState.Error("Invalid trade date format.")
                    return@launch
                }

                val tagsList = if (tagsStr.isNotBlank()) {
                    tagsStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                } else {
                    emptyList()
                }
                
                val newBalance = if (balanceShouldBeUpdated) newBalanceStr.toDoubleOrNull() else null
                if (balanceShouldBeUpdated && newBalance == null) {
                    _uiState.value = TradeEntryUiState.Error("Invalid new balance amount.")
                    return@launch
                }

                // Calculate Percentage P&L
                val percentagePnl = if (pnlAmount != null && entryAmountUSD != null && entryAmountUSD != 0.0) {
                    (pnlAmount / entryAmountUSD) * 100
                } else {
                    null // Cannot calculate if P&L or entry amount is missing or entry amount is zero
                }

                val nowTimestamp = Timestamp.now()

                val trade = Trade(
                    assetClass = assetClass,
                    specificAsset = specificAsset,
                    strategyUsed = strategyUsed,
                    marketCondition = marketCondition,
                    positionType = positionType,
                    entryPrice = entryPrice,
                    stopLossPrice = stopLossPrice,
                    takeProfitPrice = takeProfitPrice,
                    outcome = outcome,
                    tradeDate = tradeDateTimestamp,
                    pnlAmount = pnlAmount,
                    preTradeRationale = preTradeRationale,
                    executionNotes = executionNotes,
                    postTradeReview = postTradeReview,
                    tags = tagsList,
                    // screenshotPath will be set by repository
                    balanceUpdated = balanceShouldBeUpdated,
                    newBalanceAfterTrade = newBalance,
                    entryClientTimestamp = nowTimestamp, // Capture client time for entry
                    // userId and server timestamps will be set by repository/Firestore

                    // Updated fields for Trade object
                    entryAmountUSD = entryAmountUSD,
                    balanceBeforeTrade = balanceBeforeTrade,
                    percentagePnl = percentagePnl,
                    exitTimestamp = nowTimestamp // Trade is saved/exited now
                )

                val result = repository.saveTrade(trade, selectedImageUri)
                result.fold(
                    onSuccess = {
                        _uiState.value = TradeEntryUiState.Success
                    },
                    onFailure = { exception ->
                        _uiState.value = TradeEntryUiState.Error(exception.message ?: "Failed to save trade.")
                    }
                )

            } catch (e: Exception) { // Catch any other unexpected errors during VM logic
                _uiState.value = TradeEntryUiState.Error(e.message ?: "An unexpected error occurred.")
            }
        }
    }

    fun resetUiState() {
        _uiState.value = TradeEntryUiState.Idle
    }
} 