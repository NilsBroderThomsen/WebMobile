package service

import kotlinx.coroutines.runBlocking
import extension.toCsvLine
import model.UserId
import repository.MoodTrackerRepository

class CsvExportService(private val repository: MoodTrackerRepository) {
    fun exportEntriesToCsvSequence(userId: UserId): Sequence<String> = sequence {
        yield("id,userId,createdAt,title,content,moodRating")
        val entries = runBlocking { repository.findAllEntries(userId) }
        yieldAll( entries.map { it.toCsvLine() })
    }
}