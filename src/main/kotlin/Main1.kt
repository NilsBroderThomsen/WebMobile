import extension.averageMood
import extension.entriesWithTag
import extension.entriesWithMood
import extension.daysSince
import extension.isInPast
import extension.isToday
import extension.isValidEmail
import extension.isValidUsername
import extension.normalizeTag
import extension.toDateString
import extension.toDateTimeString
import extension.toMoodLevel
import model.Entry
import model.EntryId
import model.MoodLevel
import model.User
import model.UserId
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

fun main() {
    println("=== MoodTracker Demo ===\n")

    val user = User(
        id = UserId(1),
        username = "alice_123",
        email = "alice@example.com",
        passwordHash = "hashed-password",
        registrationDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
    )

    println("User erstellt: ${user.username} (${user.email})")
    println("Gültiger Benutzername: ${user.username.isValidUsername}")
    println("Gültige E-Mail: ${user.email.isValidEmail}")
    println(
        "Account-Alter: ${user.accountAge} Tage, Neuer User: ${user.isNewUser}"
    )
    println(
        "Registriert heute? ${user.registrationDate.isToday()} | In der Vergangenheit? ${user.registrationDate.isInPast()} | Tage seit Registrierung: ${user.registrationDate.daysSince()}"
    )

    val deactivatedUser = user.deactivate()
    println("Account nach Deaktivierung aktiv? ${deactivatedUser.isActive}")
    val reactivatedUser = deactivatedUser.activate()
    println("Account nach Reaktivierung aktiv? ${reactivatedUser.isActive}\n")

    val entry1 = Entry(
        id = EntryId(1),
        userId = user.id,
        title = "Toller Tag",
        content = "Heute war ein toller Tag mit viel Sonne und einem langen Spaziergang im Park.",
        moodRating = 8,
        createdAt = Clock.System.now() - 5.hours,
        tags = setOf("sport", "freunde")
    )

    val entry2 = Entry(
        id = EntryId(2),
        userId = user.id,
        title = "Stressiger Tag",
        content = "Viele Meetings und kaum Zeit für Pausen.",
        moodRating = 3,
        createdAt = Clock.System.now() - 1.days - 9.hours - 30.minutes,
        tags = setOf("arbeit")
    )

    val entry3 = Entry(
        id = EntryId(3),
        userId = user.id,
        title = "Ruhiger Abend",
        content = "Lesen eines Buches und entspannen auf dem Sofa.",
        moodRating = null,
        createdAt = Clock.System.now() - 1.days - 21.hours,
        tags = setOf("entspannung")
    )

    println("Entry erstellt: \"${entry1.title}\"")
    val entry1Mood = entry1.moodRating?.toMoodLevel()
    println(
        "Wörter: ${entry1.wordCount}, Stimmung: ${entry1.moodRating} (${entry1Mood?.emoji} ${entry1Mood?.displayName}), Tags: ${entry1.tags}"
    )
    println("Erstellt am: ${entry1.createdAt.toDateTimeString()}")

    println("Entry mit schlechter Stimmung: \"${entry2.title}\"")
    val entry2Mood = entry2.moodRating?.toMoodLevel()
    println(
        "Wörter: ${entry2.wordCount}, Stimmung: ${entry2.moodRating} (${entry2Mood?.emoji} ${entry2Mood?.displayName}), Tags: ${entry2.tags}"
    )

    val editedEntry = entry1.updateContent("Der Tag war großartig und voller Energie!")
        .updateMood(9)
        .addTag("Meditation")
    println("Nach Update bearbeitet? ${editedEntry.isEdited} | Neue Stimmung: ${editedEntry.moodRating}")
    println("Alle Tags nach Hinzufügen: ${editedEntry.tags}")

    val trimmedTag = "  Sport  ".normalizeTag()
    println("Normalisierter Tag von '  Sport  ': $trimmedTag")

    val entries = listOf(entry1, entry2, entry3, editedEntry)
    val entriesWithMood = entries.entriesWithMood()
    println("Einträge mit Stimmung: ${entriesWithMood.size} von ${entries.size}")
    println("Durchschnittsstimmung: ${"%.2f".format(entriesWithMood.averageMood())}")
    println("Einträge mit guter Stimmung (>=7): ${entriesWithMood.count { it.hasGoodMood }}")
    println("Einträge mit schlechter Stimmung (<=3): ${entriesWithMood.count { it.hasPoorMood }}")
    println("Einträge mit Tag 'Sport': ${entries.entriesWithTag("Sport").size}")

    println("\nMoodLevel-Test:")
    listOf(2, 8, 10).forEach { rating ->
        val level = MoodLevel.fromRating(rating)
        println("- Rating $rating → ${level?.emoji} ${level?.displayName}")
    }

    println("\nDatumsformatierung:")
    println("Entry 1 Datum: ${entry1.createdAt.toDateString()}")
    println("Entry 1 Datum/Zeit: ${entry1.createdAt.toDateTimeString()}")

    println("\n=== Demo Complete ===")
}
