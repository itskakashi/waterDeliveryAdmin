package com.example.admin.presentation.screens

import BottomNavigationBar
import Payment
import User
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.admin.R
import com.example.admin.presentation.FireBaseViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Date

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(viewModel: FireBaseViewModel, navController: NavController) {
    var selectedCustomer by remember { mutableStateOf<User?>(null) }
    var amountPaying by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var isDatePickerOpen by remember { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }

    val allUsers by viewModel.allUsers.collectAsState()
    val context = LocalContext.current
    val db: FirebaseFirestore = Firebase.firestore

    LaunchedEffect(Unit) {
        viewModel.getAllUsers({}, {})
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Payment", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Customer Dropdown
            CustomerDropdown(
                allUsers = allUsers,
                onCustomerSelected = { selectedCustomer = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Pending Amount
            OutlinedTextField(
                value = selectedCustomer?.amount?.toString() ?: "",
                onValueChange = { },
                label = { Text("Pending Amount") },
                leadingIcon = { Text("₹") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                shape = RoundedCornerShape(16.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Amount Paying
            OutlinedTextField(
                value = amountPaying,
                onValueChange = { amountPaying = it },
                label = { Text("Amount Paying") },
                leadingIcon = { Text("₹") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Payment Date
            OutlinedTextField(
                value = selectedDate.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                onValueChange = { },
                label = { Text("Payment Date") },
                trailingIcon = {
                    IconButton(onClick = { isDatePickerOpen = true }) {
                        Icon(
                            painter = painterResource(R.drawable.calender),
                            contentDescription = "Select Date"
                        )
                    }
                },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    if (selectedCustomer != null && amountPaying.isNotEmpty()
                        && amountPaying.toDouble() > 0.0
                        && amountPaying.toDouble() <= (selectedCustomer?.amount ?: 0.0)
                    ) {
                        showConfirmationDialog = true // Show confirmation dialog
                    } else {
                        Toast.makeText(context, "Invalid Amount", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
            ) {
                Text(text = "Add Payment", color = Color.White)
            }
        }
    }
    // Confirmation Dialog
    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = { Text("Confirm Payment") },
            text = {
                Column {
                    Text("Customer: ${selectedCustomer?.name}")
                    Text("Pending Amount: ₹${selectedCustomer?.amount ?: "0.0"}")
                    Text("Amount Paying: ₹$amountPaying")
                }
            },
            confirmButton = {
                val db: FirebaseFirestore = Firebase.firestore
                val userRef = db.collection("users").document(selectedCustomer?.userId ?: "")
                TextButton(onClick = {
                    // Perform the actual payment update
                    val payment = Payment(
                        userId = userRef,
                        paymentAmount = amountPaying.toDouble(),
                        paymentDate = Timestamp(Date()),
                        paymentMethod = "Cash" // You can change this
                    )
                    viewModel.recordPayment(payment, { paymentId ->
                        viewModel.updateAmount(
                            db.collection("users").document(selectedCustomer?.userId ?: ""),
                            amountPaying.toDouble(), false, {
                                Toast.makeText(context, "Payment Successful", Toast.LENGTH_SHORT)
                                    .show()
                                navController.popBackStack()
                            }, {
                                Toast.makeText(context, "Payment Failed", Toast.LENGTH_SHORT).show()
                            })
                        showConfirmationDialog = false
                    }, {
                        Toast.makeText(context, "Payment Failed", Toast.LENGTH_SHORT).show()
                    })

                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmationDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (isDatePickerOpen) {
        CustomDatePickerDialogg(
            onDateSelected = { selectedDate = it; isDatePickerOpen = false },
            onDismiss = { isDatePickerOpen = false }
        )
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

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDatePickerDialogg(onDateSelected: (LocalDate) -> Unit, onDismiss: () -> Unit) {
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
fun PaymentScreenPreview() {
    // PaymentScreen()
}