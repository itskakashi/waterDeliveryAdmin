

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrderManager {
    private val fireStore = FirebaseFirestore.getInstance()
    private val orderCollection = fireStore.collection("orders")
    private val usersCollection = fireStore.collection("users")

    private val db: FirebaseFirestore = Firebase.firestore
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Helper function to get the user's document reference
    private fun getUserDocumentReference(userId: String): DocumentReference {
        return db.collection("users").document(userId)
    }

    // --- Creating a New Order ---
    suspend fun createOrder(userId: String, order: Order): Result<String> {
        return try {
            val userRef = getUserDocumentReference(userId) // Get the user's reference
            val orderCollectionRef = userRef.collection("orders") // Get the orders subcollection
            val documentReference = orderCollectionRef.document()
            val orderId = documentReference.id
            order.orderId = orderId
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            order.orderDate = dateFormat.format(Date())

            documentReference.set(order).await()
            Result.success(orderId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Retrieving Order Details ---
    suspend fun getOrder(userId: String, orderId: String): Result<Order> {
        return try {
            val userRef = getUserDocumentReference(userId) // Get the user's reference
            val orderCollectionRef = userRef.collection("orders") // Get the orders subcollection
            val document = orderCollectionRef.document(orderId).get().await()
            if (document.exists()) {
                val order = document.toObject(Order::class.java)
                Result.success(order!!)
            } else {
                Result.failure(Exception("Order not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Updating Order Status ---
    fun updateOrderStatus(userId: String, orderId: String, newStatus: String): Result<Unit> {
        Log.d("Userid", "second id is : $userId")
        Log.d("orderid", "orderId is : $orderId") // Add this line
        return try {
            val orderCollectionRef = db.collection("users").document(userId).collection("orders")
            orderCollectionRef.document(orderId).update("deliveryStatus", newStatus)
                .addOnFailureListener {
                    Log.d("updated", "unsuccessful: $it")
                }
                .addOnSuccessListener {
                    Log.d("updated", "successful")
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.d("updated", "unsuccessful: $e")
            Result.failure(e)
        }
    }

    // --- Updating order ---
    suspend fun updateOrder(userId: String, order: Order): Result<Unit> {
        return try {
            val userRef = getUserDocumentReference(userId) // Get the user's reference
            val orderCollectionRef = userRef.collection("orders") // Get the orders subcollection
            orderCollectionRef.document(order.orderId!!).update(
                mapOf(
                    "orderNumber" to order.orderNumber,
                    "userID" to order.userID,
                    "deliveryAddress" to order.deliveryAddress,
                    "waterType" to order.waterType,
                    "quantity" to order.quantity,
                    "expectedDeliveryDate" to order.expectedDeliveryDate,
                    "isDelivered" to order.isDelivered,
                    "deliveryTime" to order.deliveryTime,
                    "deliveryStatus" to order.deliveryStatus,
                    "deliveryFee" to order.deliveryFee,
                    "totalAmount" to order.totalAmount,
                    "notes" to order.notes,
                    "items" to order.items,
                    "canesReturning" to order.canesReturning,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Deleting an Order ---
    suspend fun deleteOrder(userId: String, orderId: String): Result<Unit> {
        return try {
            val userRef = getUserDocumentReference(userId) // Get the user's reference
            val orderCollectionRef = userRef.collection("orders") // Get the orders subcollection
            orderCollectionRef.document(orderId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Get All Order ---
    suspend fun getAllOrders(userId: String): Result<List<Order>> {
        return try {
            val userRef = getUserDocumentReference(userId) // Get the user's reference
            val orderCollectionRef = userRef.collection("orders") // Get the orders subcollection
            val querySnapshot = orderCollectionRef.get().await()
            val orders = mutableListOf<Order>()
            for (document in querySnapshot.documents) {
                val order = document.toObject(Order::class.java)
                order?.let { orders.add(it) }
            }
            Result.success(orders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Get All Today Order ---
    suspend fun getAllTodayOrders(userId: String): Result<List<Order>> {
        return try {
            val userRef = getUserDocumentReference(userId) // Get the user's reference
            val orderCollectionRef = userRef.collection("orders") // Get the orders subcollection
            val today = Timestamp(Date())
            val querySnapshot = orderCollectionRef.whereEqualTo("expectedDeliveryDate", today).get().await()
            val orders = mutableListOf<Order>()
            for (document in querySnapshot.documents) {
                val order = document.toObject(Order::class.java)
                order?.let { orders.add(it) }
            }
            Result.success(orders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // New function: getOrdersByMonth
    suspend fun getOrdersByMonth(userId: String, firstDayOfMonth: Date, lastDayOfMonth:Date): Result<List<Order>> {
        return try {
            val userRef = getUserDocumentReference(userId)
            val orderCollectionRef = userRef.collection("orders")
            val querySnapshot = orderCollectionRef
                .whereGreaterThanOrEqualTo("orderDate",  SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(firstDayOfMonth))
                .whereLessThanOrEqualTo("orderDate", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(lastDayOfMonth))
                .get()
                .await()

            val orders = mutableListOf<Order>()
            for (document in querySnapshot.documents) {
                val order = document.toObject(Order::class.java)
                order?.let { orders.add(it) }
            }
            Result.success(orders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllOrdersForMonth(startDate: Timestamp, endDate: Timestamp): Result<List<Order>>{
        return try {
            val ordersSnapshot = orderCollection.whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate)
                .get().await()
            val orders = ordersSnapshot.toObjects(Order::class.java)
            Result.success(orders)

        } catch (e: Exception) {
            Log.e("FireBaseRepository", "Error fetching orders: ${e.message}")
            Result.failure(e)
        }
    }
}