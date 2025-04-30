package com.example.admin



import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.navigation.compose.rememberNavController
import com.example.admin.presentation.screens.OrderScreen
import com.example.admin.presentation.ui.route
import com.example.admin.ui.theme.AdminTheme
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.firestoreSettings
import navigation

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        Firebase.firestore.firestoreSettings = firestoreSettings {
            setPersistenceEnabled(true)
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController= rememberNavController()
            AdminTheme {
                navigation()
            }
        }
    }
}

