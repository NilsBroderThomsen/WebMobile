package security

import org.mindrot.jbcrypt.BCrypt

object PasswordHasher {
    fun hash(password: String): String = BCrypt.hashpw(password, BCrypt.gensalt())

    fun verify(password: String, passwordHash: String): Boolean = BCrypt.checkpw(password, passwordHash)
}
