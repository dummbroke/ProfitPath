package com.dummbroke.profitpath.ui.trade_detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dummbroke.profitpath.core.models.Trade
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

sealed interface TradeDetailUiState {
    object Loading : TradeDetailUiState
    data class Success(val trade: TradeDetailData) : TradeDetailUiState
    data class Error(val message: String) : TradeDetailUiState
    object Idle : TradeDetailUiState
    object Deleted : TradeDetailUiState
}

class TradeDetailViewModel(
    private val repository: TradeDetailRepository = TradeDetailRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<TradeDetailUiState>(TradeDetailUiState.Idle)
    val uiState: StateFlow<TradeDetailUiState> = _uiState.asStateFlow()

    // Store userId if needed for multiple operations, or pass it every time.
    // For simplicity, passing it each time for now.

    fun fetchTradeDetails(userId: String, tradeId: String) {
        Log.d("TradeDetailViewModel", "fetchTradeDetails called. userId: $userId, tradeId: $tradeId")
        if (userId.isBlank()) {
             Log.w("TradeDetailViewModel", "userId is blank in fetchTradeDetails.")
             _uiState.value = TradeDetailUiState.Error("User ID is missing.")
            return
        }
        if (tradeId.isBlank()) {
            Log.w("TradeDetailViewModel", "tradeId is blank in fetchTradeDetails.")
            _uiState.value = TradeDetailUiState.Error("Trade ID is missing.")
            return
        }
        _uiState.value = TradeDetailUiState.Loading
        viewModelScope.launch {
            repository.getTradeDetails(userId, tradeId)
                .catch { e ->
                    Log.e("TradeDetailViewModel", "Error fetching trade details: ${e.message}", e)
                    _uiState.value = TradeDetailUiState.Error(e.message ?: "Failed to fetch trade details.")
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { tradeData ->
                            Log.i("TradeDetailViewModel", "Successfully fetched trade details for tradeId: $tradeId")
                            _uiState.value = TradeDetailUiState.Success(tradeData)
                        },
                        onFailure = { e ->
                            Log.e("TradeDetailViewModel", "Failure result fetching trade details for tradeId: $tradeId: ${e.message}", e)
                            _uiState.value = TradeDetailUiState.Error(e.message ?: "Failed to load trade data.")
                        }
                    )
                }
        }
    }

    fun deleteTrade(userId: String, tradeId: String) {
        Log.d("TradeDetailViewModel", "deleteTrade called. userId: $userId, tradeId: $tradeId")
        if (userId.isBlank()) {
            _uiState.value = TradeDetailUiState.Error("User ID is missing for deletion.")
           return
       }
        if (tradeId.isBlank()) {
            _uiState.value = TradeDetailUiState.Error("Trade ID is missing for deletion.")
            return
        }
        _uiState.value = TradeDetailUiState.Loading
        viewModelScope.launch {
            repository.deleteTrade(userId, tradeId)
                .catch { e ->
                     Log.e("TradeDetailViewModel", "Error deleting trade: ${e.message}", e)
                    _uiState.value = TradeDetailUiState.Error(e.message ?: "Failed to delete trade.")
                }
                .collect { result ->
                    result.fold(
                        onSuccess = {
                            Log.i("TradeDetailViewModel", "Successfully deleted trade: $tradeId")
                            _uiState.value = TradeDetailUiState.Deleted
                        },
                        onFailure = { e ->
                            Log.e("TradeDetailViewModel", "Failure result deleting trade: $tradeId: ${e.message}", e)
                            _uiState.value = TradeDetailUiState.Error(e.message ?: "Failed to delete trade.")
                        }
                    )
                }
        }
    }

    fun resetUiStateToIdle() {
        _uiState.value = TradeDetailUiState.Idle
    }

    fun setNoTradeIdError() {
        if (_uiState.value == TradeDetailUiState.Idle) {
            _uiState.value = TradeDetailUiState.Error("No trade ID provided.")
        }
    }

    fun setError(message: String) {
        _uiState.value = TradeDetailUiState.Error(message)
    }
} 