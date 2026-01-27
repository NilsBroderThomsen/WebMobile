package dto

import kotlinx.serialization.*

// DTO für Import
@Serializable
data class ImportResult(
    val successful: Int,
    val skipped: Int,
    val failed: Int,
    val errors: List<String>
)