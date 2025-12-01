package database

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import model.Entry
import model.EntryId
import model.UserId
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

@OptIn(kotlin.time.ExperimentalTime::class)
class EntryDAO(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<EntryDAO>(EntriesTable)

    var user by UserDAO referencedOn EntriesTable.userId
    var title by EntriesTable.title
    var content by EntriesTable.content
    var moodRating by EntriesTable.moodRating
    var createdAt by EntriesTable.createdAt
    var updatedAt by EntriesTable.updatedAt

    fun toModel(): Entry = Entry(
        id = EntryId(id.value),
        userId = UserId(user.id.value),
        title = title,
        content = content,
        moodRating = moodRating,
        createdAt = createdAt.toLocalDateTime(TimeZone.UTC).toJavaLocalDateTime(),
        updatedAt = updatedAt?.toLocalDateTime(TimeZone.UTC)?.toJavaLocalDateTime(),
        tags = emptySet() // Tags später erweitern
    )
}