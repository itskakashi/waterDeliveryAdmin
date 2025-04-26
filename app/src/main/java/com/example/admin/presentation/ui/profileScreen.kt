package com.example.admin.presentation.ui

import BottomNavigationBar
import User
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomAppBarState
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
import selectedItem

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
                title = {
                    Text(
                        "Admin Profile",
                        color = Color.Black,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                ,
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack(route.dashBoardScreen,false)
                        selectedItem=0
                    }) {
                        Icon(Icons.Filled.ArrowBack, "backIcon", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = { BottomNavigationBar(navController)
                    selectedItem=4
                    }
        ,
        content = { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color(0xFFF9F9F9))
                    .padding(16.dp), // Add padding to the entire content
                verticalArrangement = Arrangement.spacedBy(16.dp) // Add spacing between items
            ) {
                item {
                    ProfileHeader(
                        imageUrl = adminData?.profilePictureUrl
                            ?: "https://via.placeholder.com/150",
                        name = adminData?.name ?: "Loading...",
                        subtitle = adminData?.serviceName ?: "Loading..."
                    )
                }
                item {
                    adminData?.let { AdminDetailsSection(it) }
                }
                item {
                    adminData?.let { ContactDetailsSection(viewModel, it) }
                }
                item {
                    Divider(color = Color.LightGray, thickness = 1.dp)
                }

            }
        }
    )
}

@Composable
fun ProfileHeader(imageUrl: String, name: String, subtitle: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                color = Color.Black,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AdminDetailsSection(adminData: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Admin Details",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(16.dp))
            DetailRow(label = "Username", value = adminData.userName ?: "Not set")
            Spacer(modifier = Modifier.height(8.dp))
            DetailRow(label = "Address", value = adminData.address ?: "Not set")
            Spacer(modifier = Modifier.height(8.dp))
            DetailRow(label = "Email", value = adminData.email ?: "Not set")
            Spacer(modifier = Modifier.height(8.dp))
            DetailRow(label = "Service Name", value = adminData.serviceName ?: "Not set")
            Spacer(modifier = Modifier.height(8.dp))
            DetailRow(label = "Initial", value = adminData.initial ?: "Not set")
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = value, color = Color.Gray, fontSize = 14.sp)
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
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit Contact Details"
                    )
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
                    viewModel.updateUser(updatedAdminData, {}, {})
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