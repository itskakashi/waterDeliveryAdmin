package com.example.admin.presentation

import AdminUser
import Analytics
import Bill
import Order
import Payment
import User
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.admin.domain.FireBaseRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.Year
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FireBaseViewModel(private val repository: FireBaseRepository) : ViewModel() {

    private val _provider = MutableStateFlow<User?>(null)
    val provider: StateFlow<User?> get() = _provider.asStateFlow()

    init {
        viewModelScope.launch {
            provider.collect { user ->
                Log.d("providerUpdated", "Provider updated: ${user}")
            }
        }
    }
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    private val _selectedMonth = MutableStateFlow(LocalDateTime.now().month)
    @RequiresApi(Build.VERSION_CODES.O)
    val selectedMonth = _selectedMonth.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    private val _selectedYear = MutableStateFlow(Year.now())
    @RequiresApi(Build.VERSION_CODES.O)
    val selectedYear = _selectedYear.asStateFlow()

    private val _userid = MutableStateFlow("")
    val userid = _userid.asStateFlow()

    // ... rest of your existing StateFlow and LiveData declarations ...
    private val _customDateOrders = MutableLiveData<List<Order>>()
    val customDateOrders: LiveData<List<Order>> = _customDateOrders

    private val _orders = MutableLiveData<List<Order>>()
    val orders: LiveData<List<Order>> = _orders

    private val _todayOrders = MutableLiveData<List<Order>>()
    val todayOrders: LiveData<List<Order>> = _todayOrders

    private val _createOrderStatus = MutableLiveData<Result<String>>()
    val createOrderStatus: LiveData<Result<String>> = _createOrderStatus

    private val _getOrderStatus = MutableLiveData<Result<Order>>()
    val getOrderStatus: LiveData<Result<Order>> = _getOrderStatus

    private val _updateOrderStatus = MutableLiveData<Result<Unit>>()
    val updateOrderStatus: LiveData<Result<Unit>> = _updateOrderStatus

    private val _deleteOrderStatus = MutableLiveData<Result<Unit>>()
    val deleteOrderStatus: LiveData<Result<Unit>> = _deleteOrderStatus

    private val _bills = MutableStateFlow<List<Bill>>(emptyList())
    val bills: StateFlow<List<Bill>> get() = _bills

    private val _selectedBill = MutableStateFlow<Bill?>(null)
    val selectedBill: StateFlow<Bill?> get() = _selectedBill

    private val _allBills = MutableStateFlow<List<Bill>>(emptyList())
    val allBills: StateFlow<List<Bill>> get() = _allBills

    private val _payments = MutableStateFlow<List<Payment>>(emptyList())
    val payments: StateFlow<List<Payment>> get() = _payments

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> get() = _user

    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    val allUsers: StateFlow<List<User>> get() = _allUsers

    private val _getOrdersByMonth = MutableLiveData<List<Order>>()
    val getOrdersByMonth: LiveData<List<Order>> = _getOrdersByMonth

    private val _adminUser = MutableStateFlow<AdminUser?>(null)
    val adminUser: StateFlow<AdminUser?> = _adminUser.asStateFlow()

    private val _isAdminLoggedIn = MutableStateFlow(false)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _isLoadingAdmin = MutableStateFlow(false)
    val isLoadingAdmin: StateFlow<Boolean> = _isLoading.asStateFlow()

    // ... rest of your existing functions ...

    fun setUserid(userid: String) {
        _userid.value = userid
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateSelectedMonth(month: Month) {
        _selectedMonth.value = month
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateSelectedYear(year: Year) {
        _selectedYear.value = year
    }

    fun updateUserId(userid: String) {
        _userid.update {
            userid
        }
    }

    fun updateCurrentUser(userid: String) {
        viewModelScope.launch {
            repository.getUser(userid).onSuccess { user ->
                Log.d("updateCurrentUser", "${user.toString()}")

                _user.update {
                    user
                }
                Log.d("updateCurrentUser1", "${_user.value}")
            }.onFailure { exception ->
                println("Error getting user : ${exception.message}")
            }
        }
    }


    fun updateCurrentAdmin(userid: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getUser(userid).onSuccess { user ->
                Log.d("updateCurrentAdmin", "${user.toString()}")
                _provider.value = user
                _isLoading.value = false

                Log.d("updateCurrentAdmin1", "${_provider.value}")
                Log.d("updateCurrentAdmin", "${provider.value}")
            }.onFailure { exception ->
                println("Error getting user : ${exception.message}")
                _isLoading.value = false
            }
        }
    }
    fun signUp(user: User, password: String, onSuccess: (String) -> Unit, onFailure: (Throwable) -> Unit) {
        viewModelScope.launch {
            repository.signUp(user, password).onSuccess { userId ->
                onSuccess(userId)
            }.onFailure { exception ->
                onFailure(exception)
            }
        }
    }

    fun signIn(email: String, password: String, onSuccess: (String) -> Unit, onFailure: (Throwable) -> Unit) {
        viewModelScope.launch {
            repository.signIn(email, password).onSuccess { userId ->
                onSuccess(userId)
            }.onFailure { exception ->
                onFailure(exception)
            }
        }
    }

    fun getUser(userId: String, onSuccess: (User) -> Unit, onFailure: (Throwable) -> Unit) {
        viewModelScope.launch {
            repository.getUser(userId).onSuccess { user ->
                onSuccess(user)
            }.onFailure { exception ->
                onFailure(exception)
            }
        }
    }

    fun updateUser(user: User, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        viewModelScope.launch {
            repository.updateUser(user).onSuccess {
                onSuccess()
            }.onFailure { exception ->
                onFailure(exception)
            }
        }
    }

    fun logOut(onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        viewModelScope.launch {
            repository.logOut().onSuccess {
                onSuccess()
            }.onFailure { exception ->
                onFailure(exception)
            }
        }
    }

    fun addCustomer(
        user: User,
        password: String,
        onSuccess: (String) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            repository.addCustomer(user, password).onSuccess { userId ->
                onSuccess(userId)
            }.onFailure { exception ->
                onFailure(exception)
            }
        }
    }

    fun deleteUser(userId: String, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        viewModelScope.launch {
            repository.deleteUser(userId).onSuccess {
                onSuccess()
            }.onFailure { exception ->
                onFailure(exception)
            }
        }
    }

    fun updateAmount(userRef: DocumentReference, amount: Double, isAddition: Boolean, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        viewModelScope.launch {
            repository.updateAmount(userRef, amount, isAddition).onSuccess {
                onSuccess()
            }.onFailure { exception ->
                onFailure(exception)
            }
        }
    }

    fun getAllUsers(onSuccess: (List<User>) -> Unit, onFailure: (Throwable) -> Unit) {
        viewModelScope.launch {
            repository.getAllUsers().onSuccess { users ->
                _allUsers.value = users
                onSuccess(users)
            }.onFailure { exception ->
                onFailure(exception)
            }
        }
    }

    fun createOrder(
        userId: String,
        order: Order,
        onSuccess: (String) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            repository.createOrder(userId, order).onSuccess { orderId ->
                _createOrderStatus.value = Result.success(orderId)
                onSuccess(orderId)
            }.onFailure { exception ->
                _createOrderStatus.value = Result.failure(exception)
                onFailure(exception)
            }
        }
    }

    fun getOrder(
        userId: String,
        order: Order,
        onSuccess: (Order) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            repository.getOrder(userId, order).onSuccess { order ->
                _getOrderStatus.value = Result.success(order)
                onSuccess(order)
            }.onFailure { exception ->
                _getOrderStatus.value = Result.failure(exception)
                onFailure(exception)
            }
        }
    }

    fun updateOrderStatus(
        userId: String,
        orderId: String,
        newStatus: String,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            repository.updateOrderStatus(userId, orderId, newStatus).onSuccess {
                _updateOrderStatus.value = Result.success(it)
                onSuccess()
            }.onFailure { exception ->
                _updateOrderStatus.value = Result.failure(exception)
                onFailure(exception)
            }
        }
    }

    fun updateOrderStatusUsingRef(
        userId: DocumentReference,
        orderId: String,
        newStatus: String,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            repository.updateOrderStatusUsingRef(userId, orderId, newStatus).onSuccess {
                _updateOrderStatus.value = Result.success(it)
                onSuccess()
            }.onFailure { exception ->
                _updateOrderStatus.value = Result.failure(exception)
                onFailure(exception)
            }
        }
    }

    fun updateOrder(
        userId: String,
        order: Order,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            repository.updateOrder(userId, order).onSuccess {
                _updateOrderStatus.value = Result.success(it)
                onSuccess()
            }.onFailure { exception ->
                _updateOrderStatus.value = Result.failure(exception)
                onFailure(exception)
            }
        }
    }

    fun deleteOrder(
        userId: String,
        orderId: String,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            repository.deleteOrder(userId, orderId).onSuccess {
                _deleteOrderStatus.value = Result.success(it)
                onSuccess()
            }.onFailure { exception ->
                _deleteOrderStatus.value = Result.failure(exception)
                onFailure(exception)
            }
        }
    }

    fun getAllOrders(
        userId: String,
        onSuccess: (List<Order>) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            repository.getAllOrders(userId).onSuccess { orders ->
                _orders.value = orders
                onSuccess(orders)
            }.onFailure { exception ->
                // Handle error
                onFailure(exception)
            }
        }
    }

    fun getAllTodayOrders(
        userId: String,
        onSuccess: (List<Order>) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            repository.getAllTodayOrders(userId).onSuccess { todayOrders ->
                _todayOrders.value = todayOrders
                onSuccess(todayOrders)
            }.onFailure { exception ->
                // Handle error
                onFailure(exception)
            }
        }
    }

    fun getOrdersByMonth(
        userId: String,
        yearMonth: String,
        onSuccess: (List<Order>) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val firstDayOfMonth = dateFormat.parse("$yearMonth-01")
            val calendar = Calendar.getInstance()
            calendar.time = firstDayOfMonth
            calendar.add(Calendar.MONTH, 1)
            calendar.add(Calendar.DAY_OF_MONTH, -1)
            val lastDayOfMonth = calendar.time
            repository.getOrdersByMonth(userId, firstDayOfMonth, lastDayOfMonth)
                .onSuccess { orders ->
                    _getOrdersByMonth.value = orders
                    onSuccess(orders)
                }.onFailure { exception ->
                    // Handle error
                    onFailure(exception)
                }
        }
    }

    fun getAllUsersTodayOrders(
        onSuccess: (List<Order>) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            repository.getAllUsersTodayOrders().onSuccess { todayOrders ->
                Log.d("todayOrdersmain", "todayOrders: $todayOrders")
                _todayOrders.value = todayOrders
                onSuccess(todayOrders)
            }.onFailure { exception ->
                Log.d("todayOrdersmain", "todayOrders: ${exception.message}")
                // Handle error
                onFailure(exception)
            }
        }
    }

    fun getAllUsersOrdersByCustomDate(
        date: LocalDate,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            repository.getAllUsersOrdersByCustomDate(date).onSuccess { orders ->
                _customDateOrders.value = orders
                onSuccess()
            }.onFailure { exception ->
                onFailure(exception)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getBillsForMonthAndYear(userId: String, month: Month, year: Year) {
        _isLoading.value = true
        _error.value = null

        Log.d("ViewModel", "Fetching bills for userId: $userId, month: $month, year: $year")

        val startOfMonth = LocalDateTime.of(year.value, month, 1, 0, 0, 0)
        val endOfMonth = startOfMonth.plusMonths(1).minusNanos(1)

        val startDate =
            Timestamp(Date.from(startOfMonth.atZone(ZoneId.systemDefault()).toInstant()))
        val endDate =
            Timestamp(Date.from(endOfMonth.atZone(ZoneId.systemDefault()).toInstant()))
        viewModelScope.launch {
            repository.getBillsForMonthAndYear(userId, startDate, endDate)
                .onSuccess {
                    Log.d("ViewModel", "Bills successfully fetched: $it")
                    _bills.value = it
                }
                .onFailure {
                    Log.e("ViewModel", "Error fetching bills: ${it.message}")
                    _error.value = it.message ?: "An unknown error occurred"
                }
            _isLoading.value = false
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun markBillAsPaidForUser(
        userId: String,
        billId: String,
        onSuccess: () -> Unit,
        onFailure: (e: Exception) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.markBillAsPaidForUser(userId, billId)
                .onSuccess {
                    onSuccess()
                }
                .onFailure {
                    onFailure(it as Exception)
                }
            _isLoading.value = false
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun markAllBillsAsPaidForMonth(
        userId: String,
        month: Month,
        year: Year,
        onSuccess: () -> Unit,
        onFailure: (e: Exception) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true

            val startOfMonth = LocalDateTime.of(year.value, month, 1, 0, 0, 0)
            val endOfMonth = startOfMonth.plusMonths(1).minusNanos(1)

            val startDate =
                Timestamp(Date.from(startOfMonth.atZone(ZoneId.systemDefault()).toInstant()))
            val endDate =
                Timestamp(Date.from(endOfMonth.atZone(ZoneId.systemDefault()).toInstant()))

            repository.markAllBillsAsPaidForMonth(userId, startDate, endDate)
                .onSuccess {
                    onSuccess()
                }
                .onFailure {
                    onFailure(it as Exception)
                }
            _isLoading.value = false
        }
    }

    fun getAllBills() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getAllBills()
                .onSuccess {
                    _allBills.value = it
                }
                .onFailure {
                    _error.value = it.message ?: "An unknown error occurred"
                }
            _isLoading.value = false
        }
    }

    fun getBill(billId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getBill(billId).onSuccess {
                _selectedBill.value = it
            }.onFailure {
                _error.value = it.message
            }
            _isLoading.value = false
        }
    }

    fun updateBill(bill: Bill, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.updateBill(bill).onSuccess {
                onSuccess()
            }.onFailure {
                onFailure(it as Exception)
            }
            _isLoading.value = false
        }
    }

    fun createBill(
        bill: Bill,
        onSuccess: (billId: String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.createBill(bill).onSuccess {
                onSuccess(it)
            }.onFailure {
                onFailure(it as Exception)
            }
            _isLoading.value = false
        }
    }

    fun getAnalytics(
        analyticsId: String,
        onSuccess: (Analytics) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            repository.getAnalytics(analyticsId).onSuccess { analytics ->
                onSuccess(analytics)
            }.onFailure { exception ->
                onFailure(exception)
            }
        }
    }

    fun updateAnalytics(
        analytics: Analytics,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            repository.updateAnalytics(analytics).onSuccess {
                onSuccess()
            }.onFailure { exception ->
                onFailure(exception)
            }
        }
    }

    fun updateCanesReturned(
        userId: String,
        canesReturned: Int,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            repository.updateCanesReturned(userId, canesReturned).onSuccess {
                onSuccess()
            }.onFailure { exception ->
                onFailure(exception)
            }
        }
    }

    fun updateCanesTaken(
        userId: String,
        canesTaken: Int,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            repository.updateCanesTaken(userId, canesTaken).onSuccess {
                onSuccess()
            }.onFailure { exception ->
                onFailure(exception)
            }
        }
    }

    fun loginAdmin(
        email: String,
        passwordHash: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        viewModelScope.launch {
            signIn(email, passwordHash, { user ->
                updateCurrentAdmin(user)
                updateUserId(user)
                getUser(user, { it ->
                    if (it.isStaff == true) {
                        Log.d("LoginScreen", "Login Successful  ")
                        onSuccess()
                    } else {
                        Log.d("LoginScreen", "Login unSuccessful1 ${it.isStaff}")
                        logOut({}, {})
                        onFailure()
                    }
                }, {
                    Log.d("LoginScreen", "Login unSuccessful2 ")
                    onFailure()
                })
            }, {
                onFailure()
            })
        }
    }
}