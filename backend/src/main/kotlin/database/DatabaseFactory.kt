package database

import database.dao.EntryDAO
import database.dao.UserDAO
import database.tables.EntriesTable
import database.tables.UsersTable
import extension.toJavaInstant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.toJavaLocalDate
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import security.PasswordHasher
import kotlin.time.Clock

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
                passwordHash = PasswordHasher.hash("test")
                registrationDate = Clock.System.todayIn(TimeZone.currentSystemDefault()).toJavaLocalDate()
                isActive = true
            }
            val hasAnyEntry = EntryDAO.find { EntriesTable.userId eq defaultUser.id }.empty().not()
            if (!hasAnyEntry) {
                EntryDAO.new {
                    user = defaultUser
                    title = "Mein erster Eintrag"
                    content = "Automatisch beim Start angelegt."
                    moodRating = 4
                    createdAt = Clock.System.now().toJavaInstant()
                    updatedAt = null
                }
            }
        }
    }
}
