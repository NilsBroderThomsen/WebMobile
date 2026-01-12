package database.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object EntriesTable : LongIdTable("entries") {
    val userId = long("user_id").references(UsersTable.id)
    val title = varchar("title", 200)
    val content = text("content")
    val moodRating = integer("mood_rating").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at").nullable()
    init {
        index(true, userId, createdAt)
    }
}
