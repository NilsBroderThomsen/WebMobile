package service

import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import database.MoodTrackerDatabaseRepository
import dto.ExportData
import extension.toExportDto
import kotlinx.serialization.json.Json
import model.UserId
import kotlinx.datetime.Clock

class ExportService(private val repository: MoodTrackerDatabaseRepository) {
    suspend fun exportToJson(userId: UserId, prettyPrint: Boolean = true): String {
        val entries = repository.findAllEntries(userId)
        val exportData = ExportData(
            exportDate = kotlin.time.Clock.System.now().toString(),
            userId = userId.value,
            totalEntries = entries.size,
            entries = entries.map { it.toExportDto() }
        )

        val json = Json {
            this.prettyPrint = prettyPrint
            encodeDefaults = true
        }

        return json.encodeToString(exportData)
    }

    suspend fun exportToCsv(userId: UserId): String {
        val entries = repository.findAllEntries(userId)
        val header = listOf("ID", "Title", "Content", "MoodRating", "CreatedAt", "UpdatedAt")
        val rows = entries.map { entry ->
            listOf(
                entry.id.value.toString(),
                entry.title,
                entry.content.replace("\n", " "),
                entry.moodRating?.toString() ?: "",
                entry.createdAt.toString(),
                entry.updatedAt?.toString() ?: ""
            )
        }

        return csvWriter().writeAllAsString(listOf(header) + rows)
    }
}