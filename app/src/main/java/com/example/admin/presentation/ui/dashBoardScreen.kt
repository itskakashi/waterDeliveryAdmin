import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.admin.R

import com.example.admin.presentation.FireBaseViewModel
import com.example.admin.presentation.ui.MyColor
import com.example.admin.presentation.ui.route
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

var selectedItem by mutableIntStateOf(0)

@Composable
fun DashboardScreen(navController: NavController, viewModel: FireBaseViewModel) {
    val admin by viewModel.provider.collectAsState()
    Log.d("admin", "${admin}")
    LaunchedEffect(Unit) {
        viewModel.updateCurrentAdmin(viewModel.userid.value)
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MyColor.background)
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            GreetingSection(admin)
            Spacer(modifier = Modifier.height(24.dp))
            DashboardGrid()
            Spacer(modifier = Modifier.height(60.dp))
            ButtonGrid(navController)
        }
    }

    Log.d("viewmdeladmin", "${viewModel.provider.value}")
    Log.d("adminDashboard", "${admin}")
}

@Composable
fun GreetingSection(admin: User?) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Good Morning,",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "${ admin?.name }",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault()).format(Date()),
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}

data class InfoCard(
    val title: String,
    val count: String,
    val percentage: String,
    @DrawableRes val iconResId: Int, // Change to Int (drawable resource ID)
    val color: Color,
)

@Composable
fun DashboardGrid() {
    val infoCards = listOf(
        InfoCard(
            "Today Orders",
            "12",
            "+15%",
            R.drawable.shoppingcart,
            Color(0xFF6200EE)
        ), // Replace with your icons
        InfoCard(
            "Total Revenue",
            "\$1,248",
            "+8%",
            R.drawable.wallet,
            Color(0xFF00897B)
        ), // Replace with your icons
        InfoCard(
            "Active Orders",
            "5",
            "-2%",
            R.drawable.deliverybus,
            Color(0xFFE64A19)
        ), // Replace with your icons
        InfoCard(
            "Pending Orders",
            "5",
            "-2%",
            R.drawable.deliverybus,
            Color(0xFF1976D2)
        ), // Replace with your icons
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(infoCards) { card ->
            InfoCardView(card = card)
        }
    }
}

@Composable
fun InfoCardView(card: InfoCard) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = card.iconResId), // Use painterResource
                contentDescription = null, // Add a proper content description if needed
                colorFilter = ColorFilter.tint(card.color),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = card.title, fontSize = 14.sp, color = Color.Gray)
                Text(text = card.count, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(text = card.percentage, fontSize = 12.sp, color = card.color)
            }
        }
    }
}

data class ButtonInfo(
    val title: String,
    @DrawableRes val iconResId: Int, // Change to Int (drawable resource ID)
)

@Composable
fun ButtonGrid(navController: NavController) {
    val buttonInfos = listOf(
        ButtonInfo("New Order", R.drawable.shoppingcart), // Replace with your icons
        ButtonInfo("Add Payment", R.drawable.payment), // Replace with your icons
        ButtonInfo("Add Customer", R.drawable.pluscustmer), // Replace with your icons
        ButtonInfo("Today's Orders", R.drawable.todayorder), // Replace with your icons
        ButtonInfo("Customer List", R.drawable.custmerlist), // Replace with your icons
        ButtonInfo("Reports", R.drawable.report), // Replace with your icons
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(buttonInfos) { buttonInfo ->
            ButtonView(buttonInfo = buttonInfo, navController = navController)
        }
    }
}

@Composable
fun ButtonView(buttonInfo: ButtonInfo, navController: NavController) {
    Button(
        onClick = {
            if (buttonInfo.title == "New Order") {
                navController.navigate(route.orderPlaceScreen)
            }
            if (buttonInfo.title == "Add Payment") {
                navController.navigate(route.paymentScreen)
            }
            if (buttonInfo.title == "Customer List") {
                navController.navigate(route.customerScreen)
                selectedItem = 2
            }
            if (buttonInfo.title == "Today's Orders") {
                navController.navigate(route.orderScreen)
                selectedItem = 1
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = buttonInfo.iconResId), // Use painterResource
                contentDescription = null, // Add a proper content description if needed
                modifier = Modifier.size(32.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = buttonInfo.title, fontSize = 14.sp, color = Color.Black)
        }
    }
}

data class BottomNavigationItem(
    val title: String,
    @DrawableRes val iconResId: Int, // Changed to drawable resource ID
)

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavigationItem("Home", R.drawable.home), // Replace with your icons
        BottomNavigationItem("Orders", R.drawable.order),
        BottomNavigationItem("Customers", R.drawable.custmerlist),// Replace with your icons
        BottomNavigationItem("Payments", R.drawable.payment), // Replace with your icons
        BottomNavigationItem("Settings", R.drawable.setting),// Replace with your icons
    )

    NavigationBar(modifier = Modifier.fillMaxWidth(), containerColor = Color.White) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    Image(
                        painter = painterResource(id = item.iconResId),
                        contentDescription = item.title,
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(
                            if (index == selectedItem) Color(0xFF007AFF) else Color(0xFF666666)
                        )
                    )
                }, // Use painterResource
                label = {
                    Text(
                        item.title,
                        color = if (index == selectedItem) Color(0xFF007AFF) else Color(0xFF666666)
                    )
                },
                selected = selectedItem == index,
                onClick = {
                    selectedItem = index
                    when (index) {
                        0 -> {
                            navController.navigate(route.dashBoardScreen)
                        }

                        1 -> {
                            navController.navigate(route.orderScreen)
                        }

                        2 -> {
                            navController.navigate(route.customerScreen)
                        }

                        3 -> {
                            navController.navigate(route.orderPlaceScreen)
                        }

                        4 -> {
                            navController.navigate(route.paymentScreen)
                        }
                    }
                }
            )
        }
    }
}