package service

import extension.parseEntryFromCsv
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import model.UserId
import database.MoodTrackerDatabaseRepository

data class ImportProgress(
    val totalLines: Int,
    val processedLines: Int,
    val successfulImports: Int,
    val failedImports: Int
) {
    val isComplete: Boolean = processedLines == totalLines
    val successRate: Double =
        if (processedLines > 0) successfulImports.toDouble() /
                processedLines else 0.0
}

class CsvImportService(private val repository: MoodTrackerDatabaseRepository) {
    fun importEntriesFlow(lines: List<String>, targetUserId: UserId): Flow<ImportProgress> = flow {
        val dataLines = lines.drop(1).filter { it.isNotBlank() }
        val total = dataLines.size
        var processed = 0
        var success = 0
        var fail = 0

        dataLines.forEachIndexed { index, line ->
            delay(50)
            processed++
            try {
                val entry = line.parseEntryFromCsv(targetUserId)
                repository.createEntry(entry)
                success++
            } catch (_: Exception ) {
                fail++
            } finally {
                emit(ImportProgress(total, processed, success, fail))
            }
        }
    }
}
