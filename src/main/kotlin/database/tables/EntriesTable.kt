package database.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

@OptIn(kotlin.time.ExperimentalTime::class)
object EntriesTable : LongIdTable("entries") {
    val userId = reference("user_id", UsersTable, onDelete = ReferenceOption.CASCADE)
    val title = varchar("title", 200)
    val content = text("content")
    val moodRating = integer("mood_rating").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at").nullable()

    init {
        index(false, userId, createdAt)
    }
}
