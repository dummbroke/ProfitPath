package com.dummbroke.profitpath.ui.trade_asset

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

// UI state for asset management
sealed interface AssetUiState {
    object Idle : AssetUiState
    object Loading : AssetUiState
    data class Success(val message: String = "") : AssetUiState
    data class Error(val message: String) : AssetUiState
}

class TradeAssetViewModel(
    private val repository: TradeAssetRepository = TradeAssetRepository()
) : ViewModel() {
    private val _assets = MutableStateFlow<List<Asset>>(emptyList())
    val assets: StateFlow<List<Asset>> = _assets.asStateFlow()

    private val _uiState = MutableStateFlow<AssetUiState>(AssetUiState.Idle)
    val uiState: StateFlow<AssetUiState> = _uiState.asStateFlow()

    // Store userId if needed for multiple operations, or pass it every time.
    // For simplicity, passing it each time for now.

    fun fetchAssets(userId: String) {
        _uiState.value = AssetUiState.Loading
        viewModelScope.launch {
            repository.getUserAssets(userId)
                .catch { e ->
                    _uiState.value = AssetUiState.Error(e.message ?: "Failed to fetch assets.")
                }
                .collect { assetList ->
                    _assets.value = assetList
                    _uiState.value = AssetUiState.Idle
                }
        }
    }

    fun addAsset(userId: String, name: String, type: String) {
        if (name.isBlank()) {
            _uiState.value = AssetUiState.Error("Asset name cannot be empty.")
            return
        }
        _uiState.value = AssetUiState.Loading
        viewModelScope.launch {
            repository.addUserAsset(userId, Asset(name = name, type = type))
                .catch { e ->
                    _uiState.value = AssetUiState.Error(e.message ?: "Failed to add asset.")
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { _uiState.value = AssetUiState.Success("Asset added.") },
                        onFailure = { e -> _uiState.value = AssetUiState.Error(e.message ?: "Failed to add asset.") }
                    )
                }
        }
    }

    fun deleteAsset(userId: String, assetId: String) {
        _uiState.value = AssetUiState.Loading
        viewModelScope.launch {
            repository.deleteUserAsset(userId, assetId)
                .catch { e ->
                    _uiState.value = AssetUiState.Error(e.message ?: "Failed to delete asset.")
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { _uiState.value = AssetUiState.Success("Asset deleted.") },
                        onFailure = { e -> _uiState.value = AssetUiState.Error(e.message ?: "Failed to delete asset.") }
                    )
                }
        }
    }

    fun updateAsset(userId: String, asset: Asset) {
        if (asset.id.isBlank() || asset.name.isBlank()) {
            _uiState.value = AssetUiState.Error("Asset ID and name required.")
            return
        }
        _uiState.value = AssetUiState.Loading
        viewModelScope.launch {
            repository.updateUserAsset(userId, asset)
                .catch { e ->
                    _uiState.value = AssetUiState.Error(e.message ?: "Failed to update asset.")
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { _uiState.value = AssetUiState.Success("Asset updated.") },
                        onFailure = { e -> _uiState.value = AssetUiState.Error(e.message ?: "Failed to update asset.") }
                    )
                }
        }
    }

    fun resetUiState() {
        _uiState.value = AssetUiState.Idle
    }
} 