package model

import api.MoodTrackerClient
import dto.EntryDto
import dto.UpdateEntryRequest

data class UpdateEntryInput(
    val title: String,
    val content: String,
    val moodRatingInput: String
)

data class UpdateEntryValidation(
    val missingTitle: Boolean,
    val missingContent: Boolean,
    val invalidMoodFormat: Boolean,
    val moodOutOfRange: Boolean
) {
    val hasErrors: Boolean
        get() = missingTitle || missingContent || invalidMoodFormat || moodOutOfRange
}

data class UpdateEntryPayload(
    val title: String,
    val content: String,
    val moodRating: Int?
)

sealed class UpdateEntryPreparation {
    data class ValidationError(val validation: UpdateEntryValidation) : UpdateEntryPreparation()
    data class Ready(val payload: UpdateEntryPayload) : UpdateEntryPreparation()
}

sealed class UpdateEntryResult {
    data class ValidationError(val validation: UpdateEntryValidation) : UpdateEntryResult()
    data class Success(val entry: EntryDto) : UpdateEntryResult()
    data class Failure(val message: String?) : UpdateEntryResult()
}

class UpdateEntryModel(private val client: MoodTrackerClient) {
    fun prepare(input: UpdateEntryInput): UpdateEntryPreparation {
        val trimmedTitle = input.title.trim()
        val trimmedContent = input.content.trim()
        val trimmedMood = input.moodRatingInput.trim()
        val moodRating = if (trimmedMood.isBlank()) {
            null
        } else {
            trimmedMood.toIntOrNull()
        }

        val validation = UpdateEntryValidation(
            missingTitle = trimmedTitle.isBlank(),
            missingContent = trimmedContent.isBlank(),
            invalidMoodFormat = trimmedMood.isNotBlank() && moodRating == null,
            moodOutOfRange = moodRating != null && moodRating !in 1..10
        )

        return if (validation.hasErrors) {
            UpdateEntryPreparation.ValidationError(validation)
        } else {
            UpdateEntryPreparation.Ready(
                UpdateEntryPayload(
                    title = trimmedTitle,
                    content = trimmedContent,
                    moodRating = moodRating
                )
            )
        }
    }

    fun validationMessage(validation: UpdateEntryValidation): String? {
        return when {
            validation.missingTitle || validation.missingContent ->
                "Bitte Title und Content ausfüllen."
            validation.invalidMoodFormat || validation.moodOutOfRange ->
                "Mood Rating muss eine Zahl zwischen 1 und 10 sein."
            else -> null
        }
    }

    suspend fun updateEntry(entryId: Long, input: UpdateEntryInput): UpdateEntryResult {
        return when (val prepared = prepare(input)) {
            is UpdateEntryPreparation.ValidationError ->
                UpdateEntryResult.ValidationError(prepared.validation)
            is UpdateEntryPreparation.Ready ->
                try {
                    val entry = client.updateEntry(
                        entryId = entryId,
                        request = UpdateEntryRequest(
                            title = prepared.payload.title,
                            content = prepared.payload.content,
                            moodRating = prepared.payload.moodRating
                        )
                    )
                    UpdateEntryResult.Success(entry)
                } catch (ex: Exception) {
                    UpdateEntryResult.Failure(ex.message)
                }
        }
    }
}
