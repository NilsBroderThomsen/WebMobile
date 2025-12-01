package database.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

@OptIn(kotlin.time.ExperimentalTime::class)
object UsersTable : LongIdTable("users") {
    val username = varchar("username", length = 100).uniqueIndex()
    val email = varchar("email", length = 200).uniqueIndex()
    val passwordHash = varchar("passwordHash", length = 255)
    val registrationDate = date("registrationDate")
    val isActive = bool("isActive").default(true)
    val createdAt = timestamp("createdAt")
}
