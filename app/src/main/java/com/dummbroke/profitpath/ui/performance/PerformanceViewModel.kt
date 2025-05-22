package com.dummbroke.profitpath.ui.performance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dummbroke.profitpath.core.models.Trade
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat

sealed class PerformanceUiState {
    object Loading : PerformanceUiState()
    data class Success(val overview: PerformanceOverview) : PerformanceUiState()
    data class Error(val message: String) : PerformanceUiState()
}

sealed class DateRange {
    object Overall : DateRange()
    object Last7Days : DateRange()
    object Last30Days : DateRange()
    data class Custom(val start: Long, val end: Long) : DateRange()
}

class PerformanceViewModel : ViewModel() {
    private val repository = PerformanceRepository(
        FirebaseFirestore.getInstance(),
        FirebaseAuth.getInstance()
    )

    private val _uiState = MutableStateFlow<PerformanceUiState>(PerformanceUiState.Loading)
    val uiState: StateFlow<PerformanceUiState> = _uiState.asStateFlow()

    private val _allTrades = MutableStateFlow<List<Trade>>(emptyList())
    private val _strategies = MutableStateFlow<List<String>>(emptyList())
    val strategies: StateFlow<List<String>> = _strategies.asStateFlow()

    private val _selectedStrategy = MutableStateFlow<String>("All")
    val selectedStrategy: StateFlow<String> = _selectedStrategy.asStateFlow()

    private val _selectedDateRange = MutableStateFlow<DateRange>(DateRange.Overall)
    val selectedDateRange: StateFlow<DateRange> = _selectedDateRange.asStateFlow()

    init {
        fetchPerformanceData()
    }

    fun updateDateRange(dateRange: DateRange) {
        _selectedDateRange.value = dateRange
        recalculateUiState()
    }

    fun updateStrategy(strategy: String) {
        _selectedStrategy.value = strategy
        recalculateUiState()
    }

