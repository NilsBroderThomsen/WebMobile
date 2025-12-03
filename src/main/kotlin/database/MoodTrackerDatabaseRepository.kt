package database

import database.dao.EntryDAO
import database.dao.UserDAO
import database.tables.EntriesTable
import database.tables.UsersTable
import kotlin.time.Clock  // Wichtig: kotlin.time.Clock, NICHT kotlinx.datetime.Clock!
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.toKotlinLocalDateTime
import model.Entry
import model.EntryId
import model.User
import model.UserId
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

@OptIn(kotlin.time.ExperimentalTime::class)
class MoodTrackerDatabaseRepository {
    // User Operations
    fun createUser(user: User): User = transaction {
        UserDAO.new {
            username = user.username
            email = user.email
            passwordHash = user.passwordHash
            registrationDate = user.registrationDate.toKotlinLocalDate()
            isActive = user.isActive
            createdAt = Clock.System.now()
        }.toModel()
    }

    fun findUserById(userId: UserId): User? = transaction {
        UserDAO.findById(userId.value)?.toModel()
    }

    fun findUserByEmail(email: String): User? = transaction {
        UserDAO.find { UsersTable.email eq email }.singleOrNull()?.toModel()
    }

    // Entry Operations
    fun createEntry(entry: Entry): Entry = transaction {
        val userDao = UserDAO.findById(entry.userId.value)
            ?: error("User with id ${entry.userId.value} not found")

        EntryDAO.new {
            user = userDao
            title = entry.title
            content = entry.content
            moodRating = entry.moodRating
            createdAt = entry.createdAt
                .toKotlinLocalDateTime()
                .toInstant(TimeZone.UTC)
            updatedAt = entry.updatedAt
                ?.toKotlinLocalDateTime()
                ?.toInstant(TimeZone.UTC)
        }.toModel()
    }

    fun findAllEntries(userId: UserId): List<Entry> = transaction {
        EntryDAO.find { EntriesTable.userId eq EntityID(userId.value, UsersTable) }
            .sortedByDescending { it.createdAt }
            .map { it.toModel() }
    }

    fun findEntryById(entryId: EntryId): Entry? = transaction {
        EntryDAO.findById(entryId.value)?.toModel()
    }

    fun updateEntry(entry: Entry): Entry = transaction {
        val entryDao = EntryDAO.findById(entry.id.value)
            ?: error("Entry with id ${entry.id.value} not found")

        entryDao.apply {
            title = entry.title
            content = entry.content
            moodRating = entry.moodRating
            updatedAt = Clock.System.now()
        }.toModel()
    }

    fun deleteEntry(entryId: EntryId): Boolean = transaction {
        val entryDao = EntryDAO.findById(entryId.value) ?: return@transaction false
        entryDao.delete()
        true
    }
}
