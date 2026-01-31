package viewmodel

import api.MoodTrackerClient
import dto.CreateEntryRequest
import dto.EntryDto

data class CreateEntryInput(
    val title: String,
    val content: String,
    val moodRatingInput: String
)

data class CreateEntryValidation(
    val missingTitle: Boolean,
    val missingContent: Boolean,
    val invalidMoodFormat: Boolean,
    val moodOutOfRange: Boolean
) {
    val hasErrors: Boolean
        get() = missingTitle || missingContent || invalidMoodFormat || moodOutOfRange
}

data class CreateEntryPayload(
    val title: String,
    val content: String,
    val moodRating: Int?
)

sealed class CreateEntryPreparation {
    data class ValidationError(val validation: CreateEntryValidation) : CreateEntryPreparation()
    data class Ready(val payload: CreateEntryPayload) : CreateEntryPreparation()
}

sealed class CreateEntryResult {
    data class ValidationError(val validation: CreateEntryValidation) : CreateEntryResult()
    data class Success(val entry: EntryDto) : CreateEntryResult()
    data class Failure(val message: String?) : CreateEntryResult()
}

class CreateEntryViewModel(private val client: MoodTrackerClient) {

    companion object {
        private val MOOD_RANGE = 1..10
    }

    fun prepare(input: CreateEntryInput): CreateEntryPreparation {
        val trimmedTitle = input.title.trim()
        val trimmedContent = input.content.trim()
        val trimmedMood = input.moodRatingInput.trim()

        val moodRating = when {
            trimmedMood.isBlank() -> null
            else -> trimmedMood.toIntOrNull()
        }

        val validation = CreateEntryValidation(
            missingTitle = trimmedTitle.isBlank(),
            missingContent = trimmedContent.isBlank(),
            invalidMoodFormat = trimmedMood.isNotBlank() && moodRating == null,
            moodOutOfRange = moodRating != null && moodRating !in MOOD_RANGE
        )

        return if (validation.hasErrors) {
            CreateEntryPreparation.ValidationError(validation)
        } else {
            CreateEntryPreparation.Ready(
                CreateEntryPayload(
                    title = trimmedTitle,
                    content = trimmedContent,
                    moodRating = moodRating
                )
            )
        }
    }

    suspend fun createEntry(userId: Long, input: CreateEntryInput): CreateEntryResult {
        return when (val prepared = prepare(input)) {
            is CreateEntryPreparation.ValidationError ->
                CreateEntryResult.ValidationError(prepared.validation)

            is CreateEntryPreparation.Ready ->
                try {
                    val entry = client.createEntry(
                        userId = userId,
                        request = CreateEntryRequest(
                            title = prepared.payload.title,
                            content = prepared.payload.content,
                            moodRating = prepared.payload.moodRating
                        )
                    )
                    CreateEntryResult.Success(entry)
                } catch (ex: Exception) {
                    CreateEntryResult.Failure(ex.message ?: "Unable to create entry.")
                }
        }
    }
}
