package dto

import kotlinx.serialization.*

// DTO for export
@Serializable
data class ExportData(
    val exportDate: String,
    val userId: Long,
    val totalEntries: Int,
    val entries: List<EntryExportDto>
)

@Serializable
data class EntryExportDto(
    val id: Long,
    val title: String,
    val content: String,
    val moodRating: Int?,
    val createdAt: String,
    val updatedAt: String?
)
