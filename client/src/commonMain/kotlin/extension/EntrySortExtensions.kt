package extension

import dto.EntryDto

enum class EntrySortOrder {
    ASC,
    DESC
}

fun List<EntryDto>.sortedByCreatedAt(order: EntrySortOrder): List<EntryDto> = when (order) {
    EntrySortOrder.ASC -> sortedBy { it.createdAt }
    EntrySortOrder.DESC -> sortedByDescending { it.createdAt }
}