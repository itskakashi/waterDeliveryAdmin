

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.admin.presentation.FireBaseViewModel
import com.example.admin.presentation.screens.CustomerDetailScreen
import com.example.admin.presentation.screens.OrderScreen
import com.example.admin.presentation.screens.PaymentScreen
import com.example.admin.presentation.ui.LoginScreen
import com.example.admin.presentation.ui.route
import com.google.firebase.auth.FirebaseAuth
import org.koin.androidx.compose.koinViewModel


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun navigation(){
    val viewModel = koinViewModel<FireBaseViewModel>()
    val navHostController = rememberNavController()
    val firebaseAuth = FirebaseAuth.getInstance()

    NavHost(navController = navHostController,startDestination =
        if (firebaseAuth.currentUser != null) {
        val currentUser = firebaseAuth.currentUser!!.uid

        viewModel.updateUserId(currentUser)
            viewModel.updateCurrentAdmin(currentUser)

        route.dashBoardScreen

    } else {
        Log.d("Navigation", "no User: ")
        route.loginScreen // User needs to log in
    }

    ){
        composable<route.dashBoardScreen> {
DashboardScreen(navHostController,viewModel)
        }
        composable<route.orderPlaceScreen>{
           NewOrderScreen(navHostController,viewModel)
        }
        composable<route.customerScreen>{
       CustomersScreen(navHostController)
        }
        composable<route.loginScreen> {
            LoginScreen(navHostController)
        }

        composable<route.customerDetailScreen> {
            val userId = it.arguments?.getString("userId")
            CustomerDetailScreen(
                userId,
                viewModel = viewModel,

            )
        }
        composable<route.orderScreen> {
            OrderScreen(navHostController,viewModel)
        }

        composable<route.paymentScreen> {
            PaymentScreen(viewModel,navHostController)
        }

    }
}
