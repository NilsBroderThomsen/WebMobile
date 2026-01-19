package database.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.date

object UsersTable : LongIdTable("users") {
    val username = varchar("username", 100).uniqueIndex()
    val email = varchar("email", 200).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val registrationDate = date("registration_date")
    val isActive = bool("is_active").default(true)
}
