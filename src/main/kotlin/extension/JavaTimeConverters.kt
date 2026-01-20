package extension

import java.time.Instant as JavaInstant
import kotlin.time.Instant

fun Instant.toJavaInstant(): JavaInstant = JavaInstant.ofEpochMilli(toEpochMilliseconds())

fun JavaInstant.toKotlinInstant(): Instant =
    Instant.fromEpochSeconds(epochSecond, nano.toLong())
