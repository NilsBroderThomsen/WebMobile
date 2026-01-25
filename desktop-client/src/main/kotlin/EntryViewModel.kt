import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import api.MoodTrackerClient
import dto.EntryDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class EntryViewModel : ViewModel() {
    private val client = MoodTrackerClient("http://localhost:8080")

    private val _entries = MutableStateFlow<List<EntryDto>>(emptyList())
    val entries: StateFlow<List<EntryDto>> = _entries.asStateFlow()

    init {
        loadEntries()
    }

    private fun loadEntries() {
        viewModelScope.launch {
            try {
                _entries.value = client.getEntries(userId = 1L)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}