    private fun fetchPerformanceData() {
        viewModelScope.launch {
            try {
                coroutineScope {
                    val tradesDeferred = async { repository.getTradesForCurrentUser() }
                    val balanceDeferred = async { repository.getCurrentBalance() }
                    val trades = tradesDeferred.await()
                    val currentBalance = balanceDeferred.await() ?: 0.0
                    _allTrades.value = trades
                    _strategies.value = trades.mapNotNull { it.strategyUsed }.distinct().sorted()
                    recalculateUiState(currentBalance)
                }
            } catch (e: Exception) {
                _uiState.value = PerformanceUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun recalculateUiState(currentBalance: Double? = null) {
        val trades = filterTrades(_allTrades.value, _selectedDateRange.value, _selectedStrategy.value)
        val balance = currentBalance ?: (_uiState.value as? PerformanceUiState.Success)?.overview?.currentBalance ?: 0.0
        val overview = calculatePerformanceOverview(trades, balance)
        _uiState.value = PerformanceUiState.Success(overview)
    }

    private fun filterTrades(trades: List<Trade>, dateRange: DateRange, strategy: String): List<Trade> {
        val now = System.currentTimeMillis()
        val filteredByDate = when (dateRange) {
            is DateRange.Overall -> trades
            is DateRange.Last7Days -> {
                val start = now - 7L * 24 * 60 * 60 * 1000
                trades.filter { it.tradeDate?.toDate()?.time ?: 0L >= start }
            }
            is DateRange.Last30Days -> {
                val start = now - 30L * 24 * 60 * 60 * 1000
                trades.filter { it.tradeDate?.toDate()?.time ?: 0L >= start }
            }
            is DateRange.Custom -> {
                trades.filter { 
                    val t = it.tradeDate?.toDate()?.time ?: 0L
                    t in dateRange.start..dateRange.end
                }
            }
        }
        return if (strategy == "All") filteredByDate else filteredByDate.filter { it.strategyUsed == strategy }
    }

    fun getDateRangeOptions(): List<Pair<String, DateRange>> = listOf(
        "Overall" to DateRange.Overall,
        "Last 7 Days" to DateRange.Last7Days,
        "Last 30 Days" to DateRange.Last30Days
        // Custom can be handled with a date picker dialog in the UI
    )

    private fun calculatePerformanceOverview(trades: List<Trade>, currentBalance: Double): PerformanceOverview {
        if (trades.isEmpty()) {
            return PerformanceOverview(
                currentBalance = currentBalance,
                netPnL = 0.0,
                pnlPercentage = 0.0,
                totalTrades = 0,
                winRate = 0.0,
                bestTrade = 0.0,
                worstTrade = 0.0,
                profitFactor = 0.0,
                expectancy = 0.0,
                avgWin = 0.0,
                avgLoss = 0.0,
                avgHoldingTime = "0m",
                longestWinStreak = 0,
                longestLossStreak = 0,
                maxDrawdown = 0.0,
                bestDayPnl = 0.0,
                worstDayPnl = 0.0
            )
        }

        // Filter out shadow trades (trades with null or zero PnL)
        val calculableTrades = trades.filter { it.pnlAmount != null && it.pnlAmount != 0.0 }

        if (calculableTrades.isEmpty()) {
            return PerformanceOverview(
                currentBalance = currentBalance,
                netPnL = 0.0,
                pnlPercentage = 0.0,
                totalTrades = 0,
                winRate = 0.0,
                bestTrade = 0.0,
                worstTrade = 0.0,
                profitFactor = 0.0,
                expectancy = 0.0,
                avgWin = 0.0,
                avgLoss = 0.0,
                avgHoldingTime = "0m",
                longestWinStreak = 0,
                longestLossStreak = 0,
                maxDrawdown = calculateMaxDrawdown(trades), // Still calculate drawdown based on all trades with balance updates
                bestDayPnl = 0.0,
                worstDayPnl = 0.0
            )
        }

        val netPnL = calculableTrades.sumOf { it.pnlAmount!! }
        // Recalculate percentagePnl based on entry amount for calculable trades
        val pnlPercentage = if (calculableTrades.isNotEmpty()) {
            calculableTrades.sumOf { it.percentagePnl ?: 0.0 }
        } else 0.0
        val totalTrades = calculableTrades.size
        val wins = calculableTrades.filter { it.outcome.equals("Win", ignoreCase = true) }
        val losses = calculableTrades.filter { it.outcome.equals("Loss", ignoreCase = true) }
        val winRate = if (totalTrades > 0) wins.size.toDouble() / totalTrades else 0.0
        val bestTrade = calculableTrades.maxOfOrNull { it.pnlAmount!! } ?: 0.0
        val worstTrade = calculableTrades.minOfOrNull { it.pnlAmount!! } ?: 0.0
        val grossProfit = wins.sumOf { it.pnlAmount!! }
        val grossLoss = losses.sumOf { it.pnlAmount!!.let { amt -> if (amt < 0) amt else 0.0 } }.let { kotlin.math.abs(it) }
        val profitFactor = if (grossLoss > 0.0) grossProfit / grossLoss else 0.0 // Handle division by zero
        val avgWin = if (wins.isNotEmpty()) wins.map { it.pnlAmount!! }.average() else 0.0
        val avgLoss = if (losses.isNotEmpty()) losses.map { it.pnlAmount!! }.average() else 0.0
        val expectancy = (winRate * avgWin) + ((1 - winRate) * avgLoss)
        // Holding time, streaks, and daily PnL should still consider all filtered trades (including shadow for context)
        val avgHoldingTime = calculateAvgHoldingTime(trades) // Use all filtered trades
        val (longestWinStreak, longestLossStreak) = calculateStreaks(trades) // Use all filtered trades
        val (bestDayPnl, worstDayPnl) = calculateBestWorstDayPnL(trades) // Use all filtered trades
        val maxDrawdown = calculateMaxDrawdown(trades)

        return PerformanceOverview(
            currentBalance = currentBalance,
            netPnL = netPnL,
            pnlPercentage = pnlPercentage,
            totalTrades = totalTrades,
            winRate = winRate,
            bestTrade = bestTrade,
            worstTrade = worstTrade,
            profitFactor = profitFactor,
            expectancy = expectancy,
            avgWin = avgWin,
            avgLoss = avgLoss,
            avgHoldingTime = avgHoldingTime,
            longestWinStreak = longestWinStreak,
            longestLossStreak = longestLossStreak,
            maxDrawdown = maxDrawdown,
            bestDayPnl = bestDayPnl,
            worstDayPnl = worstDayPnl
        )
    }

    private fun calculateAvgHoldingTime(trades: List<Trade>): String {
        val times = trades.mapNotNull { trade ->
            val entry = trade.entryClientTimestamp?.toDate()
            val exit = trade.exitTimestamp?.toDate()
            if (entry != null && exit != null) exit.time - entry.time else null
        }
        if (times.isEmpty()) return "0m"
        val avgMillis = times.average()
        val minutes = (avgMillis / 60000).toInt()
        val hours = minutes / 60
        val mins = minutes % 60
        return if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
    }

    private fun calculateStreaks(trades: List<Trade>): Pair<Int, Int> {
        var maxWinStreak = 0
        var maxLossStreak = 0
        var currentWinStreak = 0
        var currentLossStreak = 0
        trades.sortedBy { it.tradeDate?.toDate() ?: Date(0) }.forEach { trade ->
            when (trade.outcome?.lowercase(Locale.getDefault())) {
                "win" -> {
                    currentWinStreak++
                    currentLossStreak = 0
                }
                "loss" -> {
                    currentLossStreak++
                    currentWinStreak = 0
                }
                else -> {
                    currentWinStreak = 0
                    currentLossStreak = 0
                }
            }
            maxWinStreak = max(maxWinStreak, currentWinStreak)
            maxLossStreak = max(maxLossStreak, currentLossStreak)
        }
        return maxWinStreak to maxLossStreak
    }

    private fun calculateBestWorstDayPnL(trades: List<Trade>): Pair<Double, Double> {
        val grouped = trades.groupBy { it.tradeDate?.toDate()?.let { d -> d.time / (1000 * 60 * 60 * 24) } }
        val dayPnls = grouped.values.map { dayTrades -> dayTrades.sumOf { it.pnlAmount ?: 0.0 } }
        return (dayPnls.maxOrNull() ?: 0.0) to (dayPnls.minOrNull() ?: 0.0)
    }

    private fun calculateMaxDrawdown(trades: List<Trade>): Double {
        val balances = trades.sortedBy { it.tradeDate?.toDate() ?: Date(0) }
            .mapNotNull { it.newBalanceAfterTrade }
        if (balances.isEmpty()) return 0.0
        var maxDrawdown = 0.0
        var peak = balances.first()
        for (balance in balances) {
            if (balance > peak) peak = balance
            val drawdown = peak - balance
            if (drawdown > maxDrawdown) maxDrawdown = drawdown
        }
        return maxDrawdown
    }
} 