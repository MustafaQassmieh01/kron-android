package dev.kron.app

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.kron.app.application.KronApplication
import dev.kron.app.screens.bookmarks.BookmarksScreen
import dev.kron.app.screens.bookmarks.details.EventDetailsScreen
import dev.kron.app.screens.search.SearchScreen
import dev.kron.app.screens.search.details.SearchDetailsScreen
import dev.kron.app.screens.settings.SettingsScreen
import dev.kron.app.ui.theme.KronTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as KronApplication
        setContent {
            val appearance by app.appSettings.appearance.collectAsState()
            KronTheme(appearance) {
                val nav = rememberNavController()
                NavHost(
                    navController = nav,
                    startDestination = "bookmarks",
                    enterTransition = {
                        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left)
                    },
                    exitTransition = {
                        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left)
                    },
                    popEnterTransition = {
                        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right)
                    },
                    popExitTransition = {
                        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right)
                    }
                ) {
                    composable("bookmarks") {
                        BookmarksScreen(
                            app = app,
                            onSearch = { nav.navigate("search") },
                            onSettings = { nav.navigate("settings") },
                            onEvent = { nav.navigate("event/${Uri.encode(it)}") }
                        )
                    }
                    composable("search") {
                        SearchScreen(
                            app = app,
                            onBack = { nav.popBackStack() },
                            onProgramme = { school, id -> nav.navigate("programme/${Uri.encode(school)}/${Uri.encode(id)}") }
                        )
                    }
                    composable(
                        "programme/{school}/{programme}",
                        arguments = listOf(
                            navArgument("school") { type = NavType.StringType },
                            navArgument("programme") { type = NavType.StringType }
                        )
                    ) { entry ->
                        SearchDetailsScreen(
                            app = app,
                            schoolId = Uri.decode(entry.arguments?.getString("school").orEmpty()),
                            programmeId = Uri.decode(entry.arguments?.getString("programme").orEmpty()),
                            onBack = { nav.popBackStack() },
                            onEvent = { nav.navigate("event/${Uri.encode(it)}") }
                        )
                    }
                    composable(
                        "event/{eventId}",
                        arguments = listOf(navArgument("eventId") { type = NavType.StringType })
                    ) { entry ->
                        EventDetailsScreen(
                            app = app,
                            eventId = Uri.decode(entry.arguments?.getString("eventId").orEmpty()),
                            onBack = { nav.popBackStack() }
                        )
                    }
                    composable("settings") {
                        SettingsScreen(app = app, onBack = { nav.popBackStack() })
                    }
                }
            }
        }
    }
}
