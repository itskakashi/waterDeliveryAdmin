package com.example.admin.presentation.screens

import Bill
import BottomNavigationBar
import Order
import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.admin.presentation.FireBaseViewModel
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.material3.AssistChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TextButton
import androidx.navigation.NavController
import com.example.admin.presentation.ui.isNetworkAvailable
import com.example.admin.presentation.ui.route
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import selectedItem
import java.time.Instant
import java.time.ZoneOffset




@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderScreen(navController: NavController, viewModel: FireBaseViewModel) {

    var selectedDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    var selectedStatus by remember { mutableStateOf("Pending") }
    var searchQuery by remember { mutableStateOf("") }
    var isDatePickerDialogOpen by remember { mutableStateOf(false) }
    val customOrders by viewModel.customDateOrders.observeAsState(emptyList())
    val todayOrders by viewModel.todayOrders.observeAsState(emptyList())
    val showCustomDateOrders = selectedDate != null && selectedDate != LocalDate.now()
    val ordersToDisplay = if (showCustomDateOrders) customOrders else todayOrders
    val context = LocalContext.current
    var showOfflineToast by remember { mutableStateOf(false) }

    Log.d("todayOrders", "todayOrders: $todayOrders")
    LaunchedEffect(key1 = selectedDate, block = {
        if (selectedDate == LocalDate.now()) {
            viewModel.getAllUsersTodayOrders(onSuccess = {}, onFailure = {})
        } else {
            viewModel.getAllUsersOrdersByCustomDate(selectedDate!!, {}, {})
        }

    })
    Log.d("customorder", "customOrder: $customOrders")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Orders", style = MaterialTheme.typography.headlineMedium)
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack(route.dashBoardScreen, false)
                        selectedItem = 0
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            SearchBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                modifier = Modifier.padding(16.dp)
            )
            DateFilter(
                selectedDate = selectedDate,
                onDateSelected = { newDate ->
                    selectedDate = newDate
                },
                modifier = Modifier.padding(horizontal = 16.dp),
                onOpenDatePicker = { isDatePickerDialogOpen = true }

            )
            Spacer(modifier = Modifier.height(8.dp))
            StatusFilter(
                selectedStatus = selectedStatus,
                onStatusSelected = { selectedStatus = it },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OrdersList(
                orders = ordersToDisplay,
                selectedStatus = selectedStatus,
                selectedDate = selectedDate,
                searchQuery = searchQuery,
                viewModel = viewModel,
                showOfflineToast = {
                    Toast.makeText(context,"No Internet connection", Toast.LENGTH_SHORT).show()

                }
            )

        }
    }
    if (isDatePickerDialogOpen) {
        CustomDatePickerDialog(
            onDateSelected = { selectedDate = it; isDatePickerDialogOpen = false },
            onDismiss = { isDatePickerDialogOpen = false }
        )
    }
}

@SuppressLint("NewApi")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun OrdersList(
    orders: List<Order>,
    selectedStatus: String,
    selectedDate: LocalDate?,
    searchQuery: String,
    viewModel: FireBaseViewModel,
    showOfflineToast: () -> Unit
) {

    val filteredOrders = orders.filter { order ->
        val isStatusMatch = when (selectedStatus) {
            "Pending" -> order.deliveryStatus == "Pending"
            "Completed" -> order.deliveryStatus == "Completed" || order.deliveryStatus == "Cancelled"
            else -> true // Corrected here
        }

        val isDateMatch = selectedDate?.let {
            val orderDate = LocalDateTime.ofInstant(
                order.expectedDeliveryDate!!.toDate().toInstant(),
                ZoneId.systemDefault()
            ).toLocalDate()
            orderDate == it
        } ?: true
        val isSearchMatch = searchQuery.isEmpty() ||
                order.userName?.contains(searchQuery, ignoreCase = true) == true ||
                order.deliveryAddress?.contains(searchQuery, ignoreCase = true) == true
        isStatusMatch && isDateMatch && isSearchMatch
    }

    when (selectedStatus) {
        "Pending" -> Log.d("pendingOrder", "$filteredOrders")
        "Completed" -> Log.d("CompletedOrder", "$filteredOrders")
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(filteredOrders) { order ->
            orderItem(order = order, viewModel, selectedDate,showOfflineToast = showOfflineToast)
        }
    }
}


