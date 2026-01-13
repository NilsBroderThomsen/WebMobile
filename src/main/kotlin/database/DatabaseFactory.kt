package database

import database.dao.UserDAO
import database.tables.EntriesTable
import database.tables.UsersTable
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
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

            if (UserDAO.findById(1L) == null) {
                UserDAO.new(1L) {
                    username = "default-user"
                    email = "default@example.com"
                    passwordHash = "test"
                    registrationDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
                    isActive = true
                }
            }
        }
    }
}