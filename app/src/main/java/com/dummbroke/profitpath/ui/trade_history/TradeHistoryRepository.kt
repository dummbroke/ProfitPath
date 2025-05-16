package com.dummbroke.profitpath.ui.trade_history

import android.util.Log
import com.dummbroke.profitpath.core.models.Trade
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TradeHistoryRepository(private val firestore: FirebaseFirestore) {
    // Listen for real-time updates to the user's trades
    fun observeUserTrades(userId: String): Flow<Result<List<Pair<String, Trade>>>> = callbackFlow {
        val registration: ListenerRegistration = firestore.collection("users")
            .document(userId)
            .collection("trades")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error)).isSuccess
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val trades = mutableListOf<Pair<String, Trade>>()
                    for (doc in snapshot.documents) {
                        try {
                            val trade = doc.toObject(Trade::class.java)
                            if (trade != null) {
                                trades.add(doc.id to trade)
                            }
                        } catch (e: Exception) {
                            // Optionally log error
                        }
                    }
                    trySend(Result.success(trades)).isSuccess
                }
            }
        awaitClose { registration.remove() }
    }

    // Keep the old fetchUserTrades for manual refresh if needed
    suspend fun fetchUserTrades(userId: String): Result<List<Pair<String, Trade>>> {
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("trades")
                .get()
                .await()
            val trades = mutableListOf<Pair<String, Trade>>()
            for (doc in snapshot.documents) {
                Log.d("TradeHistoryRepository", "Raw Firestore trade doc: id=${doc.id}, data=${doc.data}")
                try {
                    val trade = doc.toObject(Trade::class.java)
                    if (trade != null) {
                        trades.add(doc.id to trade)
                    } else {
                        Log.w("TradeHistoryRepository", "Deserialized trade is null for doc id=${doc.id}")
                    }
                } catch (e: Exception) {
                    Log.e("TradeHistoryRepository", "Failed to deserialize trade doc id=${doc.id}: ${e.message}. Raw data: ${doc.data}", e)
                }
            }
            Result.success(trades)
        } catch (e: Exception) {
            Log.e("TradeHistoryRepository", "Error fetching trades: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Delete a trade by its document ID
    suspend fun deleteTrade(userId: String, tradeId: String): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .collection("trades")
                .document(tradeId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 