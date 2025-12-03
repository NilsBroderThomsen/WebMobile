package dto

import kotlinx.serialization.*
import serialization.LocalDateSerializer
import java.time.LocalDate

// TODO: UserDTO data class erstellen
// @Serializable
// Felder: id (Long), username, email, registrationDate (String), isActive
// WICHTIG: KEIN passwordHash aus Sicherheitsgründen!
// TODO: Extension function User.toDTO() implementieren
// fun User.toDTO(): UserDTO = ...
// registrationDate mit .toString() konvertieren