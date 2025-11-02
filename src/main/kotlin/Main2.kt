import kotlinx.coroutines.coroutineScope
import model.UserId
import repository.MoodTrackerRepository
import service.CsvExportService
import service.CsvImportService

suspend fun main() = coroutineScope {
    println("=== MoodTracker CSV Demo ===\n")
    val repo = MoodTrackerRepository()
    repo.initializeWithTestData()
    val exportService = CsvExportService(repo)
    val importService = CsvImportService(repo)

    // 1. Sequence-basierter Export
    println("1. CSV-Export mit Sequences:")
    val csvLines = exportService.exportEntriesToCsvSequence(UserId(1))
        .take(5)  // Nur erste 5 Zeilen (Header + 4 Entries)
        .toList()
    csvLines.forEach { println(it) }
    println()

    // 2. Flow-basierter Import (mit Progress)
    println("2. CSV-Import mit Flow (Progress-Updates):")
    val testCsvLines = listOf(
        "id,userId,createdAt,title,content,moodRating",
        "100,1,2024-01-20T10:00:00,Imported Entry,Test content here,7",
        "101,1,2024-01-21T11:00:00,Another One,More content,6"
    )

    importService.importEntriesFlow(testCsvLines, UserId(1))
        .collect { progress ->
            println("Progress: ${progress.processedLines}/ ${progress.totalLines} | " + "Success: ${progress.successfulImports} | " + "Failed: ${progress.failedImports}")
        }

    println("=== Demo Complete ===")
}