@Composable
fun SearchBar(searchQuery: String, onSearchQueryChange: (String) -> Unit, modifier: Modifier) {
    OutlinedTextField(
        singleLine = true,
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
        placeholder = { Text("Search orders by customer name") },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DateFilter(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    onOpenDatePicker: () -> Unit
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        FilterChip(
            selected = selectedDate == LocalDate.now(),
            onClick = { onDateSelected(LocalDate.now()) },
            label = { Text("Today") }
        )
        FilterChip(

            selected = selectedDate == LocalDate.now().minusDays(1),
            onClick = { onDateSelected(LocalDate.now().minusDays(1)) },
            label = { Text("Yesterday") },

            )
        AssistChip(
            onClick = onOpenDatePicker,
            leadingIcon = {
                Icon(
                    painter = painterResource(com.example.admin.R.drawable.calender),
                    contentDescription = "Select Date",
                    Modifier.size(20.dp)
                )

            },
            label = { Text("Select Date") }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusFilter(
    selectedStatus: String,
    onStatusSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf("Pending", "Completed")
    val selectedTabIndex = tabs.indexOf(selectedStatus)

    Column(modifier = modifier) {
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, tabTitle ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { onStatusSelected(tabTitle) },
                    text = { Text(tabTitle) },
                )
            }

        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun orderItem(
    order: Order,
    viewModel: FireBaseViewModel,
    selectedDate: LocalDate?,
    showOfflineToast: () -> Unit
) {
    val context = LocalContext.current
    Log.d("currentorder", "current  is : $order")
    // Use a single formatter for date and time
    val dateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())

    // Convert Timestamp to LocalDateTime
    val expectedDeliveryDateTime = order.expectedDeliveryDate?.let { timestamp ->
        timestamp.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
    }

    // Extract LocalTime from LocalDateTime, or default to null
    val deliveryTime: LocalTime? = expectedDeliveryDateTime?.toLocalTime()

    // Get LocalDate from LocalDateTime, or default to null
    val deliveryDate: LocalDate? = expectedDeliveryDateTime?.toLocalDate()

    // Determine the delivery day string
    val deliveryDay = when {
        deliveryDate == null -> "Date not specified"
        deliveryDate == LocalDate.now() -> "Today"
        deliveryDate == LocalDate.now().minusDays(1) -> "Yesterday"
        else -> deliveryDate.format(DateTimeFormatter.ofPattern("EEEE", Locale.getDefault()))
    }

    // Format the delivery time or provide a default
    val formattedDeliveryTime = deliveryTime?.format(dateTimeFormatter) ?: "Time not specified"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.userName ?: "Unknown Customer",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(text = "#${order.orderId}", color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.LocationOn,
                    contentDescription = "Address",
                    modifier = Modifier.size(16.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = order.deliveryAddress ?: "Unknown Address", color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = com.example.admin.R.drawable.shopping_bag),
                    contentDescription = "Qty",
                    modifier = Modifier.size(16.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Normal: ${order.normalWaterQuantity ?: 0} | Cold: ${order.coldWaterQuantity ?: 0}",
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    painter = painterResource(id = com.example.admin.R.drawable.time),
                    contentDescription = "Delivery Time",
                    modifier = Modifier.size(16.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "$deliveryDay, $formattedDeliveryTime", color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Cane Returned: ${order.canesReturning}", color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))
            if (order.deliveryStatus == "Pending") {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    var showDialog by remember { mutableStateOf(false) }
                    var showConfirmDialog by remember { mutableStateOf(false) }

                    // cancel
                    Button(
                        onClick = {
                            showDialog = true
                        },
                        modifier = Modifier,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF97316))
                    ) {
                        Text(text = " Cancel", color = Color.White)
                    }
                    if (showDialog) {

                        AlertDialog(
                            onDismissRequest = { showDialog = false },
                            title = { Text("Cancel Order") },
                            text = { Text("Are you sure you want to cancel this order?") },
                            confirmButton = {
                                TextButton(onClick = {
                                    if (isNetworkAvailable(context)) {

                                        viewModel.updateOrderStatusUsingRef(
                                            order.userID!!,
                                            order.orderId ?: "",
                                            "Cancelled",
                                            {},
                                            {}
                                        )
                                        if (selectedDate == LocalDate.now()) {
                                            viewModel.getAllUsersTodayOrders(
                                                onSuccess = {},
                                                onFailure = {}
                                            )
                                        } else if (selectedDate == LocalDate.now().minusDays(1)) {
                                            viewModel.getAllUsersOrdersByCustomDate(
                                                selectedDate!!,
                                                {},
                                                {}
                                            )
                                        } else {
                                            viewModel.getAllUsersOrdersByCustomDate(
                                                selectedDate!!,
                                                {},
                                                {}
                                            )
                                        }
                                    }else{
                                        showOfflineToast()
                                    }

                                    showDialog = false
                                }) {
                                    Text("Confirm")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }

                    //Confirm
                    Button(
                        onClick = {
                            showConfirmDialog = true
                        },
                        modifier = Modifier,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(text = "Mark as Delivered", color = Color.White)
                    }

                    if (showConfirmDialog) {
                        AlertDialog(
                            onDismissRequest = { showConfirmDialog = false },
                            title = { Text("Confirm Delivery") },
                            text = { Text("Are you sure you want to mark this order as delivered?") },
                            confirmButton = {
                                TextButton(onClick = {
                                    if (isNetworkAvailable(context)) {

                                        val db: FirebaseFirestore = Firebase.firestore
                                        viewModel.updateOrderStatusUsingRef(
                                            order.userID!!,
                                            order.orderId ?: "",
                                            "Completed",
                                            {},
                                            {}
                                        )
                                        if (selectedDate == LocalDate.now()) {
                                            viewModel.getAllUsersTodayOrders(
                                                onSuccess = {},
                                                onFailure = {}
                                            )
                                        } else if (selectedDate == LocalDate.now().minusDays(1)) {
                                            viewModel.getAllUsersOrdersByCustomDate(
                                                selectedDate!!,
                                                {},
                                                {}
                                            )
                                        } else {
                                            viewModel.getAllUsersOrdersByCustomDate(
                                                selectedDate!!,
                                                {},
                                                {}
                                            )
                                        }



                                        viewModel.updateAmount(
                                            order.userID!!,
                                            order.totalAmount ?: 0.0,
                                            true,
                                            {
                                                Log.d(
                                                    "amountUpdated",
                                                    "amount is updated successfully "
                                                )
                                            },
                                            {
                                                Log.d(
                                                    "amountUpdated",
                                                    "amount is updated Unsuccessfully "
                                                )
                                            })
                                        // Assuming you have an instance of FireBaseViewModel called 'viewModel'
                                        viewModel.updateCanesTaken(
                                            userRef = order.userID!!,
                                            canesToAdd = (order.coldWaterQuantity!! + order.normalWaterQuantity!!),
                                            canesToSubtract = order.canesReturning ?: 0,
                                            onSuccess = {
                                                // Handle successful update (e.g., show a success message)
                                                Log.d("ViewModelUpdate", "canes update successful")
                                            },
                                            onFailure = { error ->
                                                // Handle failure (e.g., show an error message)
                                                Log.e(
                                                    "ViewModelUpdate",
                                                    "Failed to update canes: ${error.message}"
                                                )
                                            }
                                        )
                                    }else{
                                        showOfflineToast()
                                    }



                                    showConfirmDialog = false
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

            } else if (order.deliveryStatus == "Completed") {
                Button(
                    onClick = { /* Handle mark as delivered action */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(text = "Order Completed", color = Color.White)
                }
            } else {
                Button(
                    onClick = { /* Handle mark as delivered action */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF97316))
                ) {
                    Text(text = "Order Cancelled", color = Color.White)
                }
            }


        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDatePickerDialog(onDateSelected: (LocalDate) -> Unit, onDismiss: () -> Unit) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let {
                    val selectedDate = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                    onDateSelected(selectedDate)
                }
                onDismiss()

            }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun OrderScreenPreview() {
    // OrderScreen()
}