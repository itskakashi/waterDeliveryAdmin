package com.example.admin.presentation.screens

import AdminUser
import Customer
import Order
import Payment
import User
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.admin.R
import com.example.admin.presentation.FireBaseViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.Year
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import com.google.firebase.Timestamp
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    userId: String?,
    viewModel: FireBaseViewModel,

    ) {
    val user by viewModel.user.collectAsState()
    LaunchedEffect(userId) {
        if (!userId.isNullOrEmpty()) {
            viewModel.updateCurrentUser(userId)

        }

    }
    Log.d("user0," ,"${user}")

    var showEditDialog by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var editedUser by remember { mutableStateOf(user ?: User()) }
    val ordersByMonth by viewModel.getOrdersByMonth.observeAsState(emptyList())
    LaunchedEffect(Unit) {
        viewModel.getOrdersByMonth(
            userId ?: "",
            selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM")),
            {},
            {}
        )
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customer Profile", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Text(text = "Edit", color = Color.Blue)
                    }
                    IconButton(onClick = { /* Handle share action */ }) {
                        Icon(Icons.Filled.Share, "Share")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        user?.let {

            CustomerDetailContent(
                modifier = Modifier.padding(innerPadding),
                customer = it,
                onDateSelected = { newDate ->
                    selectedDate = newDate
                    // Correctly format the yearMonth string
                    val yearMonth = newDate.format(
                        DateTimeFormatter.ofPattern(
                            "yyyy-MM",
                            Locale.getDefault()
                        )
                    )
                    viewModel.getOrdersByMonth(
                        userId = userId!!,
                        yearMonth = yearMonth, // Pass the formatted yearMonth
                        onSuccess = {},
                        onFailure = {}
                    )

                },
                ordersByMonth = ordersByMonth,
                viewModel = viewModel,
                userId = userId
            )
        }
    }

    if (showEditDialog) {
        Log.d("wihoutupdateduser00," ,"${editedUser}")
        EditCustomerDialog(

            editedUser = user?: User(),
            onUserChange = { editedUser = it },
            onDismiss = { showEditDialog = false },
            onSave = {
                Log.d("updateduser00," ,"${editedUser}")
                        viewModel.updateUser(editedUser,{},{})
                      showEditDialog=false;
                viewModel.updateCurrentUser(userId?:"")

                    },

                )


    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CustomerDetailContent(
    modifier: Modifier = Modifier,
    customer: User,
    onDateSelected: (LocalDate) -> Unit,
    ordersByMonth: List<Order>,
    viewModel: FireBaseViewModel,
    userId: String?
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        CustomerInfoSection(customer)

        Spacer(modifier = Modifier.height(16.dp))

        OutstandingPaymentSection(customer)

        Spacer(modifier = Modifier.height(24.dp))

        ActionButtonsSection()

        Spacer(modifier = Modifier.height(24.dp))

        OrderHistorySection(onDateSelected, ordersByMonth)

        Spacer(modifier = Modifier.height(24.dp))

        PaymentHistorySection()
    }
}

@Composable
fun CustomerInfoSection(customer: User) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(text = customer.name ?: "", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Phone, "Phone", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = customer.contactInfo ?: "", color = Color.Gray, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Email, "Email", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = customer.email ?: "", color = Color.Gray, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Info, "address", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = customer.address ?: "", color = Color.Gray, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun OutstandingPaymentSection(customer: User) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFF0F0), RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Outstanding Payment",
            color = Color(0xFFE53935),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = String.format("₹%.2f", customer.amount), fontWeight = FontWeight.Bold, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { /* Handle mark as paid action */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
        ) {
            Text(text = "Mark as Paid", color = Color.White)
        }
    }
}

