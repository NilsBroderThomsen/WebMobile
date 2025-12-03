package database.dao

import kotlinx.datetime.TimeZone
import java.time.LocalDateTime

@OptIn(kotlin.time.ExperimentalTime::class)
class EntryDAO(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<EntryDAO>(EntriesTable)
    // TODO: Properties mit 'var' und 'by' delegation
    // - user (Referenz zu UserDAO via referencedOn)
    // - title
    // - content
    // - moodRating
    // - createdAt (Typ: kotlinx.datetime.Instant)
    // - updatedAt (Typ: kotlinx.datetime.Instant?, nullable)
    // TODO: toModel() Methode implementieren
    // WICHTIG: Konvertiere kotlinx.datetime.Instant → java.time.LocalDateTime
    // Zweistufig: instant.toLocalDateTime(TimeZone.UTC) → java.time.LocalDateTime.of(...)
}