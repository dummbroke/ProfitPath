package com.dummbroke.profitpath.ui.home

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import com.dummbroke.profitpath.core.models.Trade

// Data class for Home screen user profile
data class HomeUserProfile(
    val name: String = "",
    val tradingStyle: String = "",
    val balance: Double = 0.0
)

data class TradeStatSummary(
    val totalTrades: Int,
    val winRate: Double,
    val bestTradeAmount: Double
)

data class TradeInsights(
    val biggestWin: Double,
    val worstLoss: Double,
    val mostTradedAsset: String
)

class HomeRepository {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun getHomeUserProfile(): Flow<HomeUserProfile?> = callbackFlow {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val docRef = firestore.collection("users").document(userId).collection("profile").document("user_profile_data")
        val listener = docRef.addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                val name = snapshot.getString("name") ?: "Trader Name"
                val tradingStyle = snapshot.getString("tradingStyle") ?: ""
                val balance = snapshot.getDouble("currentBalance") ?: 0.0
                trySend(HomeUserProfile(name, tradingStyle, balance))
            }
        }
        awaitClose { listener.remove() }
    }

    fun getTradeStats(): Flow<TradeStatSummary?> = callbackFlow {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val tradesRef = firestore.collection("users").document(userId).collection("trades")
        val listener = tradesRef.addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                val trades = snapshot.documents.mapNotNull { it.toObject(Trade::class.java) }
                val total = trades.size
                val wins = trades.count { it.outcome.equals("Win", ignoreCase = true) }
                val winRate = if (total > 0) (wins.toDouble() / total) * 100 else 0.0
                val bestTrade = trades.maxOfOrNull { it.pnlAmount ?: 0.0 } ?: 0.0
                trySend(TradeStatSummary(total, winRate, bestTrade))
            }
        }
        awaitClose { listener.remove() }
    }

    fun getTradeInsights(): Flow<TradeInsights?> = callbackFlow {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val tradesRef = firestore.collection("users").document(userId).collection("trades")
        val listener = tradesRef.addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                val trades = snapshot.documents.mapNotNull { it.toObject(Trade::class.java) }
                val biggestWin = trades.filter { (it.pnlAmount ?: 0.0) > 0.0 }.maxOfOrNull { it.pnlAmount ?: 0.0 } ?: 0.0
                val worstLoss = trades.filter { (it.pnlAmount ?: 0.0) < 0.0 }.minOfOrNull { it.pnlAmount ?: 0.0 } ?: 0.0
                val mostTradedAsset = trades.groupBy { it.specificAsset ?: "" }
                    .maxByOrNull { it.value.size }?.key ?: "-"
                trySend(TradeInsights(biggestWin, worstLoss, mostTradedAsset))
            }
        }
        awaitClose { listener.remove() }
    }

    fun getRecentTrades(): Flow<List<Trade>> = callbackFlow {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val tradesRef = firestore.collection("users").document(userId).collection("trades")
            .orderBy("entryClientTimestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(2)
        val listener = tradesRef.addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                val trades = snapshot.documents.mapNotNull { it.toObject(Trade::class.java) }
                trySend(trades)
            }
        }
        awaitClose { listener.remove() }
    }

    fun getPreviousDayBalance(): Flow<Double?> = callbackFlow {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val now = System.currentTimeMillis()
        val startOfToday = java.util.Calendar.getInstance().apply {
            timeInMillis = now
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
        val tradesRef = firestore.collection("users").document(userId).collection("trades")
            .whereLessThan("tradeDate", com.google.firebase.Timestamp(startOfToday / 1000, 0))
            .orderBy("tradeDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(1)
        val listener = tradesRef.addSnapshotListener { snapshot, _ ->
            if (snapshot != null && !snapshot.isEmpty) {
                val trade = snapshot.documents.firstOrNull()?.toObject(Trade::class.java)
                trySend(trade?.newBalanceAfterTrade)
            } else {
                trySend(null)
            }
        }
        awaitClose { listener.remove() }
    }
} 