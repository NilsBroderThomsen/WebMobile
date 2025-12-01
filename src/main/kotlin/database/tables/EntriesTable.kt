package database.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

@OptIn(kotlin.time.ExperimentalTime::class)
object EntriesTable : LongIdTable("entries") {
    val userId = reference("userId", UsersTable)
    val title = varchar("title", length = 200)
    val content = text("content")
    val moodRating = integer("moodRating").nullable()
    val createdAt = timestamp("createdAt")
    val updatedAt = timestamp("updatedAt").nullable()

    init {
        index(isUnique = false, userId, createdAt)
    }
}
