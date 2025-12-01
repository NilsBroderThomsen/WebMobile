package database

import database.dao.EntryDAO
import database.dao.UserDAO
import database.tables.EntriesTable
import database.tables.UsersTable
import kotlinx.datetime.LocalDate as KtLocalDate
import kotlinx.datetime.LocalDateTime as KtLocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import model.Entry
import model.EntryId
import model.User
import model.UserId
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
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
            registrationDate = KtLocalDate(
                user.registrationDate.year,
                user.registrationDate.monthValue,
                user.registrationDate.dayOfMonth
            )
            isActive = user.isActive
            createdAt = Clock.System.now()
        }.toModel()
    }

    fun findUserById(userId: UserId): User? = transaction {
        UserDAO.findById(userId.value)?.toModel()
    }

    fun findUserByEmail(email: String): User? = transaction {
        UserDAO.find { UsersTable.email eq email }
            .singleOrNull()
            ?.toModel()
    }

    // Entry Operations
    fun createEntry(entry: Entry): Entry = transaction {
        val userDao = UserDAO.findById(entry.userId.value)
            ?: throw IllegalArgumentException("User not found")

        val createdAtInstant = KtLocalDateTime(
            entry.createdAt.year,
            entry.createdAt.monthValue,
            entry.createdAt.dayOfMonth,
            entry.createdAt.hour,
            entry.createdAt.minute,
            entry.createdAt.second,
            entry.createdAt.nano
        ).toInstant(TimeZone.UTC)

        val updatedAtInstant = entry.updatedAt?.let {
            KtLocalDateTime(
                it.year,
                it.monthValue,
                it.dayOfMonth,
                it.hour,
                it.minute,
                it.second,
                it.nano
            ).toInstant(TimeZone.UTC)
        }

        EntryDAO.new {
            user = userDao
            title = entry.title
            content = entry.content
            moodRating = entry.moodRating
            createdAt = createdAtInstant
            updatedAt = updatedAtInstant
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
        val entryDao = EntryDAO.findById(entry.id.value)
            ?: throw IllegalArgumentException("Entry not found")

        entryDao.title = entry.title
        entryDao.content = entry.content
        entryDao.moodRating = entry.moodRating
        entryDao.updatedAt = Clock.System.now()
        entryDao.toModel()
    }

    fun deleteEntry(entryId: EntryId): Boolean = transaction {
        val entryDao = EntryDAO.findById(entryId.value) ?: return@transaction false
        entryDao.delete()
        true
    }

    // Compatibility helpers with previous repository naming
    fun addEntry(entry: Entry): Entry = createEntry(entry)
}
