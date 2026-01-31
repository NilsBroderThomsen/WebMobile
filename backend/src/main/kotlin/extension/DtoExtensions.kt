package extension

import dto.EntryDto
import dto.UserDto
import model.Entry
import model.User

fun Entry.toDto(): EntryDto = EntryDto(
    id = this.id.value,
    userId = this.userId.value,
    title = this.title,
    content = this.content,
    moodRating = this.moodRating,
    createdAt = this.createdAt.toString(),
    updatedAt = this.updatedAt.toString()
)

fun User.toDto(): UserDto = UserDto(
    id = this.id.value,
    username = this.username,
    email = this.email,
    registrationDate = this.registrationDate.toString(),
    isActive = this.isActive
)
