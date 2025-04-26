package com.example.admin.presentation.ui

import User
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.admin.presentation.FireBaseViewModel
import kotlinx.coroutines.launch

// Color Constants
private val BluePrimary = Color(0xFF1E90FF)
private val GreyLight = Color.LightGray
private val RedError = Color.Red

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomerScreen(navController: NavController, viewModel: FireBaseViewModel) {
    // State variables for form fields and loading/error states
    var customerName by remember { mutableStateOf(TextFieldValue("")) }
    var phoneNumber by remember { mutableStateOf(TextFieldValue("")) }
    var emailAddress by remember { mutableStateOf(TextFieldValue("")) }
    var address by remember { mutableStateOf(TextFieldValue("")) }
    var notes by remember { mutableStateOf(TextFieldValue("")) }
    var depositedMoney by remember { mutableStateOf(TextFieldValue("")) }
    var normalWaterPrice by remember { mutableStateOf(TextFieldValue("")) }
    var coldWaterPrice by remember { mutableStateOf(TextFieldValue("")) }
    var isLoading by remember { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }

    // Error states
    var customerNameError by remember { mutableStateOf<String?>(null) }
    var phoneNumberError by remember { mutableStateOf<String?>(null) }
    var addressError by remember { mutableStateOf<String?>(null) }
    var depositedMoneyError by remember { mutableStateOf<String?>(null) }
    var normalWaterPriceError by remember { mutableStateOf<String?>(null) }
    var coldWaterPriceError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    //clear the errors state after the user change the data
    LaunchedEffect(customerName.text) {
        customerNameError = null
    }
    LaunchedEffect(phoneNumber.text) {
        phoneNumberError = null
    }
    LaunchedEffect(address.text) {
        addressError = null
    }
    LaunchedEffect(depositedMoney.text) {
        depositedMoneyError = null
    }
    LaunchedEffect(normalWaterPrice.text) {
        normalWaterPriceError = null
    }
    LaunchedEffect(coldWaterPrice.text) {
        coldWaterPriceError = null
    }

    // confirmation dialog
    if (showConfirmationDialog) {
        ConfirmationDialog(
            customerName = customerName.text,
            phoneNumber = phoneNumber.text,
            emailAddress = emailAddress.text,
            address = address.text,
            notes = notes.text,
            depositedMoney = depositedMoney.text,
            normalWaterPrice = normalWaterPrice.text,
            coldWaterPrice = coldWaterPrice.text,
            onConfirm = {
                isLoading = true
                val newUser = User(
                    name = customerName.text,
                    contactInfo = phoneNumber.text,
                    email = if(emailAddress.text.isNullOrEmpty())null else emailAddress.text,
                    address = address.text,
                    depositMoney = depositedMoney.text.toDouble(),
                    regularWaterPrice = normalWaterPrice.text.toDouble(),
                    coldWaterPrice=coldWaterPrice.text.toDouble(),
                )
                viewModel.addCustomerWithoutAuth(
                    user = newUser,

                    onSuccess = { userId ->
                        isLoading = false
                        // Handle success, e.g., show a Toast and navigate back
                        Log.d("customer successfull", "success to add customer: ${userId}")
                        navController.popBackStack()
                    },
                    onFailure = { exception ->
                        isLoading = false
                        // Handle failure, e.g., show an error Toast
                        Log.d("customer failed", "Failed to add customer: ${exception.message}")

                        scope.launch {
                            snackbarHostState.showSnackbar(
                                "Failed to add customer: ${exception.message}"
                            )
                        }
                    }
                )
                showConfirmationDialog = false // Dismiss the dialog after confirmation
            },
            onDismiss = { showConfirmationDialog = false }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Add Customer",
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .background(MaterialTheme.colorScheme.background),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CustomerInformationSection()
                Spacer(modifier = Modifier.height(16.dp))
                // Input Fields
                InputField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = "Customer Name",
                    icon = Icons.Filled.Person,
                    isRequired = true,
                    errorMessage = customerNameError,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        capitalization = KeyboardCapitalization.Words
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                InputField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = "Phone Number",
                    icon = Icons.Filled.Phone,
                    isRequired = true,
                    errorMessage = phoneNumberError,
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone)

                )

                Spacer(modifier = Modifier.height(16.dp))

                InputField(
                    value = emailAddress,
                    onValueChange = { emailAddress = it },
                    label = "Email Address (optional)",
                    icon = Icons.Filled.Email,
                    isRequired = false
                )

                Spacer(modifier = Modifier.height(16.dp))

                InputField(
                    value = address,
                    onValueChange = { address = it },
                    label = "Address",
                    icon = Icons.Filled.LocationOn,
                    isRequired = true,
                    errorMessage = addressError
                )

                Spacer(modifier = Modifier.height(16.dp))
                InputField(
                    value = depositedMoney,
                    onValueChange = { depositedMoney = it },
                    label = "Deposited Money",
                    icon = Icons.Filled.Star,
                    isRequired = true,
                    errorMessage = depositedMoneyError,
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(16.dp))

                InputField(
                    value = normalWaterPrice,
                    onValueChange = { normalWaterPrice = it },
                    label = "Normal Water Price",
                    icon = Icons.Filled.Edit,
                    isRequired = true,
                    errorMessage = normalWaterPriceError,
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(16.dp))

                InputField(
                    value = coldWaterPrice,
                    onValueChange = { coldWaterPrice = it },
                    label = "Cold Water Price",
                    icon = Icons.Filled.Edit,
                    isRequired = true,
                    errorMessage = coldWaterPriceError,
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(16.dp))

                InputField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = "Notes",
                    icon = Icons.Filled.Menu,
                    isRequired = false
                )

                Spacer(modifier = Modifier.weight(1f))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = {
                            customerNameError = null
                            phoneNumberError = null
                            addressError = null
                            depositedMoneyError = null
                            normalWaterPriceError= null
                            coldWaterPriceError = null
                            // Validate the required fields
                            if (customerName.text.isBlank()) {
                                customerNameError = "Customer Name is required"
                            }
                            if (phoneNumber.text.isBlank()) {
                                phoneNumberError = "Phone Number is required"
                            }
                            if (address.text.isBlank()) {
                                addressError = "Address is required"
                            }
                            if(depositedMoney.text.isBlank()) {
                                depositedMoneyError = "Deposited Money is required"
                            }
                            else if(depositedMoney.text.isNotBlank() && depositedMoney.text.toDoubleOrNull() == null) {
                                depositedMoneyError = "Invalid amount"
                            }
                            if(normalWaterPrice.text.isBlank()) {
                                normalWaterPriceError = "Normal water price is required"
                            }
                            else if(normalWaterPrice.text.isNotBlank() && normalWaterPrice.text.toDoubleOrNull() == null) {
                                normalWaterPriceError = "Invalid amount"
                            }
                            if(coldWaterPrice.text.isBlank()) {
                                coldWaterPriceError = "Cold water price is required"
                            }
                            else if(coldWaterPrice.text.isNotBlank() && coldWaterPrice.text.toDoubleOrNull() == null) {
                                coldWaterPriceError = "Invalid amount"
                            }
                            if (customerNameError != null || phoneNumberError != null || addressError != null || depositedMoneyError != null|| normalWaterPriceError != null||coldWaterPriceError!=null) {
                                return@Button
                            }

                            showConfirmationDialog = true
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isLoading // Disable button when loading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White)
                        } else {
                            Text("Save Customer", color = Color.White)
                        }
                    }
                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = GreyLight),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel", color = Color.Black)
                    }


                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isRequired: Boolean,
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            label = {
                Row {
                    Text(text = label)
                    if (isRequired) {
                        Text(text = "*", color = RedError)
                    }
                }
            },
            leadingIcon = { Icon(icon, contentDescription = label) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            isError = errorMessage != null, // Show error state if there's an error
            keyboardOptions = keyboardOptions,
            trailingIcon = {
                if (errorMessage != null) {
                    IconButton(onClick = { onValueChange(TextFieldValue("")) }) {
                        Icon(imageVector = Icons.Filled.Clear, contentDescription = "Clear")
                    }
                }
            }
        )
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = RedError,
                style = TextStyle(fontSize = 12.sp),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun ColumnScope.CustomerInformationSection() {
    Text(
        text = "Customer Information",
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = Color.Black,
        modifier = Modifier.fillMaxWidth()
    )
    Text(
        text = "* Required fields",
        fontSize = 12.sp,
        color = Color.Gray,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun ConfirmationDialog(
    customerName: String,
    phoneNumber: String,
    emailAddress: String,
    address: String,
    notes: String,
    depositedMoney: String,
    normalWaterPrice:String,
    coldWaterPrice:String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Customer Details") },
        text = {
            Column {
                Text("Customer Name: $customerName")
                Text("Phone Number: $phoneNumber")
                if (emailAddress.isNotBlank()) {
                    Text("Email: $emailAddress")
                }
                Text("Address: $address")
                Text("Deposited Money: $depositedMoney")

                Text("Normal water price : $normalWaterPrice")

                Text("Cold water price : $coldWaterPrice")
                if (notes.isNotBlank()) {
                    Text("Notes: $notes")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}