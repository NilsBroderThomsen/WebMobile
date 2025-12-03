package dto

import kotlinx.serialization.*
import serialization.LocalDateTimeSerializer
import java.time.LocalDateTime

// TODO: EntryDTO data class erstellen
// @Serializable
// Felder: id (Long), userId (Long), title, content, moodRating (Int?), createdAt (String), updatedAt (String?)
// TODO: Extension function Entry.toDTO() implementieren
// fun Entry.toDTO(): EntryDTO = ...
// Timestamps mit .toString() konvertieren