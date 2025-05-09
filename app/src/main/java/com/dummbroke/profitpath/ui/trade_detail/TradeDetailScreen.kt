package com.dummbroke.profitpath.ui.trade_detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun TradeDetailScreen(tradeId: String? = null) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (tradeId != null) {
            Text("Trade Detail Screen for ID: $tradeId")
        } else {
            Text("Trade Detail Screen (No specific trade ID provided)")
        }
    }
} 