package database.dao

import database.tables.EntriesTable
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
        id = EntryId(this.id.value),
        userId = UserId(this.user.id),
        title = this.title,
        content = this.content,
        moodRating = this.moodRating,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
    )
}