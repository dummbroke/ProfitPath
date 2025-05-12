package com.dummbroke.profitpath.ui.trade_detail

import android.util.Log
import com.dummbroke.profitpath.core.models.Trade
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TradeDetailRepository {

    private val firestore = FirebaseFirestore.getInstance()
    // No longer a single top-level collection, path will be user-specific
    // private val tradesCollection = firestore.collection("trades") 

    // Function to get a single trade's details from Firestore
    fun getTradeDetails(userId: String, tradeId: String): Flow<Result<TradeDetailData>> = callbackFlow {
        Log.d("TradeDetailRepository", "getTradeDetails called. userId: $userId, tradeId: $tradeId")
        if (userId.isBlank()) {
            Log.w("TradeDetailRepository", "userId is blank.")
            trySend(Result.failure(IllegalArgumentException("User ID cannot be blank.")))
            close()
            return@callbackFlow
        }
        if (tradeId.isBlank()) {
            Log.w("TradeDetailRepository", "tradeId is blank.")
            trySend(Result.failure(IllegalArgumentException("Trade ID cannot be blank.")))
            close()
            return@callbackFlow
        }

        val docRef = firestore.collection("users").document(userId)
            .collection("trades").document(tradeId)
            
        val listenerRegistration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("TradeDetailRepository", "Error listening to trade document $tradeId for user $userId: ${error.message}", error)
                trySend(Result.failure(error))
                close(error) 
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                Log.i("TradeDetailRepository", "Document found for tradeId: $tradeId, userId: $userId. Mapping data.")
                val firestoreData = snapshot.data
                Log.d("TradeDetailRepository", "Raw Firestore snapshot data: $firestoreData")

                if (firestoreData != null) {
                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
                    
                    val tradeDetailData = TradeDetailData(
                        id = snapshot.id,
                        assetPair = firestoreData["specificAsset"] as? String ?: "N/A",
                        assetClass = firestoreData["assetClass"] as? String ?: "N/A",
                        date = (firestoreData["tradeDate"] as? Long)?.let { sdf.format(java.util.Date(it)) } ?: "N/A",
                        strategy = firestoreData["strategyUsed"] as? String ?: "N/A",
                        marketCondition = firestoreData["marketCondition"] as? String ?: "N/A",
                        positionType = firestoreData["positionType"] as? String ?: "N/A",
                        entryPrice = (firestoreData["entryPrice"] as? Number)?.toDouble(),
                        stopLossPrice = (firestoreData["stopLossPrice"] as? Number)?.toDouble(),
                        takeProfitPrice = (firestoreData["takeProfitPrice"] as? Number)?.toDouble(),
                        outcome = firestoreData["outcome"] as? String ?: "N/A",
                        pnlAmount = (firestoreData["pnlAmount"] as? Number)?.toDouble(),
                        entryAmountUSD = (firestoreData["entryAmountUSD"] as? Number)?.toDouble(),
                        balanceBeforeTrade = (firestoreData["balanceBeforeTrade"] as? Number)?.toDouble(),
                        accountBalanceAfterTrade = (firestoreData["newBalanceAfterTrade"] as? Number)?.toDouble()
                            ?: (firestoreData["balanceBeforeTrade"] as? Number)?.toDouble()?.let { before ->
                                (firestoreData["pnlAmount"] as? Number)?.toDouble()?.let { pnl -> before + pnl }
                            },
                        preTradeRationale = firestoreData["preTradeRationale"] as? String ?: "",
                        executionNotes = firestoreData["executionNotes"] as? String ?: "",
                        postTradeReview = firestoreData["postTradeReview"] as? String ?: "",
                        tags = (firestoreData["tags"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                        screenshotUri = firestoreData["screenshotPath"] as? String,
                        percentagePnl = (firestoreData["percentagePnl"] as? Number)?.toDouble(),
                        exitTimestamp = (firestoreData["exitTimestamp"] as? Long)?.let { sdf.format(java.util.Date(it)) },
                        entryClientTimestamp = (firestoreData["entryClientTimestamp"] as? Long)?.let { sdf.format(java.util.Date(it)) },
                        balanceUpdated = firestoreData["balanceUpdated"] as? Boolean ?: false,
                        leverage = (firestoreData["leverage"] as? Number)?.toDouble()
                    )
                    Log.i("TradeDetailRepository", "Successfully mapped Firestore data to TradeDetailData for $tradeId: $tradeDetailData")
                    trySend(Result.success(tradeDetailData))
                } else {
                    Log.e("TradeDetailRepository", "Firestore data is null for $tradeId, snapshot was: $snapshot")
                    trySend(Result.failure(Exception("Firestore data is null.")))
                }
            } else {
                Log.w("TradeDetailRepository", "Trade document not found or does not exist. tradeId: $tradeId, userId: $userId")
                trySend(Result.failure(Exception("Trade not found.")))
            }
        }
        awaitClose { 
            Log.d("TradeDetailRepository", "Closing listener for $tradeId")
            listenerRegistration.remove() 
        }
    }

    // Function to delete a trade from Firestore
    fun deleteTrade(userId: String, tradeId: String): Flow<Result<Unit>> = callbackFlow {
        Log.d("TradeDetailRepository", "deleteTrade called. userId: $userId, tradeId: $tradeId")
        if (userId.isBlank()) {
            Log.w("TradeDetailRepository", "userId is blank for delete.")
            trySend(Result.failure(IllegalArgumentException("User ID cannot be blank for deletion.")))
            close()
            return@callbackFlow
        }
        if (tradeId.isBlank()) {
            Log.w("TradeDetailRepository", "tradeId is blank for delete.")
            trySend(Result.failure(IllegalArgumentException("Trade ID cannot be blank for deletion.")))
            close()
            return@callbackFlow
        }

        firestore.collection("users").document(userId)
            .collection("trades").document(tradeId).delete()
            .addOnSuccessListener {
                Log.i("TradeDetailRepository", "Successfully deleted trade $tradeId for user $userId from Firestore.")
                trySend(Result.success(Unit))
                close()
            }
            .addOnFailureListener { e ->
                Log.e("TradeDetailRepository", "Failed to delete trade $tradeId for user $userId: ${e.message}", e)
                trySend(Result.failure(e))
                close(e)
            }
        awaitClose { Log.d("TradeDetailRepository", "deleteTrade flow closed for $tradeId") }
    }
} 