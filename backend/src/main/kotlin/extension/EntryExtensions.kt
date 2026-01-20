package extension

import model.Entry
fun Entry.toCsvLine(): String {
    return listOf(
        id.value,
        userId.value,
        createdAt.toString(),
        title.sanitizeForCsv(),
        content.sanitizeForCsv(),
        moodRating?.toString() ?: ""
    ).joinToString(",")
}