import AdminUser
import Analytics
import Bill
import Order
import Payment
import User
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.runtime.traceEventEnd
import androidx.room.util.TableInfo
import com.example.admin.R
import com.example.admin.presentation.FireBaseViewModel
import com.example.admin.presentation.ui.MyColor
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flow
import org.koin.androidx.compose.koinViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Date
import kotlin.text.contains

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewOrderScreen() {
    val viewModel = koinViewModel<FireBaseViewModel>()

    var selectedJar by remember { mutableIntStateOf(0) }
    var Quantity by remember { mutableIntStateOf(1) }
    var selectedCustomer by remember { mutableStateOf<User?>(null) }
    var selectedDate by remember { mutableStateOf<Date?>(null) }
    var returnCanCount by remember { mutableIntStateOf(0) }

    var isMarkedAsDelivered by remember { mutableStateOf(false) }
    var isMarkedAsPaid by remember { mutableStateOf(false) }

    val allUsers by viewModel.allUsers.collectAsState()

    LaunchedEffect(key1 = true) {
        viewModel.getAllUsers(onSuccess = {
            Log.d("users", it.toString())
        }) {
            Log.d("users", "can not get ${it.message}")
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
//                navigationIcon = {
//                    IconButton(onClick = { navController.popBackStack() }) {
//                        Icon(Icons.Filled.ArrowBack, "Back", tint = Color.Black)
//                    }
//                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = { BottomNavigationBar() }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            StandardDeliveryButton()
            Spacer(modifier = Modifier.height(24.dp)) // Increased spacing
            CustomerDropdown(allUsers = allUsers, onCustomerSelected = { selectedCustomer = it })
            Spacer(modifier = Modifier.height(8.dp))
            AddNewCustomerLink()
            Spacer(modifier = Modifier.height(24.dp)) // Increased spacing
            DeliveryAddressField(selectedCustomer= selectedCustomer)
            Spacer(modifier = Modifier.height(24.dp)) // Increased spacing
            JarTypeSegmentedButton(onValueChange = { selectedJar = it })
            Spacer(modifier = Modifier.height(24.dp)) // Increased spacing
            QuantityCounter(onValueChange = { Quantity = it })
            Spacer(modifier = Modifier.height(24.dp)) // Increased spacing
            ReturnCanCounter(onValueChange = { returnCanCount = it })
            Spacer(modifier = Modifier.height(24.dp)) // Increased spacing
            DatePickerField(onDateSelected = { selectedDate = it })
            Spacer(modifier = Modifier.height(24.dp)) // Increased spacing
            MarkAsDeliveredCheckbox(
                isChecked = isMarkedAsDelivered,
                onCheckedChange = { isMarkedAsDelivered = it }
            )
            Spacer(modifier = Modifier.height(24.dp)) // Increased spacing
            MarkAsPaymentCheckbox(
                isChecked = isMarkedAsPaid,
                onCheckedChange = { isMarkedAsPaid = it }
            )
            Spacer(modifier = Modifier.height(24.dp))
            Spacer(modifier = Modifier.weight(1f)) // Push the button to the bottom
            CreateOrderButton(
                selectedCustomer = selectedCustomer,
                address = selectedCustomer?.address?:"",
                selectedJar = selectedJar,
                Quantity = Quantity,
                selectedDate = selectedDate,
                isMarkedAsDelivered = isMarkedAsDelivered,
                isMarkedAsPaid = isMarkedAsPaid,
                returnCanCount=returnCanCount,
                viewModel = viewModel,
//                navController = navController
            )
        }
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
                painter = painterResource(id = R.drawable.standarddelivery), // Replace with your icon
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

    // Search-related state
    var searchQuery by remember { mutableStateOf("") }
    var filteredUsers by remember { mutableStateOf(allUsers) }


    LaunchedEffect(searchQuery, allUsers) {
        if(allUsers.isNotEmpty()){
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
                modifier = Modifier
                    .fillMaxWidth(),
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
                // Search field in DropdownMenu
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    placeholder = { Text("Search by name or address") }
                )

                if (filteredUsers.isEmpty()) {
                    DropdownMenuItem(text = { Text("No results found") }, onClick = {})
                } else {

                    filteredUsers.forEach { customer ->
                        DropdownMenuItem(
                            text = {   Column {
                                Text(text = customer.name ?: "", fontWeight = FontWeight.Bold)
                                Text(text = customer.address ?: "", color = Color.Gray, fontSize = 12.sp)
                            } },
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
        fontSize = 16.sp, // Match font size to image
        modifier = Modifier.clickable { /* Handle add new customer action */ }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryAddressField( selectedCustomer: User?) {

    Log.d("address", "DeliveryAddressField: ${selectedCustomer?.address?:""}")
    Text(
        text = "Delivery Address",
        color = Color.Black,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = selectedCustomer?.address?: "",
        readOnly = true,
        onValueChange = {
        },
        placeholder = { Text("Enter delivery address", color = Color.Gray) }, // Gray placeholder
        trailingIcon = {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                "Delivery Address",
                tint = Color.Gray
            )
        }, // Gray icon

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
fun JarTypeSegmentedButton(onValueChange: (Int) -> Unit) {
    var selectedIndex by remember { mutableIntStateOf(0) }
    val options = listOf("Normal", "Cold")

    Column {
        Text(
            text = "Jar Type",
            color = Color.Black,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            options.forEachIndexed { index, label ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                    onClick = {
                        selectedIndex = index
                        onValueChange(selectedIndex)
                    },
                    selected = selectedIndex == index,
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = Color(0xFF007AFF),
                        inactiveContainerColor = Color(0xFFF2F2F7), // Correct light gray
                        activeContentColor = Color.White,
                        inactiveContentColor = Color.Black
                    )
                ) {
                    Text(
                        text = label,
                        color = if (selectedIndex == index) Color.White else Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun QuantityCounter(onValueChange: (Int) -> Unit) {
    var count by remember { mutableIntStateOf(1) }
    Column {

        Text(
            "Quantity",
            color = Color.Black,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE5E7EB), shape = RoundedCornerShape(10.dp)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (count > 1) count--
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
                .background(Color(0xFFE5E7EB), shape = RoundedCornerShape(10.dp)),
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
fun DatePickerField(onDateSelected: (Date) -> Unit) {
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDateText by remember { mutableStateOf("") }

    val currentDate = LocalDate.now()
    val currentMillis = currentDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = currentMillis)

    // Automatically set the current date when the composable launches
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
                            selectedDateText = dateText.substring(0,10)

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
fun MarkAsDeliveredCheckbox(isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
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
            Checkbox(checked = isChecked, onCheckedChange = onCheckedChange)
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
fun MarkAsPaymentCheckbox(isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
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
            Checkbox(checked = isChecked, onCheckedChange = onCheckedChange)
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




@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CreateOrderButton(
    selectedCustomer: User?,
    address: String,
    selectedJar: Int,
    Quantity: Int,
    selectedDate: Date?,
    isMarkedAsDelivered: Boolean,
    isMarkedAsPaid: Boolean,
    returnCanCount:Int,
    viewModel: FireBaseViewModel,
//    navController: NavController
) {
    Button(
        onClick = {
            if (selectedCustomer != null && selectedDate != null && Quantity>0) {
                val db = Firebase.firestore
                val userRef = db.collection("users").document(selectedCustomer.userId!!)
                val order = Order(
                    userID = userRef,
                    deliveryAddress = address,
                    waterType = if (selectedJar == 0) "Normal" else "Cold",
                    quantity = Quantity,
                    expectedDeliveryDate = Timestamp(selectedDate),
                    isDelivered = isMarkedAsDelivered,
                    orderDate = Timestamp.now().toString(),
                    totalAmount = (Quantity * (if (selectedJar == 0) 30 else 35)).toDouble(),
                    deliveryStatus = if(isMarkedAsDelivered)"Completed" else "Pending",
                    canesReturning = returnCanCount,

                    )
                viewModel.createOrder(selectedCustomer.userId!!, order, onSuccess = {
                    Log.d("order", "order created successful ${it.toString()}")
//
                    val db: FirebaseFirestore = Firebase.firestore

                    val testBill = Bill(
                        billId = "testBill_${selectedCustomer.userId ?:""}_${System.currentTimeMillis()}",
                        userId = db.collection("users").document(selectedCustomer.userId ?:""),
                        amount = Quantity.toDouble() *(if (selectedJar == 0) 30 else 35 ).toDouble(),
                        totalJars = Quantity,
                        billDate = Timestamp(java.util.Date()),
                        paymentStatus = if (isMarkedAsPaid == true) "Paid" else "UnPaid",
                        isPaid = isMarkedAsPaid,
                        orderId = db.collection("orders").document(),

                        )

                    viewModel.createBill(testBill,
                        onSuccess = {
                            Log.d("billGenerated"," bill is created successfully ${it.toString()}")
                        },
                        onFailure = { e ->
                            Log.d("billGeneratedFailed"," bill is created Unsuccessfully ${it.toString()}")

                        })



                }) {
                    Log.d("order", "can not create an order ${it.message}")
                }

            } else {
                Log.d("order", "can not create an order")
            }



        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF))
    ) {
        Text(text = "Create Order", color = Color.White, fontSize = 16.sp)
    }
}