package com.example.admin.presentation.ui

import AdminUser
import Analytics
import Bill
import Order
import Payment
import androidx.compose.material.icons.filled.Clear
import User
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.getSystemService
import androidx.navigation.NavController
import com.example.admin.R
import com.example.admin.presentation.FireBaseViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flow
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Date
fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewOrderScreen(navController: NavController, viewModel: FireBaseViewModel) {

    var selectedNormalJarQuantity by remember { mutableIntStateOf(0) }
    var selectedColdJarQuantity by remember { mutableIntStateOf(0) }
    var selectedCustomer by remember { mutableStateOf<User?>(null) }
    var selectedDate by remember { mutableStateOf<Date?>(null) }
    var returnCanCount by remember { mutableIntStateOf(0) }
    var isMarkedAsDelivered by remember { mutableStateOf(false) }
    var isMarkedAsPaid by remember { mutableStateOf(false) }
    var totalPrice by remember { mutableIntStateOf(0) }
    val allUsers by viewModel.allUsers.collectAsState()
    var showConfirmDialog by remember { mutableStateOf(false) } // State for showing the dialog
    val context = LocalContext.current

    LaunchedEffect(key1 = true) {
        viewModel.getAllUsers(onSuccess = {
            Log.d("users", it.toString())
        }) {
            Log.d("users", "can not get ${it.message}")
        }
    }


    fun confirmOrder() {

        if (selectedCustomer != null && selectedDate != null && (selectedNormalJarQuantity > 0 || selectedColdJarQuantity > 0)) {
            if (isNetworkAvailable(context)) {
                // Proceed with the order creation as normal
                val db = Firebase.firestore
                val userRef = db.collection("users").document(selectedCustomer!!.userId!!)

                val order = Order(
                    userName = selectedCustomer!!.name,
                    userID = userRef,
                    deliveryAddress = selectedCustomer!!.address,
                    normalWaterQuantity = selectedNormalJarQuantity,
                    coldWaterQuantity = selectedColdJarQuantity,
                    expectedDeliveryDate = Timestamp(selectedDate!!),
                    isDelivered = isMarkedAsDelivered,
                    orderDate = Timestamp.now().toString(),
                    totalAmount = totalPrice.toDouble(),
                    deliveryStatus = if (isMarkedAsDelivered) "Completed" else "Pending",
                    canesReturning = returnCanCount,
                )
                viewModel.createOrder(selectedCustomer!!.userId!!, order, onSuccess = { orderId ->
                    Toast.makeText(context, "your order is successful $orderId", Toast.LENGTH_SHORT).show()
                    Log.d("order", "order created successful ${orderId.toString()}")
                    val db: FirebaseFirestore = Firebase.firestore
                    val userRef = db.collection("users").document(selectedCustomer!!.userId!!)

                    if (isMarkedAsDelivered) {
                        if (!isMarkedAsPaid) {
                            selectedCustomer?.let { customer ->
                                val newAmount = customer.amount?.plus(totalPrice.toDouble())
                                    ?: totalPrice.toDouble()
                                val cantaken = customer.canesTaken?.plus((selectedNormalJarQuantity + selectedColdJarQuantity).minus(returnCanCount))

                                viewModel.updateUser(
                                    customer.copy(
                                        amount = newAmount,
                                        canesTaken = cantaken
                                    ), {
                                        Log.d(
                                            "UpdatedSuccessfully",
                                            " bill amount is successfully updated $"
                                        )
                                    }, {
                                        Log.d(
                                            "UpdatedUNSuccessfully",
                                            " bill amount is UNsuccessfully updated ${it.message.toString()}"
                                        )
                                    })
                            }
                        } else {
                            // Create a payment receipt
                            Log.d("zzzzzz"," zzzzzzzz")
                            selectedCustomer?.let { customer ->

                                val cantaken = customer.canesTaken?.plus((selectedNormalJarQuantity + selectedColdJarQuantity).minus(returnCanCount))

                                viewModel.updateUser(
                                    customer.copy(
                                        canesTaken = cantaken
                                    ), {
                                        Log.d(
                                            "UpdatedSuccessfully",
                                            " bill amount is successfully updated $"
                                        )
                                    }, {
                                        Log.d(
                                            "UpdatedUNSuccessfully",
                                            " bill amount is UNsuccessfully updated ${it.message.toString()}"
                                        )
                                    })
                            }

                            val payment = Payment(
                                userId = userRef,
                                paymentAmount = totalPrice.toDouble(),
                                paymentDate = Timestamp.now(),
                                paymentMethod = "Cash", // You can change the payment method


                            )
                            // Add payment to payments collection
                            viewModel.recordPayment(payment, {}, {})
                        }
                        navController.popBackStack()

                    } else {
                        navController.popBackStack()
                    }


                }) {
                    Log.d("order", "can not create an order ${it.message}")
                    Toast.makeText(
                        context,
                        "Failed to create order. Please try again later.",
                        Toast.LENGTH_LONG
                    ).show()
                    navController.popBackStack()

                }
            } else {
                //save information locally and then return to previous screen
                Toast.makeText(
                    context,
                    "No internet connection",
                    Toast.LENGTH_LONG
                ).show()
                navController.popBackStack()

            }


        } else {
            if (selectedCustomer == null) {
                Toast.makeText(context, "Please select a customer", Toast.LENGTH_SHORT).show()
            } else if (selectedDate == null) {
                Toast.makeText(context, "Please select a date", Toast.LENGTH_SHORT).show()
            } else if (selectedNormalJarQuantity <= 0 && selectedColdJarQuantity <= 0) {
                Toast.makeText(context, "Please add Jar", Toast.LENGTH_SHORT).show()
            }
        }
    }



    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.fillMaxWidth(),
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "New Order",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 48.dp),
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            StandardDeliveryButton()
            Spacer(modifier = Modifier.height(24.dp))
            CustomerDropdown(allUsers = allUsers, onCustomerSelected = { selectedCustomer = it })
            Spacer(modifier = Modifier.height(8.dp))
            AddNewCustomerLink()
            Spacer(modifier = Modifier.height(24.dp))
            DeliveryAddressField(selectedCustomer = selectedCustomer)
            Spacer(modifier = Modifier.height(24.dp))
            JarTypeCounter(
                onNormalWaterChange = { selectedNormalJarQuantity = it },
                onColdWaterChange = { selectedColdJarQuantity = it }
            )
            Spacer(modifier = Modifier.height(24.dp))
            ReturnCanCounter(onValueChange = { returnCanCount = it })
            Spacer(modifier = Modifier.height(24.dp))
            DatePickerFieldd(onDateSelected = { selectedDate = it })
            Spacer(modifier = Modifier.height(24.dp))
            MarkAsDeliveredCheckbox(
                isChecked = isMarkedAsDelivered,
                onCheckedChange = { isMarkedAsDelivered = it }
            )
            Spacer(modifier = Modifier.height(24.dp))
            MarkAsPaymentCheckbox(
                isChecked = isMarkedAsPaid,
                onCheckedChange = { isMarkedAsPaid = it },
                isEnabled = isMarkedAsDelivered
            )
            Spacer(modifier = Modifier.height(24.dp))
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE5E7EB), shape = RoundedCornerShape(10.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(modifier = Modifier.padding(top = 10.dp))
                Text("Order Summery")
                Spacer(modifier = Modifier.padding(top = 10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (selectedNormalJarQuantity > 0) {
                        Text("Normal Water($selectedNormalJarQuantity)")
                        Text("₹ ${selectedCustomer?.regularWaterPrice?.times(selectedNormalJarQuantity) ?: 0.0}.00")
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (selectedColdJarQuantity > 0) {
                        Text("Cold Water($selectedColdJarQuantity)")
                        Text("₹ ${selectedCustomer?.coldWaterPrice?.times(selectedColdJarQuantity) ?: 0.0}.00")
                    }
                }
                Spacer(modifier = Modifier.padding(top = 10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Delivery Fee")
                    Text("₹ 0.00")
                }
                Spacer(modifier = Modifier.padding(top = 10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Cane Returning ")
                    Text("${returnCanCount}")
                }

                Spacer(modifier = Modifier.padding(top = 10.dp))
                Spacer(modifier = Modifier.padding(top = 10.dp))
                HorizontalDivider(
                    modifier = Modifier.padding(10.dp),
                    thickness = 2.dp,
                    color = Color.LightGray
                )
                Spacer(modifier = Modifier.padding(top = 10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total")
                    totalPrice = ((selectedCustomer?.regularWaterPrice?.times(selectedNormalJarQuantity) ?: 0.0) +
                            (selectedCustomer?.coldWaterPrice?.times(selectedColdJarQuantity) ?: 0.0)).toInt()
                    Text("₹ ${totalPrice} ")
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    // Show the confirmation dialog
                    showConfirmDialog = true
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF))
            ) {
                Text(text = "Create Order", color = Color.White, fontSize = 16.sp)
            }
        }
    }

    // Confirmation Dialog
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Your Order") },
            text = {
                Column {
                    Text("Customer Name: ${selectedCustomer?.name}")
                    Text("Delivery Address: ${selectedCustomer?.address}")
                    if (selectedNormalJarQuantity > 0) {
                        Text(
                            "Normal Water: $selectedNormalJarQuantity - ₹ ${
                                selectedCustomer?.regularWaterPrice?.times(
                                    selectedNormalJarQuantity
                                ) ?: 0.0
                            }.00"
                        )
                    }
                    if (selectedColdJarQuantity > 0) {
                        Text(
                            "Cold Water: $selectedColdJarQuantity - ₹ ${
                                selectedCustomer?.coldWaterPrice?.times(
                                    selectedColdJarQuantity
                                ) ?: 0.0
                            }.00"
                        )
                    }
                    Text("Returning Canes: ${returnCanCount}")
                    Text("Total: ₹ ${totalPrice}")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    confirmOrder()
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StandardDeliveryButton() {
    Button(
        onClick = { /* Handle click */ },
        modifier = Modifier
            .fillMaxWidth()
            .size(299.dp, 88.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,

            ) {
            Icon(
                painter = painterResource(id = R.drawable.standarddelivery),
                contentDescription = "Standard Delivery",
                tint = Color.White,
                modifier = Modifier.size(24.dp, 27.2.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Standard Delivery", color = Color.White,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    fontStyle = FontStyle.Normal
                )
            )
        }
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDropdown(allUsers: List<User>, onCustomerSelected: (User) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var selectedCustomer by remember { mutableStateOf<User?>(null) }
    val interactionSource = remember { MutableInteractionSource() }
    var searchQuery by remember { mutableStateOf("") }
    var filteredUsers by remember { mutableStateOf(allUsers) }

    LaunchedEffect(searchQuery, allUsers) {
        if (allUsers.isNotEmpty()) {
            flow {
                emit(searchQuery)
            }.debounce(500).collect { query ->
                filteredUsers = if (query.isBlank()) {
                    allUsers
                } else {
                    allUsers.filter { user ->
                        user.name?.contains(query, ignoreCase = true) == true ||
                                user.address?.contains(query, ignoreCase = true) == true
                    }
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = " Select Customer",
            color = Color.Black,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box {
            OutlinedTextField(
                value = selectedCustomer?.name ?: "Select a customer",
                onValueChange = { },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                interactionSource = interactionSource,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Dropdown",
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF007AFF),
                    unfocusedBorderColor = Color.LightGray,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    disabledTextColor = Color.Transparent,
                    focusedContainerColor = Color(0xFFE5E7EB),
                    unfocusedContainerColor = Color(0xFFE5E7EB),
                ),
                shape = RoundedCornerShape(10.dp)
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        expanded = !expanded
                    }
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    placeholder = { Text("Search by name or address") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                searchQuery = "" // Clear the search query
                                expanded = false // Collapse the dropdown
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = "Clear Search",
                                    tint = Color.Gray
                                )
                            }
                        }
                    }
                )

                if (filteredUsers.isEmpty()) {
                    DropdownMenuItem(text = { Text("No results found") }, onClick = { })
                } else {
                    filteredUsers.forEach { customer ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = customer.name ?: "",
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = customer.address ?: "",
                                        color = Color.Gray,
                                        fontSize = 12.sp
                                    )
                                }
                            },
                            onClick = {
                                selectedCustomer = customer
                                onCustomerSelected(customer)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddNewCustomerLink() {
    Text(
        text = "+ Add New Customer",
        color = Color(0xFF007AFF),
        fontSize = 16.sp,
        modifier = Modifier.clickable { /* Handle add new customer action */ }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryAddressField(selectedCustomer: User?) {

    Log.d("address", "DeliveryAddressField: ${selectedCustomer?.address ?: ""}")
    Text(
        text = "Delivery Address",
        color = Color.Black,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = selectedCustomer?.address ?: "",
        readOnly = true,
        onValueChange = {
        },
        placeholder = { Text("Enter delivery address", color = Color.Gray) },
        trailingIcon = {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                "Delivery Address",
                tint = Color.Gray
            )
        },
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF007AFF),
            unfocusedBorderColor = Color.LightGray,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            disabledTextColor = Color.Transparent,
            focusedContainerColor = Color(0xFFE5E7EB),
            unfocusedContainerColor = Color(0xFFE5E7EB),
        ),
        shape = RoundedCornerShape(10.dp),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JarTypeCounter(onNormalWaterChange: (Int) -> Unit, onColdWaterChange: (Int) -> Unit) {
    var normalWaterCount by remember { mutableIntStateOf(0) }
    var coldWaterCount by remember { mutableIntStateOf(0) }

    Column {
        Text(
            "Jar Type",
            color = Color.Black,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE5E7EB), shape = RoundedCornerShape(10.dp))
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Normal Water", modifier = Modifier.padding(start = 16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    if (normalWaterCount > 0) normalWaterCount--
                    onNormalWaterChange(normalWaterCount)
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.minus),
                        modifier = Modifier.size(20.dp),
                        contentDescription = "Decrease Normal Water Quantity",
                        tint = Color(0xFF4B5563)
                    )
                }
                Text(
                    text = normalWaterCount.toString(),
                    fontSize = 18.sp,
                    color = Color.Black
                )
                IconButton(onClick = {
                    normalWaterCount++
                    onNormalWaterChange(normalWaterCount)
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.plus),
                        modifier = Modifier.size(20.dp),
                        contentDescription = "Increase Normal Water Quantity",
                        tint = Color(0xFF4B5563)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE5E7EB), shape = RoundedCornerShape(10.dp))
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Cold Water", modifier = Modifier.padding(start = 16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    if (coldWaterCount > 0) coldWaterCount--
                    onColdWaterChange(coldWaterCount)
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.minus),
                        modifier = Modifier.size(20.dp),
                        contentDescription = "Decrease Cold Water Quantity",
                        tint = Color(0xFF4B5563)
                    )
                }
                Text(
                    text = coldWaterCount.toString(),
                    fontSize = 18.sp,
                    color = Color.Black
                )
                IconButton(onClick = {
                    coldWaterCount++
                    onColdWaterChange(coldWaterCount)
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.plus),
                        modifier = Modifier.size(20.dp),
                        contentDescription = "Increase Cold Water Quantity",
                        tint = Color(0xFF4B5563)
                    )
                }
            }
        }
    }
}

