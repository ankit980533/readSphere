package com.novelplatform.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.novelplatform.app.ui.screens.HomeScreen
import com.novelplatform.app.ui.screens.NovelDetailScreen
import com.novelplatform.app.ui.screens.ReaderScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(navController)
        }
        composable("novel/{novelId}") { backStackEntry ->
            val novelId = backStackEntry.arguments?.getString("novelId")?.toLong()
            NovelDetailScreen(novelId, navController)
        }
        composable("reader/{chapterId}") { backStackEntry ->
            val chapterId = backStackEntry.arguments?.getString("chapterId")?.toLong()
            ReaderScreen(chapterId, navController)
        }
    }
}
