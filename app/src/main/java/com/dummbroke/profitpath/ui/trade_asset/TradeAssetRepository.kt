package com.dummbroke.profitpath.ui.trade_asset

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

data class Asset(
    val id: String = "",
    val name: String = "",
    val type: String = "Forex" // Could be Forex, Crypto, Stock, etc.
)

class TradeAssetRepository {

    private val firestore = FirebaseFirestore.getInstance()
    // No longer a single top-level collection, path will be user-specific
    // private val tradesCollection = firestore.collection("trades")

    // --- Asset CRUD ---
    fun getUserAssets(userId: String): Flow<List<Asset>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val assetsRef = firestore.collection("users").document(userId).collection("assets")
        val listener = assetsRef.addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                val assets = snapshot.documents.mapNotNull { doc ->
                    val name = doc.getString("name") ?: return@mapNotNull null
                    val type = doc.getString("type") ?: "Forex"
                    Asset(id = doc.id, name = name, type = type)
                }
                trySend(assets)
            }
        }
        awaitClose { listener.remove() }
    }

    fun addUserAsset(userId: String, asset: Asset): Flow<Result<Unit>> = callbackFlow {
        if (userId.isBlank() || asset.name.isBlank()) {
            trySend(Result.failure(Exception("User ID and asset name required.")))
            close()
            return@callbackFlow
        }
        val assetsRef = firestore.collection("users").document(userId).collection("assets")
        val docRef = if (asset.id.isNotBlank()) assetsRef.document(asset.id) else assetsRef.document()
        val data = mapOf("name" to asset.name, "type" to asset.type)
        docRef.set(data)
            .addOnSuccessListener { trySend(Result.success(Unit)); close() }
            .addOnFailureListener { e -> trySend(Result.failure(e)); close(e) }
        awaitClose {}
    }

    fun deleteUserAsset(userId: String, assetId: String): Flow<Result<Unit>> = callbackFlow {
        if (userId.isBlank() || assetId.isBlank()) {
            trySend(Result.failure(Exception("User ID and asset ID required.")))
            close()
            return@callbackFlow
        }
        val docRef = firestore.collection("users").document(userId).collection("assets").document(assetId)
        docRef.delete()
            .addOnSuccessListener { trySend(Result.success(Unit)); close() }
            .addOnFailureListener { e -> trySend(Result.failure(e)); close(e) }
        awaitClose {}
    }

    fun updateUserAsset(userId: String, asset: Asset): Flow<Result<Unit>> = callbackFlow {
        if (userId.isBlank() || asset.id.isBlank() || asset.name.isBlank()) {
            trySend(Result.failure(Exception("User ID, asset ID, and name required.")))
            close()
            return@callbackFlow
        }
        val docRef = firestore.collection("users").document(userId).collection("assets").document(asset.id)
        val data = mapOf("name" to asset.name, "type" to asset.type)
        docRef.set(data)
            .addOnSuccessListener { trySend(Result.success(Unit)); close() }
            .addOnFailureListener { e -> trySend(Result.failure(e)); close(e) }
        awaitClose {}
    }
} 