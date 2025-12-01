package database.dao

import database.tables.EntriesTable
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import model.Entry
import model.EntryId
import model.UserId
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.time.LocalDateTime

@OptIn(kotlin.time.ExperimentalTime::class)
class EntryDAO(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<EntryDAO>(EntriesTable)

    var user by UserDAO referencedOn EntriesTable.userId
    var title by EntriesTable.title
    var content by EntriesTable.content
    var moodRating by EntriesTable.moodRating
    var createdAt by EntriesTable.createdAt
    var updatedAt by EntriesTable.updatedAt

    fun toModel(): Entry {
        val created = createdAt.toLocalDateTime(TimeZone.UTC)
        val updated = updatedAt?.toLocalDateTime(TimeZone.UTC)

        return Entry(
            id = EntryId(id.value),
            userId = UserId(user.id.value),
            title = title,
            content = content,
            moodRating = moodRating,
            createdAt = LocalDateTime.of(
                created.year,
                created.monthNumber,
                created.dayOfMonth,
                created.hour,
                created.minute,
                created.second,
                created.nanosecond
            ),
            updatedAt = updated?.let {
                LocalDateTime.of(
                    it.year,
                    it.monthNumber,
                    it.dayOfMonth,
                    it.hour,
                    it.minute,
                    it.second,
                    it.nanosecond
                )
            }
        )
    }
}
