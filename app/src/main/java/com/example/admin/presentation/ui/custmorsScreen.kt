import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Data class to represent a customer
data class Customer(
    val name: String,
    val initials: String,
    val phone: String?,
    val email: String?,
    val balance: String,
    val status: String,
    val statusColor: Color,
    val address:String?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersScreen() {
    // Sample customer data (replace with your actual data source)
    val customers = remember {
        listOf(
            Customer("Sarah Johnson", "SJ", "+1 (555) 123-4567", null, "$1,250", "Overdue", Color.Red, address = "mauganj"),
            Customer("Michael Chen", "MC", "9200212857", "michael@email.com", "$850", "Paid", Color.Green, address = "patel nagar"),
            Customer("Emma Williams", "EW", "+1 (555) 987-6543", null, "$2,100", "Overdue", Color.Red, address = "bhopal"),
            Customer("David Brown", "DB", null, "david.b@email.com", "$450", "Paid", Color.Green, address = "indore"),
            Customer("Lisa Anderson", "LA", "+1 (555) 246-8135", null, "$1,750", "Paid", Color.Green, address = "delhi")
        )
    }

    // State for search query
    var searchQuery by remember { mutableStateOf("") }

    // State for selected filter
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Recent", "Overdue", "Active")

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
                    IconButton(onClick = { /* Handle back action */ }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Button(
                onClick = { /* Handle add new customer */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF))
            ) {
                Text("Add New Customer", color = Color.White, fontSize = 16.sp)
            }
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
                singleLine = true
                ,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.LightGray,
                    unfocusedBorderColor = Color.LightGray,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    disabledTextColor = Color.Transparent,
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

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
                items(customers) { customer ->
                    CustomerItem(customer)
                }
            }
        }
    }
}

@Composable
fun CustomerItem(customer: Customer) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFE8F0FE)), // Light blue background
            contentAlignment = Alignment.Center
        ) {
            Text(text = customer.initials, color = Color(0xFF007AFF)) // Blue initials
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = customer.name, fontWeight = FontWeight.Bold)
            if (customer.address != null) {
                Text(text = customer.address, color = Color.Gray)
            }
            if (customer.email != null) {
                Text(text = customer.email, color = Color.Gray)
            }
            if (customer.phone != null) {
                Text(text = customer.phone, color = Color.Gray)
            }


        }

        Column(horizontalAlignment = Alignment.End) {
            Text(text = customer.balance, fontWeight = FontWeight.Bold)
            Text(text = customer.status, color = customer.statusColor)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CustomersScreenPreview() {
    CustomersScreen()
}