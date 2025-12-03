package database.tables

import org.jetbrains.exposed.dao.id.LongIdTable

@OptIn(kotlin.time.ExperimentalTime::class)
object EntriesTable : LongIdTable("entries") {
    // TODO: Spalte für Foreign Key zu Users (reference)
    // TODO: Spalte für Titel (varchar, 200 Zeichen)
    // TODO: Spalte für Content (text)
    // TODO: Spalte für moodRating (integer, nullable)
    // TODO: Spalte für createdAt (timestamp - gibt Instant zurück!)
    // TODO: Spalte für updatedAt (timestamp, nullable)
    // TODO: Index für häufige Queries erstellen (init Block)
}