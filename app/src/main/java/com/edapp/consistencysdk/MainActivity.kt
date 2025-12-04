package com.edapp.consistencysdk

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.edapp.habittracker.ui.AddNewHabit
import com.edapp.habittracker.ui.ConsistencyRowView
import com.edapp.habittracker.ui.HabitViewModel
import com.edapp.habittracker.ui.MainScreen
import com.edapp.habittracker.ui.theme.MyAppTheme
import com.edapp.habittracker.util.Screens
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: HabitViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            MyAppTheme {
                NavigationStack(viewModel, navController)
            }
        }
    }
}

@Composable
fun NavigationStack(
    viewModel: HabitViewModel,
    navController: NavHostController
) {
    NavHost(
        navController = navController, startDestination = Screens.Main.route) {
        composable(route = Screens.Main.route) {
            MainScreen(viewModel, navController)
//            ConsistencyRowView(viewModel, PaddingValues(0.dp))
        }
        composable(route = Screens.Settings.route) {
//            LevelsScreen(navController, viewModel)
        }
        composable(route = Screens.AddHabit.route) {
            AddNewHabit( viewModel , navController)
        }

    }
}



