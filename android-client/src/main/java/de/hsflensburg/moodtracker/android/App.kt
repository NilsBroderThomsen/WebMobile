package de.hsflensburg.moodtracker.android

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import api.MoodTrackerClient
import dto.EntryDto
import views.CreateEntryPage
import views.EntryDetailsPage
import views.EntryListPage
import views.HomePage
import views.LoginPage
import views.RegisterPage
import views.UpdateEntryPage

@Composable
fun App(client: MoodTrackerClient) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var authUserId by remember { mutableStateOf<Long?>(null) }

    when (currentScreen) {
        Screen.Home -> {
            HomePage(
                onNavigateToEntries = {
                    currentScreen = if (authUserId == null) {
                        Screen.Login
                    } else {
                        Screen.Entries
                    }
                },
                onNavigateToLogin = {
                    currentScreen = Screen.Login
                },
                onNavigateToRegister = {
                    currentScreen = Screen.Register
                },
                showLogout = authUserId != null,
                onLogout = {
                    client.logout()
                    authUserId = null
                    currentScreen = Screen.Home
                }
            )
        }
        Screen.Login -> {
            LoginPage(
                client = client,
                onNavigateBack = {
                    currentScreen = Screen.Home
                },
                onNavigateToEntries = { userId ->
                    authUserId = userId
                    currentScreen = Screen.Entries
                }
            )
        }
        Screen.Register -> {
            RegisterPage(
                client = client,
                onNavigateBack = {
                    currentScreen = Screen.Home
                },
                onNavigateToEntries = { userId ->
                    authUserId = userId
                    currentScreen = Screen.Entries
                }
            )
        }
        Screen.Entries -> {
            val userId = authUserId
            if (userId == null) {
                LaunchedEffect(Unit) {
                    currentScreen = Screen.Login
                }
            } else {
                EntryListPage(
                    client = client,
                    userId = userId,
                    onNavigateBack = {
                        currentScreen = Screen.Home
                    },
                    onCreateEntry = {
                        currentScreen = Screen.CreateEntry
                    },
                    onUpdateEntry = { entryDto ->
                        currentScreen = Screen.UpdateEntry(entryDto)
                    },
                    onEntrySelected = { entryId ->
                        currentScreen = Screen.EntryDetails(entryId)
                    }
                )
            }
        }
        Screen.CreateEntry -> {
            val userId = authUserId
            if (userId == null) {
                LaunchedEffect(Unit) {
                    currentScreen = Screen.Login
                }
            } else {
                CreateEntryPage(
                    client = client,
                    userId = userId,
                    onNavigateBack = {
                        currentScreen = Screen.Entries
                    }
                )
            }
        }
        is Screen.UpdateEntry -> {
            UpdateEntryPage(
                client = client,
                entryDto = (currentScreen as Screen.UpdateEntry).entryDto,
                onNavigateBack = {
                    currentScreen = Screen.Entries
                }
            )
        }
        is Screen.EntryDetails -> {
            EntryDetailsPage(
                client = client,
                entryId = (currentScreen as Screen.EntryDetails).entryId,
                onNavigateBack = {
                    currentScreen = Screen.Entries
                }
            )
        }
    }
}

private sealed interface Screen {
    data object Home : Screen
    data object Login : Screen
    data object Register : Screen
    data object Entries : Screen
    data object CreateEntry : Screen
    data class UpdateEntry(val entryDto: EntryDto) : Screen
    data class EntryDetails(val entryId: Long) : Screen
}
