package repository

import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import model.Entry
import model.EntryId
import model.User
import model.UserId
import kotlin.time.Duration.Companion.days

class MoodTrackerRepository {
    private val users = mutableListOf<User>()
    private val entries = mutableListOf<Entry>()

    suspend fun findEntryById(entryId: EntryId): Entry? {
        delay(50) // Simuliert Datenbankzugriff
        return entries.firstOrNull { it.id == entryId }
    }

    suspend fun findAllEntries(userId: UserId): List<Entry> {
        delay(100) // Simuliert Datenbankzugriff
        return entries.filter { it.userId == userId }
    }

    suspend fun addEntry(entry: Entry): Entry {
        delay(80) // Simuliert Datenbankzugriff
        entries.add(entry)
        return entry
    }

    suspend fun deleteEntry(entryId: EntryId): Boolean {
        delay(70) // Simuliert Datenbankzugriff
        return entries.removeIf { it.id == entryId }
    }

    suspend fun updateEntry(entry: Entry): Entry? {
        delay(80) // Simuliert Datenbankzugriff
        val index = entries.indexOfFirst { it.id == entry.id }
        if (index == -1) {
            return null
        }
        entries[index] = entry
        return entries[index]
    }

    fun initializeWithTestData() {
        val user = User(id = UserId(1), "Nils", "nils@sample.com", "dbhasjl", Clock.System.todayIn(TimeZone.currentSystemDefault()))
        users.add(user)

        fun e(
            id: Long,
            title: String,
            content: String,
            mood: Int?,
            daysAgo: Long
        ) = Entry(
            id = EntryId(id),
            userId = user.id,
            title = title,
            content = content,
            moodRating = mood,
            createdAt = Clock.System.now() - daysAgo.days,
            updatedAt = null,
            tags = emptySet()
        )

        entries += listOf(
            e(1, "Happy Day", "Great day,,, at work!", 8, 7),
            e(2, "Tired", "Long night.", 4, 6),
            e(3, "Excited", "New project started!", 9, 5),
            e(4, "Neutral Day", "Nothing special.", null, 4),
            e(5, "Focused", "Deep work session, few distractions.", 7, 3),
            e(6, "Stressed", "Deadlines piling up.", 3, 2),
            e(7, "Okay-ish", "Average day, nothing big.", 5, 1),
            e(8, "Great Weather", "Walked outside and relaxed.", 8, 0)
        )
    }
}
