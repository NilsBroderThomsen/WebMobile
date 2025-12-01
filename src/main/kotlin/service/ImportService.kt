package service

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import dto.ExportData
import dto.ImportResult
import kotlinx.serialization.json.Json
import model.EntryBuilder
import model.EntryId
import model.UserId
import database.MoodTrackerDatabaseRepository
import java.time.LocalDateTime

class ImportService(private val repository: MoodTrackerDatabaseRepository) {
    suspend fun importFromJson(jsonData: String, userId: UserId): ImportResult {
        val json = Json { ignoreUnknownKeys = true }
        return try {
            val exportData = json.decodeFromString<ExportData>(jsonData)
            var successful = 0
            var skipped = 0
            var failed = 0
            val errors = mutableListOf<String>()

            val existingTitles = repository.findAllEntries(userId)
                .map { it.title.lowercase() }
                .toMutableSet()

            exportData.entries.forEach { entryDto ->
                val title = entryDto.title.trim()
                val titleKey = title.lowercase()

                if (title.isBlank()) {
                    failed++
                    errors += "Failed to import entry with empty title (id=${entryDto.id})."
                    return@forEach
                }

                if (entryDto.content.isBlank()) {
                    failed++
                    errors += "Failed to import entry '${entryDto.title}': content must not be blank."
                    return@forEach
                }

                if (titleKey in existingTitles) {
                    skipped++
                    errors += "Skipped duplicate title '${entryDto.title}'."
                    return@forEach
                }

                try {
                    val createdAt = LocalDateTime.parse(entryDto.createdAt)
                    val builder = EntryBuilder()
                        .withId(EntryId(entryDto.id))
                        .forUser(userId)
                        .withTitle(title)
                        .withContent(entryDto.content)
                        .createdAt(createdAt)

                    entryDto.moodRating?.let { builder.withMood(it) }

                    entryDto.updatedAt
                        ?.takeIf { it.isNotBlank() }
                        ?.let { builder.updatedAt(LocalDateTime.parse(it)) }

                    val entry = builder.build()
                    repository.createEntry(entry)
                    successful++
                    existingTitles += titleKey
                } catch (e: Exception) {
                    errors += "Failed to import entry '${entryDto.title}': ${e.message}"
                    failed++
                }
            }

            ImportResult(
                successful = successful,
                skipped = skipped,
                failed = failed,
                errors = errors
            )
        } catch (e: Exception) {
            ImportResult(
                successful = 0,
                skipped = 0,
                failed = 1,
                errors = listOf("Failed to parse JSON: ${e.message}")
            )
        }
    }

    suspend fun importFromCsv(csvData: String, userId: UserId): ImportResult {
        return try {
            val rows = csvReader().readAllWithHeader(csvData)
            var successful = 0
            var skipped = 0
            var failed = 0
            val errors = mutableListOf<String>()

            val existingTitles = repository.findAllEntries(userId)
                .map { it.title.lowercase() }
                .toMutableSet()

            rows.forEach { row ->
                val title = row["Title"]?.trim().orEmpty()
                val titleKey = title.lowercase()

                if (title.isBlank()) {
                    failed++
                    errors += "Failed to import CSV entry with empty title."
                    return@forEach
                }

                if (titleKey in existingTitles) {
                    skipped++
                    errors += "Skipped duplicate title '$title'."
                    return@forEach
                }

                try {
                    val id = row["ID"]?.toLongOrNull()
                        ?: throw IllegalArgumentException("Invalid ID value")
                    val content = row["Content"]?.trim()
                        ?: throw IllegalArgumentException("Content missing")
                    if (content.isBlank()) {
                        throw IllegalArgumentException("Content must not be blank")
                    }
                    val createdAtRaw = row["CreatedAt"]?.trim()
                        ?: throw IllegalArgumentException("CreatedAt missing")
                    val createdAt = LocalDateTime.parse(createdAtRaw)
                    val moodRating = row["MoodRating"].orEmpty().trim().takeIf { it.isNotEmpty() }?.toInt()
                    val updatedAt = row["UpdatedAt"].orEmpty().trim().takeIf { it.isNotEmpty() }

                    val builder = EntryBuilder()
                        .withId(EntryId(id))
                        .forUser(userId)
                        .withTitle(title)
                        .withContent(content)
                        .createdAt(createdAt)

                    moodRating?.let { builder.withMood(it) }
                    updatedAt?.let { builder.updatedAt(LocalDateTime.parse(it)) }

                    val entry = builder.build()
                    repository.createEntry(entry)
                    successful++
                    existingTitles += titleKey
                } catch (e: Exception) {
                    failed++
                    errors += "Failed to import CSV entry '$title': ${e.message}"
                }
            }

            ImportResult(
                successful = successful,
                skipped = skipped,
                failed = failed,
                errors = errors
            )
        } catch (e: Exception) {
            ImportResult(
                successful = 0,
                skipped = 0,
                failed = 1,
                errors = listOf("Failed to parse CSV: ${e.message}")
            )
        }
    }
}