@Composable
fun ReturnCanCounter(onValueChange: (Int) -> Unit) {
    var count by remember { mutableIntStateOf(0) }
    Column {

        Text(
            "Return Can",
            color = Color.Black,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE5E7EB), shape = RoundedCornerShape(10.dp))
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (count > 0) count--
                onValueChange(count);
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.minus),
                    modifier = Modifier.size(20.dp),
                    contentDescription = "Decrease Quantity",
                    tint = Color(0xFF4B5563)
                )
            }
            Text(text = count.toString(), fontSize = 18.sp, color = Color.Black)
            IconButton(onClick = {
                count++
                onValueChange(count);

            }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.plus),
                    modifier = Modifier.size(20.dp),
                    contentDescription = "Increase Quantity",
                    tint = Color(0xFF4B5563)
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerFieldd(onDateSelected: (Date) -> Unit) {
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDateText by remember { mutableStateOf("") }

    val currentDate = LocalDate.now()
    val currentMillis = currentDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = currentMillis)

    LaunchedEffect(key1 = true) {
        val initialDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(currentMillis), ZoneOffset.UTC)
        val initialDate = Date.from(initialDateTime.toInstant(ZoneOffset.UTC))
        onDateSelected(initialDate)
        selectedDateText = initialDateTime.toLocalDate().toString()
    }

    Column {
        Text(

            text = "Expected Delivery Date",
            color = Color.Black,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = selectedDateText,
            onValueChange = { },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(
                        painter = painterResource(R.drawable.calender),
                        contentDescription = "Date",
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(20.dp)

                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF007AFF),
                unfocusedBorderColor = Color.LightGray,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color(0xFF9CA3AF),
                disabledTextColor = Color.Transparent,
                focusedContainerColor = Color(0xFFE5E7EB),
                unfocusedContainerColor = Color(0xFFE5E7EB),
            ),
            shape = RoundedCornerShape(10.dp)
        )

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            val instant = Instant.ofEpochMilli(it)
                            val dateTime = LocalDateTime.ofInstant(instant, ZoneOffset.UTC)
                            val date = Date.from(dateTime.toInstant(ZoneOffset.UTC))
                            onDateSelected(date)

                            var dateText = dateTime.toString()
                            selectedDateText = dateText.substring(0, 10)

                        }
                        showDatePicker = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDatePicker = false
                    }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

@Composable
fun MarkAsDeliveredCheckbox(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.background(Color(0xFFE5E7EB), shape = RoundedCornerShape(10.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = onCheckedChange
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = "Mark as Delivered", color = Color.Black)

            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                painter = painterResource(R.drawable.time), null,
                modifier = Modifier.size(16.dp),
                tint = Color(0xFF4B5563)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = "Delivery Time: Now", color = Color.Gray, fontSize = 12.sp)

            }
        }
        Spacer(Modifier.height(16.dp))
    }
}


@Composable
fun MarkAsPaymentCheckbox(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isEnabled: Boolean // Add this parameter
) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.background(Color(0xFFE5E7EB), shape = RoundedCornerShape(10.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = if (isEnabled) onCheckedChange else null, //Conditionally set onCheckedChange
                enabled = isEnabled // Conditionally enable/disable the checkbox
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = "Mark as Payment Completed", color = Color.Black)

            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                painter = painterResource(R.drawable.time), null,
                modifier = Modifier.size(16.dp),
                tint = Color(0xFF4B5563)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = "Payment Time: Now", color = Color.Gray, fontSize = 12.sp)

            }
        }
        Spacer(Modifier.height(16.dp))
    }
}