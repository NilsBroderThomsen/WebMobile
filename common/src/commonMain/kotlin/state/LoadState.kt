package state

sealed interface LoadState<out T> {
    data object Loading : LoadState<Nothing>

    data class Success<T>(val data: T) : LoadState<T>

    data class Error(val message: String, val cause: Throwable? = null) : LoadState<Nothing>
}

suspend fun <T> fetchLoadState(
    defaultErrorMessage: String,
    block: suspend () -> T
): LoadState<T> {
    return try {
        LoadState.Success(block())
    } catch (ex: Exception) {
        LoadState.Error(ex.message ?: defaultErrorMessage, ex)
    }
}
