package com.example.admin.presentation.ui

import User
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.admin.presentation.FireBaseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, viewModel: FireBaseViewModel) {
    val adminData by viewModel.provider.collectAsState()
    LaunchedEffect(key1 = true) {
        try {
        } catch (e: Exception) {
            // Handle error (e.g., show a toast)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", color = Color.Black, fontSize = 20.sp, fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "backIcon", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        content = { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(innerPadding)
                    .background(Color(0xFFF9F9F9)),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    ProfileHeader(
                        imageUrl = adminData?.profilePictureUrl?: "https://via.placeholder.com/150",
                        name = adminData?.name ?: "Loading...",
                        subtitle = adminData?.serviceName ?: "Loading..."
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
                item {
                    adminData?.let { BottlePricesSection(viewModel = viewModel, adminData = it) }
                    Spacer(modifier = Modifier.height(24.dp))
                }
                item {
                    adminData?.let { ContactDetailsSection(viewModel, it) }
                    Spacer(modifier = Modifier.height(24.dp))
                }
                item {
                    StatsSection()
                    Spacer(modifier = Modifier.height(32.dp))
                }
                item {
                    ActionsSection()
                }
            }
        }
    )
}

@Composable
fun ProfileHeader(imageUrl: String, name: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.size(100.dp)) {
            Image(
                painter = rememberAsyncImagePainter(imageUrl),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = "Verified",
                tint = Color(0xFF2196F3),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = name,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Text(text = subtitle, fontSize = 14.sp, color = Color.Gray)
    }
}

@Composable
fun BottlePricesSection(viewModel: FireBaseViewModel, adminData: User) {
    var regularWaterPrice by remember { mutableStateOf(adminData.regularWaterPrice) }
    var coldWaterPrice by remember { mutableStateOf(adminData.coldWaterPrice) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Bottle Prices",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(16.dp))
            PriceRow(
                label = "Regular Water",
                price = regularWaterPrice?.toInt() ?: 0,
                onIncrement = { regularWaterPrice = (regularWaterPrice ?: 0.0) + 1.0 },
                onDecrement = { if ((regularWaterPrice ?: 0.0) > 0.0) regularWaterPrice = (regularWaterPrice ?: 0.0) - 1.0 }
            )
            Spacer(modifier = Modifier.height(16.dp))
            PriceRow(
                label = "Cold Water",
                price = coldWaterPrice?.toInt() ?: 0,
                onIncrement = { coldWaterPrice = (coldWaterPrice ?: 0.0) + 1.0 },
                onDecrement = { if ((coldWaterPrice ?: 0.0) > 0.0) coldWaterPrice = (coldWaterPrice ?: 0.0) - 1.0 }
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    // Handle updating prices
                    val updatedAdminData = adminData.copy(regularWaterPrice = regularWaterPrice, coldWaterPrice = coldWaterPrice)
//                    viewModel.updateCurrentAdmin(adminData.userId!!, updatedAdminData)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E90FF)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(12.dp)
            ) {
                Text("Update Prices", color = Color.White, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun PriceRow(label: String, price: Int, onIncrement: () -> Unit, onDecrement: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 16.sp, color = Color.Black)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clickable { onDecrement() }
                    .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                    .padding(6.dp)
            ) {
                Text(
                    text = "-",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.LightGray
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "₹$price",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .clickable { onIncrement() }
                    .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                    .padding(6.dp)
            ) {
                Text(
                    text = "+",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.LightGray
                )
            }
        }
    }
}

@Composable
fun ContactDetailsSection(viewModel: FireBaseViewModel, adminData: User) {
    var isEditing by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var editedPhone by remember { mutableStateOf(adminData.contactInfo ?: "") }
    var editedEmail by remember { mutableStateOf(adminData.email ?: "") }
    var editedLocation by remember { mutableStateOf(adminData.address ?: "") }
    var editedTime by remember { mutableStateOf(adminData.preferredDeliveryTime ?: "") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Contact Details",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                IconButton(onClick = { isEditing = !isEditing }) {
                    Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit Contact Details")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (isEditing) {
                OutlinedTextField(
                    value = editedPhone,
                    onValueChange = { editedPhone = it },
                    label = { Text("Phone") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = editedEmail,
                    onValueChange = { editedEmail = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = editedLocation,
                    onValueChange = { editedLocation = it },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = editedTime,
                    onValueChange = { editedTime = it },
                    label = { Text("Time") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { showConfirmDialog = true }) {
                    Text("Confirm Changes")
                }
            } else {
                ContactRow(icon = Icons.Filled.Phone, text = editedPhone)
                Spacer(modifier = Modifier.height(12.dp))
                ContactRow(icon = Icons.Filled.Email, text = editedEmail)
                Spacer(modifier = Modifier.height(12.dp))
                ContactRow(icon = Icons.Filled.LocationOn, text = editedLocation)
                Spacer(modifier = Modifier.height(12.dp))
                ContactRow(icon = Icons.Filled.DateRange, text = editedTime)
            }
        }
    }
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Changes") },
            text = { Text("Are you sure you want to save these changes?") },
            confirmButton = {
                TextButton(onClick = {
                    val updatedAdminData = adminData.copy(
                        contactInfo = editedPhone,
                        email = editedEmail,
                        address = editedLocation,
                        preferredDeliveryTime = editedTime
                    )
                    viewModel.updateUser(updatedAdminData,{},{})
                    showConfirmDialog = false
                    isEditing = false
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
fun ContactRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Contact Icon",
            tint = Color.Gray,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, fontSize = 14.sp, color = Color.Gray)
    }
}

@Composable
fun StatsSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            StatsItem(count = "2,450", label = "Delivered")
            StatsItem(count = "₹45K", label = "Revenue", icon = Icons.Filled.Star)
            StatsItem(count = "4.5", label = "Rating")
        }
    }
}

@Composable
fun StatsItem(
    count: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = count,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color(0xFFF97316),
                    modifier = Modifier
                        .size(18.dp)
                        .padding(start = 4.dp)
                )
            }
        }
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ActionsSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = { /* Handle View Orders */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E90FF)),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(12.dp)
        ) {
            Text("View Orders", color = Color.White, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { /* Handle Update Availability */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(12.dp),
        ) {
            Text("Update Availability", color = Color(0xFF1E90FF), fontSize = 16.sp)
        }
    }
}