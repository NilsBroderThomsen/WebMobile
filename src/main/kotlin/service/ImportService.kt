package service

import dto.ImportResult
import kotlinx.serialization.json.Json
import model.UserId
import repository.MoodTrackerRepository

class ImportService(private val repository: MoodTrackerRepository) {
    suspend fun importFromJson(jsonData: String, userId: UserId): ImportResult {
        val json = Json { ignoreUnknownKeys = true }
        return try {
            // TODO: JSON zu ExportData deserialisieren
            var successful = 0
            var skipped = 0
            val errors = mutableListOf<String>()
            // TODO: Für jeden Entry in exportData.entries:
            // - Duplikat-Check (Titel bereits vorhanden?)
            // - Wenn Duplikat: skipped++
            // - Wenn neu: Entry erstellen und addEntry()
            // - Bei Fehler: zu errors Liste hinzufügen
            // TODO: ImportResult zurückgeben
            TODO("ImportService.importFromJson implementieren")
        } catch (e: Exception) {
            ImportResult(
                successful = 0,
                skipped = 0,
                failed = 1,
                errors = listOf("Failed to parse JSON: ${e.message}")
            )
        }
    }

    suspend fun importFromCsv(csvData: String, userId: UserId):
            ImportResult {
        return try {
            // TODO: CSV mit csvReader().readAllWithHeader(csvData) parsen
            var successful = 0
            var skipped = 0
            val errors = mutableListOf<String>()
            // TODO: Für jede row:
            // - Felder auslesen (Title, Content, MoodRating, CreatedAt)
            // - Validierung
            // - Duplikat-Check
            // - Entry erstellen und addEntry()
            // TODO: ImportResult zurückgeben
            TODO("ImportService.importFromCsv implementieren")
        }
        catch (e: Exception) {
            ImportResult(
                successful = 0,
                skipped = 0,
                failed = 1,
                errors = listOf("Failed to parse CSV: ${e.message}")
            )
        }
    }
}