package com.example.admin.data.FireStoreDatabase.managers

import AdminUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


class AdminManager {
    private val db = FirebaseFirestore.getInstance()
    private val adminsCollection = db.collection("AdminUsers")
    private val adminDocumentId = "admin1" // Define a constant ID for the single admin document

    suspend fun getAdmin(): Result<AdminUser> {
        return try {
            val documentSnapshot = adminsCollection.document(adminDocumentId).get().await()
            if (documentSnapshot.exists()) {
                val adminUser = documentSnapshot.toObject(AdminUser::class.java)
                if (adminUser != null) {
                    Result.success(adminUser)
                } else {
                    Result.failure(Exception("Failed to parse admin user data."))
                }
            } else {
                Result.failure(Exception("Admin user not found."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}