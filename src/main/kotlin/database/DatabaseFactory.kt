package database

import database.dao.EntryDAO
import database.dao.UserDAO
import database.tables.EntriesTable
import database.tables.UsersTable
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init() {
        Database.connect(
            url = "jdbc:sqlite:moodtracker.db",
            driver = "org.sqlite.JDBC"
        )

        transaction {
            SchemaUtils.create(
                UsersTable,
                EntriesTable
            )

            val defaultUser = UserDAO.findById(1L) ?: UserDAO.new(1L) {
                username = "default-user"
                email = "default@example.com"
                passwordHash = "test"
                registrationDate = LocalDate.now(ZoneId.systemDefault())
                isActive = true
            }

            val hasAnyEntry = EntryDAO.find { EntriesTable.userId eq defaultUser.id.value }.empty().not()
            if (!hasAnyEntry) {
                EntryDAO.new {
                    user = defaultUser
                    title = "Mein erster Eintrag"
                    content = "Automatisch beim Start angelegt."
                    moodRating = 4
                    createdAt = Instant.now()
                    updatedAt = null
                }
            }
        }
    }
}
