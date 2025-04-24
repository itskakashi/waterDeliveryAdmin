import android.content.ContentValues.TAG
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
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

    suspend fun updateOrderStatusUsingRef(userId: DocumentReference, orderId: String, newStatus: String): Result<Unit> {
        Log.d("OrderManager", "Updating order status using ref for userId: $userId, orderId: $orderId, newStatus: $newStatus")
        return try {
            // 1. Get the DocumentReference for the Order within the User's subcollection
            val orderDocRef: DocumentReference = userId.collection("orders").document(orderId)
            Log.d("OrderManager", "orderDocRef: $orderDocRef")

            // 2. Update the order's deliveryStatus
            orderDocRef.update("deliveryStatus", newStatus).await() // Use await() for coroutine support

            Log.d("OrderManager", "Successfully updated order status using ref.")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("OrderManager", "Error updating order status using ref:", e)
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



    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getAllUsersTodayOrders(): Result<List<Order>> {
        return try {
            // Get the start and end of today in the current time zone
            val todayStart = LocalDateTime.of(
                LocalDate.now(),
                LocalTime.MIN)
            val todayEnd = LocalDateTime.of(LocalDate.now(),
                LocalTime.MAX)

            // Convert to Timestamps for Firestore query
            val startTimestamp = Timestamp(Date.from(todayStart.atZone(ZoneId.systemDefault()).toInstant()))
            val endTimestamp = Timestamp(Date.from(todayEnd.atZone(ZoneId.systemDefault()).toInstant()))

            // Query the "orders" collection, not subcollections
            val querySnapshot = fireStore.collectionGroup("orders")
                .whereGreaterThanOrEqualTo("expectedDeliveryDate", startTimestamp)
                .whereLessThanOrEqualTo("expectedDeliveryDate", endTimestamp)
                .orderBy("expectedDeliveryDate", Query.Direction.ASCENDING)
                .get()
                .await()

            val orders = mutableListOf<Order>()
            for (document in querySnapshot.documents) {
                val order = document.toObject(Order::class.java)
                order?.let { orders.add(it) }
            }
            Result.success(orders)
        } catch (e: Exception) {
            Log.e("OrderManager", "Error fetching today's orders: ${e.message}")
            Result.failure(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getOrdersByCustomDate(date: LocalDate): Result<List<Order>> {
        // Define the start and end of the day
        val startOfDay = LocalDateTime.of(date, LocalTime.MIN) // Use LocalTime.MIN for 00:00:00
        val endOfDay = LocalDateTime.of(date, LocalTime.MAX)   // Use LocalTime.MAX for 23:59:59.999999999

        // Convert LocalDateTime to Instant, then to Date, and finally to Timestamp
        val startTimestamp = Timestamp(Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant()))
        val endTimestamp = Timestamp(Date.from(endOfDay.atZone(ZoneId.systemDefault()).toInstant()))

        Log.d(TAG, "getOrdersByCustomDate: startTimestamp = $startTimestamp, endTimestamp = $endTimestamp")

        return try {
            val querySnapshot = db.collectionGroup("orders") // Use collectionGroup here
                .whereGreaterThanOrEqualTo("expectedDeliveryDate", startTimestamp)
                .whereLessThanOrEqualTo("expectedDeliveryDate", endTimestamp)
                .orderBy("expectedDeliveryDate", Query.Direction.ASCENDING) // Corrected: Use ASCENDING for date order
                .get()
                .await()

            val orders = mutableListOf<Order>()
            for (document in querySnapshot.documents) {
                val order = document.toObject(Order::class.java)
                if (order != null) {
                    // Get the userId from the document path
                    val userId = document.reference.parent.parent?.id
                    if (userId != null) {

                        val userRef: DocumentReference = db.collection("users").document(userId)
                        order.userID = userRef // Now correctly assigns a DocumentReference
                        orders.add(order)
                    }
                }
            }
            Result.success(orders)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting orders for custom date: ${e.message}", e)
            Result.failure(e)
        }
    }


}