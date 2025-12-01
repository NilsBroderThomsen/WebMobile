package extension

import dto.EntryDTO
import dto.EntryExportDto
import dto.UserDTO
import model.Entry
import model.User

fun Entry.toDTO(): EntryDTO = EntryDTO(
    id = this.id.value,
    userId = this.userId.value,
    title = this.title,
    content = this.content,
    moodRating = this.moodRating,
    createdAt = this.createdAt.toString(),
    updatedAt = this.updatedAt?.toString()
)

fun Entry.toExportDto(): EntryExportDto = EntryExportDto(
    id = this.id.value,
    title = this.title,
    content = this.content,
    moodRating = this.moodRating,
    createdAt = this.createdAt.toString(),
    updatedAt = this.updatedAt?.toString()
)

fun User.toDTO(): UserDTO = UserDTO(
    id = this.id.value,
    username = this.username,
    email = this.email,
    registrationDate = this.registrationDate.toString(),
    isActive = this.isActive
)
