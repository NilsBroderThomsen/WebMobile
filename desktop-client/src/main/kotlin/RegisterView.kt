import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun RegisterView(onNavigateBack: () -> Unit, onNavigateToEntries: () -> Unit) {
    Column {
        Button(onClick = onNavigateBack) {
            Text("Back")
        }
    }
}
