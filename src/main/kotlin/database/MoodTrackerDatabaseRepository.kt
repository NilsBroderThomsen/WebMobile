package database

import database.dao.EntryDAO
import database.dao.UserDAO
import database.tables.EntriesTable
import database.tables.UsersTable
import kotlin.time.Clock
import kotlinx.datetime.LocalDate as KtxLocalDate
import kotlinx.datetime.LocalDateTime as KtxLocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import model.Entry
import model.EntryId
import model.User
import model.UserId
import org.jetbrains.exposed.sql.transactions.transaction

@OptIn(kotlin.time.ExperimentalTime::class)
class MoodTrackerDatabaseRepository {
    fun createUser(user: User): User = transaction {
        val dao = UserDAO.new {
            username = user.username
            email = user.email
            passwordHash = user.passwordHash
            registrationDate = KtxLocalDate(
                user.registrationDate.year,
                user.registrationDate.monthValue,
                user.registrationDate.dayOfMonth
            )
            isActive = user.isActive
            createdAt = Clock.System.now()
        }
        dao.toModel()
    }

    fun findUserById(userId: UserId): User? = transaction {
        UserDAO.findById(userId.value)?.toModel()
    }

    fun findUserByEmail(email: String): User? = transaction {
        UserDAO.find { UsersTable.email eq email }.firstOrNull()?.toModel()
    }

    fun createEntry(entry: Entry): Entry = transaction {
        val userDao = UserDAO.findById(entry.userId.value)
            ?: throw IllegalArgumentException("User not found")

        val createdAtInstant = KtxLocalDateTime(
            entry.createdAt.year,
            entry.createdAt.monthValue,
            entry.createdAt.dayOfMonth,
            entry.createdAt.hour,
            entry.createdAt.minute,
            entry.createdAt.second,
            entry.createdAt.nano
        ).toInstant(TimeZone.UTC)

        val updatedAtInstant = entry.updatedAt?.let {
            KtxLocalDateTime(
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
        val dao = EntryDAO.findById(entry.id.value)
            ?: throw IllegalArgumentException("Entry not found")

        val createdAtInstant = KtxLocalDateTime(
            entry.createdAt.year,
            entry.createdAt.monthValue,
            entry.createdAt.dayOfMonth,
            entry.createdAt.hour,
            entry.createdAt.minute,
            entry.createdAt.second,
            entry.createdAt.nano
        ).toInstant(TimeZone.UTC)

        val updatedAtInstant = Clock.System.now()

        dao.user = UserDAO.findById(entry.userId.value)
            ?: throw IllegalArgumentException("User not found")
        dao.title = entry.title
        dao.content = entry.content
        dao.moodRating = entry.moodRating
        dao.createdAt = createdAtInstant
        dao.updatedAt = updatedAtInstant
        dao.toModel()
    }

    fun deleteEntry(entryId: EntryId): Boolean = transaction {
        val dao = EntryDAO.findById(entryId.value) ?: return@transaction false
        dao.delete()
        true
    }
}
