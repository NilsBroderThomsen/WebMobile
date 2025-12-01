package database

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    fun init() {
        // SQLite Verbindung herstellen
        val driverClassName = "org.sqlite.JDBC"
        val jdbcURL = "jdbc:sqlite:moodtracker.db"
        val database = Database.connect(jdbcURL, driverClassName)

        // Schema erstellen (Tabellen anlegen)
        transaction(database) {
            SchemaUtils.create(UsersTable, EntriesTable)
        }
    }

    // Helper-Funktion für suspendable Database Queries
    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}