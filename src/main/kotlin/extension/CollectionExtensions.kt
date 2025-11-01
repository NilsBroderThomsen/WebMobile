package org.example.extension

import extension.normalizeTag
import model.Entry

fun List<Entry>.entriesWithMood(): List<Entry> =
    this.filter { it.moodRating != null }

fun List<Entry>.averageMood(): Double {
    val ratings = mapNotNull { it.moodRating }
    return if (ratings.isEmpty()) 0.0 else ratings.average()
}

fun List<Entry>.entriesWithTag(tag: String): List<Entry> =
    this.filter { it.tags.contains(tag.normalizeTag()) }
