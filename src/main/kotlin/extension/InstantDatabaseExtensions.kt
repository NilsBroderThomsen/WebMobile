package extension

import java.time.Instant as JavaInstant
import kotlin.time.Instant as KotlinInstant

fun KotlinInstant.toJavaInstant(): JavaInstant = JavaInstant.ofEpochMilli(this.toEpochMilliseconds())

fun JavaInstant.toKotlinInstant(): KotlinInstant = KotlinInstant.fromEpochMilliseconds(this.toEpochMilli())
