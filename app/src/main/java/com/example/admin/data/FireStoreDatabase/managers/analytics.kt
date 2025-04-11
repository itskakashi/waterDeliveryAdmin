





import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await


class AnalyticsManager {
    private val db: FirebaseFirestore = Firebase.firestore
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // --- update analytics ---
    suspend fun updateAnalytics(analytics: Analytics): Result<Unit> {
        return try {
            db.collection("Analytics").document(analytics.analyticsId!!).update(
                mapOf(
                    "userId" to analytics.userId,
                    "totalOrder" to analytics.totalOrder,
                    "totalDelivered" to analytics.totalDelivered,
                    "totalAmount" to analytics.totalAmount,
                    "averageRating" to analytics.averageRating,
                    "waterConsumption" to analytics.waterConsumption,
                    "totalJars" to analytics.totalJars,
                    "totalAmountSpend" to analytics.totalAmountSpend,
                    "weeklyAverage" to analytics.weeklyAverage,
                    "insights" to analytics.insights,
                    "newOrderToday" to analytics.newOrderToday,
                    "scheduledDeliveries" to analytics.scheduledDeliveries,
                    "activeOrders" to analytics.activeOrders,
                    "totalRevenue" to analytics.totalRevenue,
                    "availableStock" to analytics.availableStock,
                    "activeCustomers" to analytics.activeCustomers,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit) // Return Unit for success
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Get analytics ---
    suspend fun getAnalytics(analyticsId: String): Result<Analytics> {
        return try {
            val document = db.collection("Analytics").document(analyticsId).get().await()
            if (document.exists()) {
                val analytics = document.toObject(Analytics::class.java)
                Result.success(analytics!!)
            } else {
                Result.failure(Exception("analytics not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}












