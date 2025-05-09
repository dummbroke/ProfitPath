package com.dummbroke.profitpath.ui.trade_history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun TradeHistoryScreen(onTradeClick: (tradeId: String) -> Unit) {
    // Replace with your actual Trade History UI
    // For now, an example clickable text to test navigation
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onTradeClick("exampleTradeId123") }, // Example usage
        contentAlignment = Alignment.Center
    ) {
        Text("Trade History Screen (Click me to see details for ID: exampleTradeId123)")
    }
} 