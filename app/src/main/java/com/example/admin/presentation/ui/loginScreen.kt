package com.example.admin.presentation.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue



import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

import com.example.admin.R
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.admin.presentation.FireBaseViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, ) {
   val viewModel= koinViewModel <FireBaseViewModel>()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMeChecked by remember { mutableStateOf(false) }


    val isLoading by viewModel.isLoading.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            // Icon
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = "User Icon",
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0xFFD0E6FF), shape = RoundedCornerShape(50))
                    .padding(16.dp),
                tint = Color(0xFF0059D0)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Title
            Text(
                text = "Provider H...",
                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp),
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Welcome Text
            Text(
                text = "Welcome Back!",
                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 28.sp),
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = "Please log in to manage your operations",
                style = TextStyle(fontSize = 16.sp),
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Username or Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Username or Email") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Username or Email Icon"
                    )
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.lock),
                        contentDescription = "Password Icon"
                    )
                },
//                trailingIcon = {
//                    val icon = if (passwordVisible) Icons.Filled.ope else Icons.Filled.Visibility
//                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
//                        Icon(
//                            imageVector = icon,
//                            contentDescription = "Toggle Password Visibility"
//                        )
//                    }
//                }
            )
            // Remember Me and Forgot Password
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = rememberMeChecked, onCheckedChange = { rememberMeChecked = it })
                    Text(text = "Remember Me", style = TextStyle(fontSize = 14.sp))
                }
                TextButton(onClick = { /*TODO*/ }) {
                    Text(text = "Forgot Password?", style = TextStyle(fontSize = 14.sp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Login Button
            val context= LocalContext.current
            Button(
                onClick = {
                    viewModel.loginAdmin(email, password, onSuccess = {

                        Toast.makeText(context,"Login Successful",Toast.LENGTH_SHORT).show()
                        Log.d("LoginScreen", "Login Successful ")

                        if(!viewModel.isLoadingAdmin.value){
                            navController.navigate(route.dashBoardScreen)
                        }

                    }, onFailure = {
                        Toast.makeText(context,"Login Failed ",Toast.LENGTH_SHORT).show()
                        Log.d("LoginScreen", "Login Failed ")
                    })
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Log...", style = TextStyle(fontWeight = FontWeight.Bold))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Spacer(modifier = Modifier.weight(1f))

            // Security Text
            Text(
                text = "Keep your login details secure. Contact support if you face any issues.",
                style = TextStyle(fontSize = 12.sp),
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Image(painter = painterResource(id = R.drawable.line), contentDescription = "",modifier = Modifier
                .fillMaxWidth()
                .height(4.dp))
        }
    }
}

@Preview
@Composable
fun LoginScreenPreview() {
    val navController = androidx.navigation.compose.rememberNavController()
    LoginScreen(navController)
}
