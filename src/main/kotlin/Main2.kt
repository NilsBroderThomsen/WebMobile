import database.DatabaseFactory
import database.MoodTrackerDatabaseRepository
import kotlinx.coroutines.coroutineScope
import model.Entry
import model.EntryId
import model.User
import model.UserId
import service.CsvExportService
import service.CsvImportService
import java.time.LocalDate
import java.time.LocalDateTime

suspend fun main() = coroutineScope {
    println("=== MoodTracker CSV Demo ===\n")
    DatabaseFactory.init()
    val repo = MoodTrackerDatabaseRepository()
    val defaultUser = repo.findUserById(UserId(1)) ?: repo.createUser(
        User(UserId(0), "DemoUser", "demo@example.com", "password", LocalDate.now())
    )

    if (repo.findAllEntries(defaultUser.id).isEmpty()) {
        repo.createEntry(
            Entry(
                EntryId(System.currentTimeMillis()),
                defaultUser.id,
                "First entry",
                "Getting started with persistence",
                8,
                LocalDateTime.now(),
                null,
                emptySet()
            )
        )
    }

    val exportService = CsvExportService(repo)
    val importService = CsvImportService(repo)

    // 1. Sequence-basierter Export
    println("1. CSV-Export mit Sequences:")
    val csvLines = exportService.exportEntriesToCsvSequence(defaultUser.id)
        .take(5)  // Nur erste 5 Zeilen (Header + 4 Entries)
        .toList()
    csvLines.forEach { println(it) }
    println()

    // 2. Flow-basierter Import (mit Progress)
    println("2. CSV-Import mit Flow (Progress-Updates):")
    val testCsvLines = listOf(
        "id,userId,createdAt,title,content,moodRating",
        "100,${defaultUser.id.value},2024-01-20T10:00:00,Imported Entry,Test content here,7",
        "101,${defaultUser.id.value},2024-01-21T11:00:00,Another One,More content,6"
    )

    importService.importEntriesFlow(testCsvLines, defaultUser.id)
        .collect { progress ->
            println("Progress: ${progress.processedLines}/ ${progress.totalLines} | " + "Success: ${progress.successfulImports} | " + "Failed: ${progress.failedImports}")
        }

    println("=== Demo Complete ===")
}
