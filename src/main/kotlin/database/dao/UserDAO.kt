package database.dao

import database.tables.EntriesTable
import database.tables.UsersTable
import model.User
import model.UserId
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class UserDAO(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<UserDAO>(UsersTable)

    var username by UsersTable.username
    var email by UsersTable.email
    var passwordHash by UsersTable.passwordHash
    var registrationDate by UsersTable.registrationDate
    var isActive by UsersTable.isActive

    val entries by EntryDAO referrersOn EntriesTable.userId

    fun toModel(): User = User(
        id = UserId(id.value),
        username = username,
        email = email,
        passwordHash = passwordHash,
        registrationDate = registrationDate,
        isActive = isActive
    )
}