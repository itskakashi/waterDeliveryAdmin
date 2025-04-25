

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import kotlin.text.get

class BillingAndPaymentManager {
    private val fireStore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val billsCollection = fireStore.collection("bills")
    private val paymentsCollection = fireStore.collection("payments")
    private val usersCollection = fireStore.collection("users")

    // --- BILLS ---

    suspend fun createBill(bill: Bill): Result<String> {
        return try {
            val billDocument = billsCollection.document()
            bill.billId = billDocument.id
            billDocument.set(bill).await()
            Result.success(billDocument.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markBillAsPaid(billId: String): Result<Unit> {
        return try {
            val billDocument = billsCollection.document(billId)
            billDocument.update("isPaid", true).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateBill(bill: Bill): Result<Unit> {
        return try {
            val billDocument = billsCollection.document(bill.billId!!)
            billDocument.set(bill).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBill(billId: String): Result<Bill> {
        return try {
            val billDocument = billsCollection.document(billId).get().await()
            val bill = billDocument.toObject(Bill::class.java)!!
            Result.success(bill)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllBills(): Result<List<Bill>> {
        return try {
            val billsSnapshot = billsCollection.get().await()
            val bills = billsSnapshot.toObjects(Bill::class.java)
            Result.success(bills)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getBillsForMonthAndYear(userId: String, startDate: Timestamp, endDate: Timestamp): Result<List<Bill>> {
        return try {
            Log.d("FireBaseRepository", "Fetching bills for userId: $userId, startDate: $startDate, endDate: $endDate")
            val userRef = usersCollection.document(userId)

            val billsSnapshot = billsCollection
                .whereEqualTo("userId", userRef)
                .whereGreaterThanOrEqualTo("billDate", startDate)
                .whereLessThanOrEqualTo("billDate", endDate)
                .get().await()

            Log.d("FireBaseRepository", "Bills snapshot size: ${billsSnapshot.size()}")

            val bills = billsSnapshot.toObjects(Bill::class.java)
            Result.success(bills)
        } catch (e: Exception) {
            Log.e("FireBaseRepository", "Error fetching bills: ${e.message}")
            Result.failure(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun markAllBillsAsPaidForMonth(userId: String, startDate: Timestamp, endDate: Timestamp): Result<Unit> {
        return try {
            val userRef = usersCollection.document(userId)
            val billsSnapshot = billsCollection
                .whereEqualTo("userId", userRef)
                .whereGreaterThanOrEqualTo("billDate", startDate)
                .whereLessThanOrEqualTo("billDate", endDate)
                .get().await()

            val batch = fireStore.batch()
            for (billDocument in billsSnapshot.documents) {
                batch.update(billDocument.reference, "isPaid", true)
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun markBillAsPaidForUser(userId: String, billId: String): Result<Unit> {
        return try {
            val userRef = usersCollection.document(userId)
            val billDocument = billsCollection.document(billId)
            val bill = billDocument.get().await().toObject(Bill::class.java)

            if (bill == null) {
                throw Exception("Bill not found")
            }
            if (bill.userId != userRef) {
                throw Exception("User not authorized to update this bill")
            }

            billDocument.update("isPaid", true).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun generateMonthlyBills(onSuccess: () -> Unit, onFailure: (Exception) -> Unit, OrderManager:OrderManager) {
        try {
            val currentMonth = LocalDateTime.now().month
            val currentYear = LocalDateTime.now().year
            val startOfMonth = LocalDateTime.of(currentYear, currentMonth, 1, 0, 0, 0)
            val endOfMonth = startOfMonth.plusMonths(1).minusNanos(1)

            val startDate = Timestamp(Date.from(startOfMonth.atZone(ZoneId.systemDefault()).toInstant()))
            val endDate = Timestamp(Date.from(endOfMonth.atZone(ZoneId.systemDefault()).toInstant()))

            val ordersResult= OrderManager.getAllOrdersForMonth( startDate,endDate)

            if(ordersResult.isSuccess){
                val  orders=ordersResult.getOrThrow()
                Log.d("FireBaseRepository", "Orders snapshot size: ${orders.size}")
                val bills = mutableListOf<Bill>()

                orders.forEach { order ->
                    if (order.userID != null) {
                        val bill = Bill(
                            userId = order.userID,
                            amount = order.totalAmount, // Replace with logic to determine amount
                            totalJars = order.quantity, // Replace with logic to determine total jars
                            billDate = order.orderDate as Timestamp?,
                            orderId = usersCollection.document(order.orderId?:""),
                            paymentStatus = "unpaid", // Set initial payment status
                            isPaid = false, // Set initial isPaid status
                            month = currentMonth.value,
                            year = currentYear,
                            overdueDate = null,
                            isOverdue = false,
                            date = null

                        )
                        bills.add(bill)
                    }

                }
                val batch = fireStore.batch()
                bills.forEach { bill ->
                    val billDocument = billsCollection.document()
                    bill.billId = billDocument.id
                    batch.set(billDocument, bill)
                }

                batch.commit().await()

                onSuccess() // Call onSuccess when done
            }else {
                onFailure(ordersResult.exceptionOrNull()!! as Exception)
            }

        } catch (e: Exception) {
            onFailure(e) // Call onFailure if there's an error
        }
    }

    // --- PAYMENTS ---

    suspend fun recordPayment(payment: Payment): Result<String> {
        return try {
            val paymentDocument = paymentsCollection.document()
            payment.paymentId = paymentDocument.id
            paymentDocument.set(payment).await()
            Result.success(paymentDocument.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllPayments(): Result<List<Payment>> {
        return try {
            val paymentsSnapshot = paymentsCollection.get().await()
            val payments = paymentsSnapshot.toObjects(Payment::class.java)
            Result.success(payments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- OTHER ---

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }


    suspend fun getAllPaymentsForUser(userId: String): Result<List<Payment>> {
        return try {
            val snapshot = paymentsCollection
                .whereEqualTo("userId", usersCollection.document(userId))
                .get()
                .await()
            val payments = snapshot.toObjects(Payment::class.java)
            Result.success(payments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
