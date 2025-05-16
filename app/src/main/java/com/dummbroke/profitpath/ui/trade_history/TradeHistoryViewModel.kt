package com.dummbroke.profitpath.ui.trade_history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dummbroke.profitpath.core.models.Trade
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest

sealed class TradeHistoryUiState {
    object Loading : TradeHistoryUiState()
    data class Success(val trades: List<Pair<String, Trade>>) : TradeHistoryUiState()
    data class Error(val message: String) : TradeHistoryUiState()
}

class TradeHistoryViewModel(
    private val repository: TradeHistoryRepository = TradeHistoryRepository(FirebaseFirestore.getInstance()),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {
    private val _uiState = MutableStateFlow<TradeHistoryUiState>(TradeHistoryUiState.Loading)
    val uiState: StateFlow<TradeHistoryUiState> = _uiState

    init {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            // Listen for real-time updates
            viewModelScope.launch {
                repository.observeUserTrades(userId).collectLatest { result ->
                    _uiState.value = result.fold(
                        onSuccess = { TradeHistoryUiState.Success(it) },
                        onFailure = { TradeHistoryUiState.Error(it.message ?: "Unknown error") }
                    )
                }
            }
        } else {
            _uiState.value = TradeHistoryUiState.Error("User not logged in.")
        }
    }

    // Optionally keep fetchTrades for manual refresh if needed
    fun fetchTrades(userId: String) {
        _uiState.value = TradeHistoryUiState.Loading
        viewModelScope.launch {
            val result = repository.fetchUserTrades(userId)
            _uiState.value = result.fold(
                onSuccess = { TradeHistoryUiState.Success(it) },
                onFailure = { TradeHistoryUiState.Error(it.message ?: "Unknown error") }
            )
        }
    }

    // Delete a trade by its document ID
    fun deleteTrade(tradeId: String) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val result = repository.deleteTrade(userId, tradeId)
            result.onFailure { e ->
                _uiState.value = TradeHistoryUiState.Error(e.message ?: "Failed to delete trade.")
            }
            // On success, the real-time listener will update the UI automatically
        }
    }
} 