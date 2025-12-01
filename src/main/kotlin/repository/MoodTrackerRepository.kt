package repository

import database.MoodTrackerDatabaseRepository
import model.Entry
import model.EntryId
import model.User
import model.UserId
import java.time.LocalDate
import java.time.LocalDateTime

class MoodTrackerRepository {
    private val databaseRepository = MoodTrackerDatabaseRepository()

    suspend fun findEntryById(entryId: EntryId): Entry? = databaseRepository.findEntryById(entryId)

    suspend fun findAllEntries(userId: UserId): List<Entry> = databaseRepository.findAllEntries(userId)

    suspend fun addEntry(entry: Entry): Entry = databaseRepository.createEntry(entry)

    suspend fun deleteEntry(entryId: EntryId): Boolean = databaseRepository.deleteEntry(entryId)

    suspend fun updateEntry(entry: Entry): Entry? = try {
        databaseRepository.updateEntry(entry)
    } catch (ex: IllegalArgumentException) {
        null
    }

    suspend fun findUserById(userId: UserId): User? = databaseRepository.findUserById(userId)

    suspend fun findUserByEmail(email: String): User? = databaseRepository.findUserByEmail(email)

    suspend fun createUser(user: User): User = databaseRepository.createUser(user)

    fun initializeWithTestData() {
        val existingUser = databaseRepository.findUserByEmail("nils@sample.com")
        val user = existingUser ?: databaseRepository.createUser(
            User(UserId(0), "Nils", "nils@sample.com", "dbhasjl", LocalDate.now())
        )

        if (databaseRepository.findAllEntries(user.id).isNotEmpty()) {
            return
        }

        val now = LocalDateTime.now()

        fun e(
            title: String,
            content: String,
            mood: Int?,
            daysAgo: Long
        ) = Entry(
            id = EntryId(0),
            userId = user.id,
            title = title,
            content = content,
            moodRating = mood,
            createdAt = now.minusDays(daysAgo),
            updatedAt = null,
            tags = emptySet()
        )

        listOf(
            e("Happy Day", "Great day,,, at work!", 8, 7),
            e("Tired", "Long night.", 4, 6),
            e("Excited", "New project started!", 9, 5),
            e("Neutral Day", "Nothing special.", null, 4),
            e("Focused", "Deep work session, few distractions.", 7, 3),
            e("Stressed", "Deadlines piling up.", 3, 2),
            e("Okay-ish", "Average day, nothing big.", 5, 1),
            e("Great Weather", "Walked outside and relaxed.", 8, 0)
        ).forEach { databaseRepository.createEntry(it) }
    }
}
