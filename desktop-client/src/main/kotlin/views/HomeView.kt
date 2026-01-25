package views

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun HomeView(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToEntries: () -> Unit
) {
    Column {
        Button(onClick = onNavigateToLogin) {
            Text("Login")
        }
        Button(onClick = onNavigateToRegister) {
            Text("Register")
        }
        Button(onClick = onNavigateToEntries) {
            Text("My Entries")
        }
    }
}
