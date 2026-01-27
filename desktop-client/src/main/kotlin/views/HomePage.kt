package views

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun HomePage(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToEntries: () -> Unit,
    showLogout: Boolean,
    onLogout: () -> Unit
) {
    Column {
        if (!showLogout) {
            Button(onClick = onNavigateToLogin) {
                Text("Login")
            }
            Button(onClick = onNavigateToRegister) {
                Text("Register")
            }
        }
        if (showLogout) {
            Button(onClick = onNavigateToEntries) {
                Text("My Entries")
            }
            Button(onClick = onLogout) {
                Text("Logout")
            }
        }
    }
}
