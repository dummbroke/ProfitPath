package com.dummbroke.profitpath.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine

import com.dummbroke.profitpath.ui.home.HomeRepository
import com.dummbroke.profitpath.ui.home.HomeUserProfile
import com.dummbroke.profitpath.ui.home.TradeStatSummary
import com.dummbroke.profitpath.ui.home.TradeInsights
import com.dummbroke.profitpath.core.models.Trade

class HomeViewModel : ViewModel() {
    private val repository = HomeRepository()
    val userProfile: StateFlow<HomeUserProfile?> = repository.getHomeUserProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val tradeStats: StateFlow<TradeStatSummary?> = repository.getTradeStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val tradeInsights: StateFlow<TradeInsights?> = repository.getTradeInsights()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val recentTrades: StateFlow<List<Trade>> = repository.getRecentTrades()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
} 