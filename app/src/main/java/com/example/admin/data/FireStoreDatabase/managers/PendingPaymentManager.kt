



import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await


class PendingPaymentManager {
    private val db: FirebaseFirestore = Firebase.firestore
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // --- Add pending payment ---
    suspend fun addPendingPayment(pendingPayment: PendingPayment): Result<String> {
        return try {
            val documentReference = db.collection("PendingPayments").document()
            val pendingPaymentId = documentReference.id
            pendingPayment.pendingPaymentId = pendingPaymentId
            documentReference.set(pendingPayment).await()
            Result.success(pendingPaymentId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Get All pending payment ---
    suspend fun getAllPendingPayments(): Result<List<PendingPayment>> {
        return try {
            val querySnapshot = db.collection("PendingPayments").get().await()
            val pendingPayments = mutableListOf<PendingPayment>()
            for (document in querySnapshot.documents) {
                val pendingPayment = document.toObject(PendingPayment::class.java)
                pendingPayment?.let { pendingPayments.add(it) }
            }
            Result.success(pendingPayments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}