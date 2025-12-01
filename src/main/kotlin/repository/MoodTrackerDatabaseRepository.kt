package repository

import database.EntryDAO
import database.EntriesTable
import database.UserDAO
import database.UsersTable
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.toLocalDateTime
import model.Entry
import model.EntryId
import model.User
import model.UserId
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.transactions.transaction
import kotlinx.datetime.Clock

@OptIn(kotlin.time.ExperimentalTime::class)
class MoodTrackerDatabaseRepository {

    // ========== User Operations ==========

    fun createUser(user: User): User = transaction {
        val userDao = UserDAO.new {
            username = user.username
            email = user.email
            passwordHash = user.passwordHash
            registrationDate = user.registrationDate.toKotlinLocalDate()
            isActive = user.isActive
            createdAt = Clock.System.now()
        }
        userDao.toModel()
    }

    fun findUserById(userId: UserId): User? = transaction {
        UserDAO.findById(userId.value)?.toModel()
    }

    fun findUserByEmail(email: String): User? = transaction {
        UserDAO.find { UsersTable.email eq email }
            .firstOrNull()
            ?.toModel()
    }

    fun getAllUsers(): List<User> = transaction {
        UserDAO.all().map { it.toModel() }
    }

    // ========== Entry Operations ==========

    fun createEntry(entry: Entry): Entry = transaction {
        // User-DAO laden (oder Exception werfen)
        val userDao = UserDAO.findById(entry.userId.value)
            ?: throw IllegalArgumentException("User with id ${entry.userId.value} not found")

        val entryDao = EntryDAO.new {
            user = userDao
            title = entry.title
            content = entry.content
            moodRating = entry.moodRating
            createdAt = entry.createdAt.toKotlinInstant()
            updatedAt = entry.updatedAt?.toKotlinInstant()
        }
        entryDao.toModel()
    }

    fun findAllEntries(userId: UserId): List<Entry> = transaction {
        EntryDAO.find { EntriesTable.userId eq userId.value }
            .sortedByDescending { it.createdAt }
            .map { it.toModel() }
    }

    fun findEntryById(entryId: EntryId): Entry? = transaction {
        EntryDAO.findById(entryId.value)?.toModel()
    }

    fun updateEntry(entry: Entry): Entry? = transaction {
        val entryDao = EntryDAO.findById(entry.id.value)
            ?: return@transaction null

        entryDao.title = entry.title
        entryDao.content = entry.content
        entryDao.moodRating = entry.moodRating
        entryDao.updatedAt = Clock.System.now()

        entryDao.toModel()
    }

    fun deleteEntry(entryId: EntryId): Boolean = transaction {
        val entryDao = EntryDAO.findById(entryId.value)
            ?: return@transaction false

        entryDao.delete()
        true
    }
}