package com.dummbroke.profitpath.ui.performance

import com.dummbroke.profitpath.core.models.Trade
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

// Data class for user profile data
// Add this at the top or in a models file if you modularize later
data class UserProfileData(
    val currentBalance: Double? = null
)

class PerformanceRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    // Empty Repository for Performance data

    suspend fun getTradesForCurrentUser(): List<Trade> = withContext(Dispatchers.IO) {
        val userId = auth.currentUser?.uid ?: return@withContext emptyList()
        val snapshot = firestore.collection("users")
            .document(userId)
            .collection("trades")
            .get()
            .await()
        snapshot.documents.mapNotNull { it.toObject(Trade::class.java) }
    }

    suspend fun getCurrentBalance(): Double? = withContext(Dispatchers.IO) {
        val userId = auth.currentUser?.uid ?: return@withContext null
        val doc: DocumentSnapshot = firestore.collection("users")
            .document(userId)
            .collection("profile")
            .document("user_profile_data")
            .get()
            .await()
        doc.getDouble("currentBalance")
    }
} 