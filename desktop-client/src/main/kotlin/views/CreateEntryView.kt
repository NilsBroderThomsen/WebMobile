package views

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun CreateEntryView(
    onNavigateBack: () -> Unit
) {
    Column {
        Button(onClick = onNavigateBack) {
            Text("Back")
        }
    }
}
