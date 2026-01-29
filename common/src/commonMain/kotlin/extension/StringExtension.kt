package extension

val String.isValidEmail: Boolean
    get() = this.contains("@") && this.contains(".")

val String.isValidUsername: Boolean
    get() = this.matches(Regex("^[A-Za-z0-9_]{3,20}$"))