@Composable
fun ActionButtonsSection() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = { /* Handle place order action */ },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
        ) {
            Text(text = "Place Order", color = Color.White)
        }
        OutlinedButton(
            onClick = { /* Handle download report action */ },
            modifier = Modifier.weight(1f)
        ) {
            Text(text = "Download Report")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun OrderHistorySection(
    onDateSelected: (LocalDate) -> Unit,
    orders: List<Order>,

    ) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "Order History", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(16.dp))
        DatePickerField(onDateSelected = onDateSelected)

        Spacer(modifier = Modifier.height(16.dp))

        if (orders.isEmpty()) {
            Text(text = "No orders for selected period")

        } else {
            LazyColumn(modifier = Modifier.height(250.dp)) {
                items(orders) { order ->
                    OrderItem(order)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun OrderItem(order: Order) {
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")

    val formattedDate =
        order.expectedDeliveryDate?.toDate()?.toInstant()?.atZone(ZoneOffset.UTC)?.toLocalDate()
            ?.format(formatter) ?: order.orderDate ?: "Unknown Date"

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = formattedDate,
                    fontWeight = FontWeight.Bold
                )
                Text(text = "₹{order.items ?: 0} items", color = Color.Gray)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = String.format("₹%.2f", order.totalAmount ?: 0.0),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                OrderStatusIndicator(order.deliveryStatus ?: "unknown")
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Filled.ArrowForward, "More", tint = Color.Gray)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun OrderStatusIndicator(status: String) {
    val color = when (status) {
        "pending" -> Color(0xFFFF9800) // Orange
        "completed" -> Color(0xFF4CAF50) // Green
        else -> Color.Gray
    }
    val text = when (status) {
        "pending" -> "pending"
        "completed" -> "completed"
        else -> "Unknown"
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, color = color, fontSize = 12.sp)
    }
}

@Composable
fun PaymentHistorySection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "Payment History", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(16.dp))

        val payments = listOf(
            PaymentHistoryItem("Feb 28, 2024", 450.00, PaymentMethod.CreditCard),
            PaymentHistoryItem("Feb 15, 2024", 850.00, PaymentMethod.Cash),
            PaymentHistoryItem("Feb 02, 2024", 320.00, PaymentMethod.UPI)
        )

        LazyColumn(modifier = Modifier.height(250.dp)) {
            items(payments) { payment ->
                PaymentItem(payment)
            }
        }
    }
}

data class PaymentHistoryItem(val date: String, val amount: Double, val method: PaymentMethod)

enum class PaymentMethod {
    CreditCard, Cash, UPI
}

@Composable
fun PaymentItem(payment: PaymentHistoryItem) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = payment.date, fontWeight = FontWeight.Bold)
                Text(text = payment.method.name, color = Color.Gray)
            }
            Text(text = String.format("₹%.2f", payment.amount), fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(onDateSelected: (LocalDate) -> Unit) {
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDateText by remember {
        mutableStateOf(
            LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
        )
    }
    val datePickerState = rememberDatePickerState()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(onClick = { showDatePicker = true }) {
            Text(text = selectedDateText)
        }
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    Button(
                        onClick = {
                            showDatePicker = false
                            val selectedDateMillis = datePickerState.selectedDateMillis
                            if (selectedDateMillis != null) {
                                val selectedDate = Instant.ofEpochMilli(selectedDateMillis)
                                    .atZone(ZoneOffset.UTC)
                                    .toLocalDate()
                                onDateSelected(selectedDate)
                                selectedDateText =
                                    selectedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                            }
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    Button(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCustomerDialog(
    editedUser: User,
    onUserChange: (User) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    Log.d("user00," ,"₹{editedUser}")

    var name by remember { mutableStateOf(editedUser.name ?: "") }
    var contactInfo by remember { mutableStateOf(editedUser.contactInfo ?: "") }
    var address by remember { mutableStateOf(editedUser.address ?: "") }
    var initial by remember { mutableStateOf(editedUser.initial ?: "") }


    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Customer Details") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        onUserChange(editedUser.copy(name = it))
                    },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Blue)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = initial,
                    onValueChange = {
                        initial = it
                        onUserChange(editedUser.copy(initial = it))
                    },
                    label = { Text("Initial") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Blue)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = contactInfo,
                    onValueChange = {
                        contactInfo = it
                        onUserChange(editedUser.copy(contactInfo = it))
                    },
                    label = { Text("Contact Info") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Blue)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = address,
                    onValueChange = {
                        address = it
                        onUserChange(editedUser.copy(address = it))
                    },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Blue)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onSave) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}