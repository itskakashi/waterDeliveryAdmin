


import com.google.firebase.firestore.DocumentReference
import com.google.firebase.Timestamp

data class AdminUser(
    var email: String? = null,
    var passwordHash: String? = null,
    var isAdmin: Boolean? = false,
    var adminId: String? = null

)

data class User(
    var name: String? = null,
    var initial: String? = null,
    var email: String? = null,
    var userName: String? = null,
    var contactInfo: String? = null,
    var amount: Double? = 0.0,
    var status: String? = null,
    var profilePictureUrl: String? = null,
    var lastOrderDate: Timestamp? = null,
    var isActive: Boolean? = null,
    var monthlyUsage: List<Map<String, Any>>? = null,
    var isRecurringDelivery: Boolean? = null,
    var address: String? = null,
    var defaultJarSize: String? = null,
    var preferredDeliveryTime: String? = null,
    var pushNotificationsEnabled: Boolean? = null,
    var isStaff: Boolean? = false, // Default to false for customers
    var isCompany: Boolean? = null,
    var serviceName: String? = null,
    var coldWaterPrice: Double? = 35.0,
    var regularWaterPrice: Double? = 30.0,
    var isOpen: Boolean? = null,
    var depositMoney: Double? = null,
    var canesTaken: Int? = null,
    var canesReturned: Int? = null,
    var orders: List<DocumentReference> = emptyList(),
    var userId: String? = null,
)




data class Analytics(
    var userId: DocumentReference? = null,
    var totalOrder: Int? = null,
    var totalDelivered: Int? = null,
    var totalAmount: Double? = null,
    var averageRating: Double? = null,
    var waterConsumption: List<Map<String, Any>>? = null,
    var totalJars: Int? = null,
    var totalAmountSpend: Double? = null,
    var weeklyAverage: Double? = null,
    var insights: List<String>? = null,
    var newOrderToday: Int? = null,
    var scheduledDeliveries: Int? = null,
    var activeOrders: Int? = null,
    var totalRevenue: Double? = null,
    var availableStock: Int? = null,
    var activeCustomers: Int? = null,
    var analyticsId: String? = null

)






data class Bill(
    var userId: DocumentReference? = null,
    var amount: Double? = null,
    var date: Timestamp? = null,
    var billDate:Timestamp?=null,
    var month: Int? = null,
    var year: Int? = null,
    var isPaid: Boolean? = null,
    var orderId: DocumentReference? = null,
    var overdueDate: Timestamp? = null,
    var isOverdue: Boolean? = null,
    var totalJars: Int? = null,
    var paymentStatus: String? = null,
    var itemizedDetails: List<String>? = null,
    var billId: String? = null
)



data class Order(
    var userName: String? = "",
    var orderNumber: String? = null,
    var userID: DocumentReference? = null,
    var deliveryAddress: String? = null,
    var waterType: String? = null,
    var quantity: Int? = null,
    var expectedDeliveryDate: Timestamp? = null,
    var isDelivered: Boolean? = null,
    var deliveryTime: Timestamp? = null,
    var deliveryStatus: String? = null,
    var deliveryFee: Double? = null,
    var totalAmount: Double? = null,
    var notes: String? = null,
    var items: Int? = null,
    var canesReturning: Int? = null,
    var orderId: String? = null,
    var orderDate: String? = null

)






data class Payment(
    var userId: DocumentReference? = null,
    var paymentAmount: Double? = null,
    var paymentDate: Timestamp? = null,
    var paymentMethod: String? = null,
    var notes: String? = null,
    var billId: DocumentReference? = null,
    var paymentId: String? = null
)



data class PendingPayment(
    var userId: DocumentReference? = null,
    var amount: Double? = null,
    var dueDate: Timestamp? = null,
    var isOverdue: Boolean? = null,
    var pendingPaymentId: String? = null
)