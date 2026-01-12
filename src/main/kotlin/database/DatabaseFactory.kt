package database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init() {
    // TODO: Database.connect() mit SQLite aufrufen
    // URL: "jdbc:sqlite:moodtracker.db"
    // Driver: "org.sqlite.JDBC"

    // TODO: Schema erstellen in transaction { } Block
    // SchemaUtils.create(...) mit allen Tables
    }
}