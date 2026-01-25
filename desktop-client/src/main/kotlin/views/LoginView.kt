package views

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun LoginView(onNavigateBack: () -> Unit, onNavigateToEntries: () -> Unit) {
    Column {
        Button(onClick = onNavigateBack) {
            Text("Back")
        }
    }
}
