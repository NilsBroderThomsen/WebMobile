package database.dao

import database.tables.UsersTable
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.time.LocalDate

@OptIn(kotlin.time.ExperimentalTime::class)
class UserDAO(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<UserDAO>(UsersTable)
    // TODO: Properties mit 'var' und 'by' delegation zu Spalten
    // - username
    // - email
    // - passwordHash
    // - registrationDate (Typ: kotlinx.datetime.LocalDate)
    // - isActive
    // - createdAt (Typ: kotlinx.datetime.Instant)
    // TODO: Relationship zu Entries (später)
    // val entries by EntryDAO referrersOn EntriesTable.userId
    // TODO: toModel() Methode implementieren
    // fun toModel(): User = User(...)
    // WICHTIG: Konvertiere kotlinx.datetime.LocalDate → java.time.LocalDate
    // Verwende: java.time.LocalDate.of(year, monthNumber, dayOfMonth)
}