package org.example.extension

import extension.sanitizeForCsv
import model.Entry

fun Entry.similarity(other: Entry): Double {
    // Jaccard
    if (this.tags == other.tags) return 1.0
    if (this.tags.isEmpty() || other.tags.isEmpty()) return 0.0
    val intersection = this.tags.intersect(other.tags).size
    val union = this.tags.union(other.tags).size
    return intersection/union.toDouble()
}

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