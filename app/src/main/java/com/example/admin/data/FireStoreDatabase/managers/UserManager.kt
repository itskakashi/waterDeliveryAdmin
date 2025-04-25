



import android.util.Log
import androidx.compose.ui.geometry.isEmpty
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.QuerySnapshot


class UserManager {

    private val db: FirebaseFirestore = Firebase.firestore
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // --- User Sign-Up (Customer) ---
    suspend fun signUp(user: User, password: String): Result<String> {
        return try {
            val result = auth.createUserWithEmailAndPassword(user.email!!, password).await()
            val userId = result.user!!.uid
            user.userId = userId
            db.collection("users").document(userId).set(user).await()
            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- User Login (Customer and Provider) ---
    suspend fun login(email: String, password: String): Result<String> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user!!.uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Retrieving User Data ---
    suspend fun getUser(userId: String): Result<User> {
        return try {
            val document = db.collection("users").document(userId).get().await()
            if (document.exists()) {
                val user = document.toObject(User::class.java)
                Result.success(user!!)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Updating User Profile ---
    suspend fun updateUser(user: User): Result<Unit> {
        return try {
            db.collection("users").document(user.userId!!).update(
                mapOf(
                    "name" to user.name,
                    "initial" to user.initial,
                    "userName" to user.userName,
                    "contactInfo" to user.contactInfo,
                    "amount" to user.amount,
                    "status" to user.status,
                    "profilePictureUrl" to user.profilePictureUrl,
                    "lastOrderDate" to user.lastOrderDate,
                    "isActive" to user.isActive,
                    "monthlyUsage" to user.monthlyUsage,
                    "isRecurringDelivery" to user.isRecurringDelivery,
                    "address" to user.address,
                    "defaultJarSize" to user.defaultJarSize,
                    "preferredDeliveryTime" to user.preferredDeliveryTime,
                    "pushNotificationsEnabled" to user.pushNotificationsEnabled,
                    "isStaff" to user.isStaff,
                    "isCompany" to user.isCompany,
                    "serviceName" to user.serviceName,
                    "coldWaterPrice" to user.coldWaterPrice,
                    "regularWaterPrice" to user.regularWaterPrice,
                    "isOpen" to user.isOpen,
                    "depositMoney" to user.depositMoney,
                    "canesTaken" to user.canesTaken,
                    "canesReturned" to user.canesReturned,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- add customer (by the staff) ---
    suspend fun addCustomer(user: User, password: String): Result<String> {
        return try {
            val result = auth.createUserWithEmailAndPassword(user.email!!, password).await()
            val userId = result.user!!.uid
            user.userId = userId
            db.collection("users").document(userId).set(user).await()
            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Delete User (by the staff)---
    suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            db.collection("users").document(userId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Get All User (by the staff)---
    suspend fun getAllUsers(): Result<List<User>> {
        return try {
            val document = db.collection("users").get().await()
            val users = mutableListOf<User>()
            for (doc in document) {
                val user = doc.toObject(User::class.java)
                users.add(user)
            }
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun logOut(): Result<Unit> {
        return try {
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun updateAmount(userRef: DocumentReference, amount: Double, isAddition: Boolean): Result<Unit> {
        return try {
            val updateData = if (isAddition) {
                mapOf("amount" to FieldValue.increment(amount))
            } else {
                mapOf("amount" to FieldValue.increment(-amount))
            }
            userRef.update(updateData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addCustomerWithoutAuth(user: User): Result<String> {
        return try {
            // First, check if a user with the same mobile number or email already exists
            val existingUserByMobile = getUserByMobileNumber(user.contactInfo!!)
            val existingUserByEmail = if (user.email != null) getUserByEmail(user.email!!) else null
           Log.d("existingUserByMobile",existingUserByMobile.toString())
            Log.d("existingUserByEmail",existingUserByEmail.toString())


            if (existingUserByMobile != null) {
                return Result.failure(Exception("User with the same mobile number already exists"))
            }

            if (existingUserByEmail != null) {
                return Result.failure(Exception("User with the same email address already exists"))
            }

            // Generate a unique user ID (you could use a different method if needed)
            val userId = db.collection("users").document().id
            user.userId = userId

            // Set the user data in Firestore
            db.collection("users").document(userId).set(user).await()
            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun getUserByMobileNumber(mobileNumber: String): User? {
        val querySnapshot: QuerySnapshot = db.collection("users")
            .whereEqualTo("contactInfo", mobileNumber)
            .get()
            .await()

        return if (querySnapshot.isEmpty) {
            null
        } else {
            querySnapshot.documents[0].toObject(User::class.java)
        }
    }

    private suspend fun getUserByEmail(email: String): User? {
        val querySnapshot: QuerySnapshot = db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .await()

        return if (querySnapshot.isEmpty) {
            null
        } else {
            querySnapshot.documents[0].toObject(User::class.java)
        }
    }


}