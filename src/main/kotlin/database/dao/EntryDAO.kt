package database.dao

import database.tables.EntriesTable
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlinx.datetime.Instant as KotlinInstant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import model.Entry
import model.EntryId
import model.UserId

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
        createdAt = createdAt.toJavaLocalDateTime(),
        updatedAt = updatedAt?.toJavaLocalDateTime(),
    )
}

private fun KotlinInstant.toJavaLocalDateTime(): LocalDateTime {
    val localDateTime = toLocalDateTime(TimeZone.UTC)
    return LocalDateTime.of(
        localDateTime.year,
        localDateTime.monthNumber,
        localDateTime.dayOfMonth,
        localDateTime.hour,
        localDateTime.minute,
        localDateTime.second,
        localDateTime.nanosecond
    )
}

fun LocalDateTime.toKotlinInstant(): KotlinInstant {
    val instant = toInstant(ZoneOffset.UTC)
    return KotlinInstant.fromEpochSeconds(instant.epochSecond, instant.nano)
}
