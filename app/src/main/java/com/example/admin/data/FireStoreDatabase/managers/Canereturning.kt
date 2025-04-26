



import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class CanesManager {
    private val db: FirebaseFirestore = Firebase.firestore
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // --- Update canes returned---
    suspend fun updateCanesReturned(userId: String, canesReturned: Int): Result<Unit> {
        return try {
            db.collection("users").document(userId).update(
                "canesReturned", canesReturned,
                "updatedAt", FieldValue.serverTimestamp()
            ).await()
            Result.success(Unit) // Changed to Result<Unit> and Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Update canes taken---
    suspend fun updateCanesTaken(userId: String, canesTaken: Int): Result<Unit> {
        return try {
            db.collection("users").document(userId).update(
                "canesTaken", canesTaken,
                "updatedAt", FieldValue.serverTimestamp()
            ).await()
            Result.success(Unit) // Changed to Result<Unit> and Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun updateCanesTaken(userRef: DocumentReference, canesToAdd: Int, canesToSubtract: Int): Result<Unit> {
        return try {
            val totalChange = canesToAdd - canesToSubtract // Calculate the net change

            if (totalChange != 0) { // Only update if there's a net change
                userRef.update("canesTaken", FieldValue.increment(totalChange.toLong())).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}