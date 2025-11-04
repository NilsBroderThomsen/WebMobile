package service

import model.UserId
import repository.MoodTrackerRepository

class ExportService(private val repository: MoodTrackerRepository) {
    suspend fun exportToJson(userId: UserId, prettyPrint: Boolean = true): String {
        // TODO: Entries aus Repository holen
        // TODO: ExportData Objekt erstellen mit:
        //  - exportDate = LocalDateTime.now().toString()
        //  - userId = userId.value
        //  - totalEntries = entries.size
        //  - entries = entries.map { it.toExportDto() }
        // TODO: Json Instance erstellen mit Config (prettyPrint, encodeDefaults)
        // TODO: ExportData zu JSON String serialisieren
        TODO("ExportService.exportToJson implementieren")
    }

    suspend fun exportToCsv(userId: UserId): String {
        // TODO: Entries aus Repository holen
        // TODO: CSV mit csvWriter().writeAllAsString() erstellen
        // Header: listOf("ID", "Title", "Content", "MoodRating", "CreatedAt", "UpdatedAt")
        // Rows: entries.map { entry -> listOf(...) }
        // WICHTIG: content.replace("\n", " ") für CSV
        TODO("ExportService.exportToCsv implementieren")
    }
}