import android.graphics.Paint
import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.VerticalAlignmentLine
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.admin.presentation.FireBaseViewModel
import com.example.admin.presentation.ui.route
import com.google.firebase.Timestamp
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import org.koin.androidx.compose.koinViewModel

// Data class to represent a customer (aligned with Firebase User data)
data class Customer(
    val name: String?,
    val initials: String?,
    val email: String?,
    val userName: String?,
    val contactInfo: String?,
    val amount: Double?,
    val status: String?,
    val statusColor: Color,
    val profilePictureUrl: String?,
    val lastOrderDate: Timestamp?,
    val isActive: Boolean?,
    val monthlyUsage: List<Map<String, Any>>?,
    val isRecurringDelivery: Boolean?,
    val address: String?,
    val defaultJarSize: String?,
    val preferredDeliveryTime: String?,
    val pushNotificationsEnabled: Boolean?,
    val isStaff: Boolean?,
    val isCompany: Boolean?,
    val serviceName: String?,
    val coldWaterPrice: Double?,
    val regularWaterPrice: Double?,
    val isOpen: Boolean?,
    val depositMoney: Double?,
    val canesTaken: Int?,
    val canesReturned: Int?,
    val userId: String? // Add userId from Firebase
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersScreen(navController: NavController) {
    val viewModel = koinViewModel<FireBaseViewModel>()
    // Observe LiveData from ViewModel
    val allUsersState by viewModel.allUsers.collectAsState()
    Log.d("all Users original", "$allUsersState")


    LaunchedEffect(key1 = true) {
        viewModel.getAllUsers({}, {})

    }


    // Convert Firebase User to Customer
    val customers: List<Customer> = allUsersState.map { user ->
        val status = user.status ?: "Active"
        val statusColor = when (status) {
            "Overdue" -> Color.Red
            "Paid" -> Color.Green
            else -> Color.Gray
        }
        val initials = user.initial ?: ""

        Customer(
            name = user.name ?: "",
            initials = initials,
            email = user.email,
            userName = user.userName,
            contactInfo = user.contactInfo,
            amount = user.amount,
            status = status,
            statusColor = statusColor,
            profilePictureUrl = user.profilePictureUrl,
            lastOrderDate = user.lastOrderDate,
            isActive = user.isActive,
            monthlyUsage = user.monthlyUsage,
            isRecurringDelivery = user.isRecurringDelivery,
            address = user.address,
            defaultJarSize = user.defaultJarSize,
            preferredDeliveryTime = user.preferredDeliveryTime,
            pushNotificationsEnabled = user.pushNotificationsEnabled,
            isStaff = user.isStaff,
            isCompany = user.isCompany,
            serviceName = user.serviceName,
            coldWaterPrice = user.coldWaterPrice,
            regularWaterPrice = user.regularWaterPrice,
            isOpen = user.isOpen,
            depositMoney = user.depositMoney,
            canesTaken = user.canesTaken,
            canesReturned = user.canesReturned,
            userId = user.userId
        )
    }

    Log.d("all Users", "$customers")
    // State for search query
    var searchQuery by remember { mutableStateOf("") }

    // State for selected filter
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Recent", "Overdue", "Active")

    // Filtered customer list
    val filteredCustomers = remember(customers, searchQuery, selectedFilter) {
        customers.filter { customer ->
            val matchesSearchQuery = customer.name?.contains(searchQuery, ignoreCase = true) ?: false ||
                    customer.email?.contains(searchQuery, ignoreCase = true) ?: false ||
                    customer.contactInfo?.contains(searchQuery, ignoreCase = true) ?: false ||
                    customer.address?.contains(searchQuery, ignoreCase = true) ?: false

            val matchesFilter = when (selectedFilter) {
                "All" -> true
                "Recent" -> true
                "Overdue" -> customer.status == "Overdue"
                "Active" -> customer.status == "Active"
                else -> true
            }
            matchesSearchQuery && matchesFilter
        }
    }

    Scaffold(

        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Customers",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 48.dp)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack(route.dashBoardScreen, false)
                        selectedItem = 0
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {

            BottomNavigationBar(navController)

        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search customers...") },
                leadingIcon = { Icon(Icons.Filled.Search, "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Close, "Clear")
                        }
                    }

                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.LightGray,
                    unfocusedBorderColor = Color.LightGray,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    disabledTextColor = Color.Transparent,
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            // Total Customers Count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(text = "Total Customers: ${customers.size}", fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)

            ) {
                filters.forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) },
                        enabled = true, // Ensure the chip is enabled
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFE8E8E8),
                            containerColor = Color.Transparent,
                            selectedLabelColor = Color.Black,
                            labelColor = Color.Gray,
                        ),
                        border = if (selectedFilter == filter) {
                            FilterChipDefaults.filterChipBorder(
                                borderColor = Color.LightGray,
                                selectedBorderColor = Color.LightGray,
                                borderWidth = 1.dp,
                                enabled = true,
                                selected = selectedFilter == filter,

                                )
                        } else {
                            FilterChipDefaults.filterChipBorder(
                                borderColor = Color.LightGray,
                                selectedBorderColor = Color.Transparent,
                                borderWidth = 1.dp,
                                enabled = true,
                                selected = selectedFilter == filter,
                            )
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Customer List
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(filteredCustomers) { customer ->
                    CustomerItem(customer, navController)
                }
            }

        }

    }

}

@Composable
fun CustomerItem(customer: Customer, navController: NavController) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                Toast.makeText(context, "Opening ${customer.name} Profile", Toast.LENGTH_SHORT)
                    .show()
                navController.navigate(route.customerDetailScreen(customer.userId))
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Circle with Initials
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFE8F0FE)), // Light blue background
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = customer.initials ?: "",
                color = Color(0xFF007AFF),
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(16.dp))

        // Customer Info (Name, Contact)
        Column(modifier = Modifier.weight(1f)) {
            Text(text = customer.name ?: "", fontWeight = FontWeight.Bold, color = Color.Black)

            Text(text = customer.address ?: "No Address", color = Color.Gray, fontSize = 14.sp)



            Text(text = customer.contactInfo ?: "No Number", color = Color.Gray, fontSize = 14.sp)


        }

        // Amount and Status
        Column(horizontalAlignment = Alignment.End) {

            Text(
                text = "₹${customer.amount ?: "0"}",
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Text(text = customer.status ?: "", color = customer.statusColor, fontSize = 14.sp)
        }
    }
    Spacer(modifier = Modifier.height(10.dp))
}