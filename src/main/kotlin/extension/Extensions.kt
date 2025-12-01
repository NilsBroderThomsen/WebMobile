package extension

fun Int.isValidMoodRating(): Boolean = this in 1..10

fun String.normalizeTag(): String = this.trim().lowercase()
