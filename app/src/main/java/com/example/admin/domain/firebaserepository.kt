package com.example.admin.domain

import AdminUser
import Analytics
import AnalyticsManager
import Bill
import BillingAndPaymentManager
import CanesManager
import Order
import OrderManager
import Payment
import User
import UserManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.admin.data.FireStoreDatabase.managers.AdminManager

import com.google.firebase.Timestamp
import java.util.Date


class FireBaseRepository(
      val OrderManager:OrderManager,
      val  analyticsManager: AnalyticsManager,
      val  BillingAndPaymentManager: BillingAndPaymentManager,
      val   UserManager : UserManager,
      val  CanesManager: CanesManager,
      val adminManager: AdminManager
  )



  {

//    admin manager

      suspend fun getAdmin(): Result<AdminUser> {
          return adminManager.getAdmin()
      }

//   user manager
     suspend fun signUp(user: User, password: String): Result<String> {
         return UserManager.signUp(user, password)
     }
     suspend fun signIn(email: String, password: String): Result<String> {
         return UserManager.login(email, password)
     }
     suspend fun getUser(userId: String): Result<User> {
         return UserManager.getUser(userId)
     }
     suspend fun updateUser(user: User): Result<Unit> {
         return UserManager.updateUser(user)
     }
     suspend fun addCustomer(user: User, password: String): Result<String> {
         return UserManager.addCustomer(user, password)
     }
     suspend fun deleteUser(userId: String): Result<Unit> {
         return UserManager.deleteUser(userId)
     }
     suspend fun getAllUsers(): Result<List<User>> {
         return UserManager.getAllUsers()
     }
    suspend fun logOut(): Result<Unit> {
        return UserManager.logOut()
    }



// Order manager
     suspend fun  createOrder(userId: String,order: Order): Result<String> {
         return OrderManager.createOrder(userId,order)
     }
     suspend fun getOrder(userId: String, order: Order): Result<Order> {
         return OrderManager.getOrder(userId,order.orderId!!)
     }
     suspend fun updateOrderStatus(userId: String, orderId: String, newStatus: String): Result<Unit> {
         return OrderManager.updateOrderStatus(userId, orderId,newStatus)
     }
     suspend fun updateOrder(userId: String, order: Order): Result<Unit> {
         return OrderManager.updateOrder(userId,order)
     }
     suspend fun deleteOrder(userId: String, orderId: String): Result<Unit> {
         return OrderManager.deleteOrder(userId,orderId)
     }
     suspend fun getAllOrders(userId:String): Result<List<Order>> {
         return OrderManager.getAllOrders(userId)
     }
     suspend fun getAllTodayOrders(UserId:String): Result<List<Order>> {
         return OrderManager.getAllTodayOrders(UserId)
     }
      suspend fun getOrdersByMonth(userId: String, firstDayOfMonth: Date, lastDayOfMonth: Date): Result<List<Order>> {
          return OrderManager.getOrdersByMonth(userId, firstDayOfMonth, lastDayOfMonth)
      }



     // Bill manager

      // Bill manager
      suspend fun createBill(bill: Bill): Result<String> {
          return BillingAndPaymentManager.createBill(bill)
      }

      suspend fun markBillAsPaid(billId: String): Result<Unit> {
          return BillingAndPaymentManager.markBillAsPaid(billId)
      }

      suspend fun updateBill(bill: Bill): Result<Unit> {
          return BillingAndPaymentManager.updateBill(bill)
      }

      suspend fun getBill(billId: String): Result<Bill> {
          return BillingAndPaymentManager.getBill(billId)
      }

      suspend fun getAllBills(): Result<List<Bill>> {
          return BillingAndPaymentManager.getAllBills()
      }

      @RequiresApi(Build.VERSION_CODES.O)
      suspend fun getBillsForMonthAndYear(userId: String, startDate: Timestamp, endDate: Timestamp): Result<List<Bill>> {
          return BillingAndPaymentManager.getBillsForMonthAndYear(userId, startDate, endDate)
      }

      @RequiresApi(Build.VERSION_CODES.O)
      suspend fun generateMonthlyBills(onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
          return BillingAndPaymentManager.generateMonthlyBills(onSuccess, onFailure,OrderManager)
      }
      @RequiresApi(Build.VERSION_CODES.O)
      suspend fun markAllBillsAsPaidForMonth(userId: String, startDate: Timestamp, endDate: Timestamp): Result<Unit> {
          return BillingAndPaymentManager.markAllBillsAsPaidForMonth(userId,startDate,endDate)
      }

      suspend fun markBillAsPaidForUser(userId: String,billId: String): Result<Unit> {
          return BillingAndPaymentManager.markBillAsPaidForUser(userId,billId)
      }

      suspend fun recordPayment(payment: Payment): Result<String> {
          return BillingAndPaymentManager.recordPayment(payment)
      }

      suspend fun getAllPayments(): Result<List<Payment>> {
          return BillingAndPaymentManager.getAllPayments()
      }
      //get current user id
      fun getCurrentUserId(): String? {
          return BillingAndPaymentManager.getCurrentUserId()
      }

     /// Analytics manager

     suspend fun getAnalytics(analyticsId: String): Result<Analytics> {
          return analyticsManager.getAnalytics(analyticsId)
     }

     suspend fun updateAnalytics(analytics: Analytics): Result<Unit> {
          return analyticsManager.updateAnalytics(analytics)
     }

/// canes manager
     suspend fun updateCanesReturned(userId: String, canesReturned: Int): Result<Unit> {
          return CanesManager.updateCanesReturned(userId, canesReturned)
     }
     suspend fun updateCanesTaken(userId: String, canesTaken: Int): Result<Unit> {
          return CanesManager.updateCanesTaken(userId, canesTaken)
     }








}


