package database

import database.dao.EntryDAO
import database.dao.UserDAO
import database.tables.EntriesTable
import database.tables.UsersTable
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import model.EntryId
import model.User
import model.Entry
import model.UserId
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.time.Clock

class MoodTrackerDatabaseRepository {
    fun createUser(user: User): User = transaction {
        UserDAO.new {
            username = user.username
            email = user.email
            passwordHash = user.passwordHash
            registrationDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
            isActive = user.isActive
        }.toModel()
    }

    fun findUserById(userId: UserId): User? = transaction {
        UserDAO.findById(userId.value)?.toModel()
    }

    fun findUserByEmail(email: String): User? = transaction {
        UserDAO.find { UsersTable.email eq email }.singleOrNull()?.toModel()
    }

    fun createEntry(entry: Entry): Entry = transaction {
        val user = UserDAO.findById(entry.userId.value)
            ?: error("User with id ${entry.userId.value} not found")

        EntryDAO.new {
            this.user = user
            this.title = entry.title
            this.content = entry.content
            this.moodRating = entry.moodRating
            this.createdAt = Clock.System.now()
            this.updatedAt = entry.updatedAt
        }.toModel()
    }

    fun findAllEntries(userId: UserId): List<Entry> = transaction {
        EntryDAO.find { EntriesTable.userId eq userId.value }
            .sortedByDescending { it.createdAt }
            .map { it.toModel() }
    }

    fun findEntryById(entryId: EntryId): Entry? = transaction {
        EntryDAO.findById(entryId.value)?.toModel()
    }

    fun updateEntry(entry: Entry): Entry = transaction {
        val entryDAO = EntryDAO.findById(entry.id.value)
            ?: error("Entry with id ${entry.id.value} not found")

        entryDAO.apply {
            title = entry.title
            content = entry.content
            moodRating = entry.moodRating
            updatedAt = Clock.System.now()
        }.toModel()
    }

    fun deleteEntry(entryId: EntryId): Boolean = transaction {
        val entryDAO = EntryDAO.findById(entryId.value)
            ?: return@transaction false

        entryDAO.delete()
        true
    }
}