package database

import database.dao.EntryDAO
import database.dao.UserDAO
import database.tables.EntriesTable
import database.tables.UsersTable
import kotlinx.datetime.LocalDateTime as KxLocalDateTime
import kotlinx.datetime.LocalDate as KxLocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import model.Entry
import model.EntryId
import model.User
import model.UserId
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class MoodTrackerDatabaseRepository {
    // User Operations
    fun createUser(user: User): User = transaction {
        UserDAO.new {
            username = user.username
            email = user.email
            passwordHash = user.passwordHash
            registrationDate = KxLocalDate(user.registrationDate.year, user.registrationDate.monthValue, user.registrationDate.dayOfMonth)
            isActive = user.isActive
            createdAt = Clock.System.now()
        }.toModel()
    }

    fun findUserById(userId: UserId): User? = transaction {
        UserDAO.findById(userId.value)?.toModel()
    }

    fun findUserByEmail(email: String): User? = transaction {
        UserDAO.find { UsersTable.email eq email }
            .limit(1)
            .firstOrNull()
            ?.toModel()
    }

    // Entry Operations
    fun createEntry(entry: Entry): Entry = transaction {
        val userDao = UserDAO.findById(entry.userId.value)
            ?: throw IllegalArgumentException("User not found")

        EntryDAO.new {
            user = userDao
            title = entry.title
            content = entry.content
            moodRating = entry.moodRating
            createdAt = KxLocalDateTime(
                entry.createdAt.year,
                entry.createdAt.monthValue,
                entry.createdAt.dayOfMonth,
                entry.createdAt.hour,
                entry.createdAt.minute,
                entry.createdAt.second,
                entry.createdAt.nano
            ).toInstant(TimeZone.UTC)
            updatedAt = entry.updatedAt?.let {
                KxLocalDateTime(
                    it.year,
                    it.monthValue,
                    it.dayOfMonth,
                    it.hour,
                    it.minute,
                    it.second,
                    it.nano
                ).toInstant(TimeZone.UTC)
            }
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
        val existing = EntryDAO.findById(entry.id.value) ?: throw IllegalArgumentException("Entry not found")

        existing.apply {
            title = entry.title
            content = entry.content
            moodRating = entry.moodRating
            updatedAt = Clock.System.now()
        }.toModel()
    }

    fun deleteEntry(entryId: EntryId): Boolean = transaction {
        EntryDAO.findById(entryId.value)?.let {
            it.delete()
            true
        } ?: false
    }
}
