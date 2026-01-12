package database

import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import model.EntryId
import model.User
import model.Entry
import model.UserId
import org.jetbrains.exposed.sql.transactions.transaction

class MoodTrackerDatabaseRepository {
    // User Operations
    fun createUser(user: User): User = transaction {
    // TODO: UserDAO.new { } Block erstellen
    // TODO: Alle Properties setzen (Typ-Konvertierung für registrationDate!)
    // TODO: createdAt mit Clock.System.now() setzen
    // TODO: toModel() zurückgeben
    }

    fun findUserById(userId: UserId): User? = transaction {
    // TODO: UserDAO.findById() verwenden und mit toModel() konvertieren
    }

    fun findUserByEmail(email: String): User? = transaction {
    // TODO: UserDAO.find { } mit email-Bedingung verwenden
    }

    // Entry Operations
    fun createEntry(entry: Entry): Entry = transaction {
    // TODO: User-DAO laden (oder Exception)
    // TODO: EntryDAO.new { } Block erstellen
    // TODO: Properties setzen (Typ-Konvertierung für createdAt!)
    // TODO: toModel() zurückgeben
    }

    fun findAllEntries(userId: UserId): List<Entry> = transaction {
    // TODO: EntryDAO.find { } mit userId-Filter
    // TODO: Nach createdAt sortieren (verwende sortedByDescending, nicht orderBy!)
    // TODO: Mit map { } zu List<Entry> konvertieren
    }
    fun findEntryById(entryId: EntryId): Entry? = transaction {
    // TODO: EntryDAO.findById() und toModel()
    }
    fun updateEntry(entry: Entry): Entry = transaction {
    // TODO: EntryDAO finden
    // TODO: Properties aktualisieren
    // TODO: updatedAt setzen
    // TODO: toModel() zurückgeben
    }
    fun deleteEntry(entryId: EntryId): Boolean = transaction {
    // TODO: EntryDAO finden und delete() aufrufen
    // TODO: Boolean zurückgeben (Erfolg?)
    }
}