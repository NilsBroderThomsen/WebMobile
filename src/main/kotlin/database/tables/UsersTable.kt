package database.tables

import org.jetbrains.exposed.dao.id.LongIdTable

@OptIn(kotlin.time.ExperimentalTime::class)
object UsersTable : LongIdTable("users") {
    // TODO: username Spalte (varchar, 100 Zeichen, uniqueIndex)
    // TODO: email Spalte (varchar, 200 Zeichen, uniqueIndex)
    // TODO: passwordHash Spalte (varchar, 255 Zeichen)
    // TODO: registrationDate Spalte (date - gibt kotlinx.datetime.LocalDate zurück)
    // TODO: isActive Spalte (bool, default = true)
    // TODO: createdAt Spalte (timestamp - gibt kotlinx.datetime.Instant zurück!)
}