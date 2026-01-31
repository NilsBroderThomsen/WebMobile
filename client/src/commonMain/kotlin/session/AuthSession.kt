package session

import api.MoodTrackerClient

class AuthSession(private val client: MoodTrackerClient) {
    val authenticatedUserId: Long?
        get() = client.authenticatedUserId

    fun requireAuthenticatedUserId(): Long {
        return authenticatedUserId ?: throw IllegalStateException("Login erforderlich.")
    }

    fun logout() {
        client.logout()
    }
}
