package thisissadeghi.kickoff.common

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import thisissadeghi.kickoff.common.Money.Companion.Zero
import thisissadeghi.kickoff.common.Money.Companion.displayFractionDivision
import thisissadeghi.kickoff.common.Money.Companion.displayFractionLength
import thisissadeghi.kickoff.common.Money.Companion.fractionLength
import kotlin.jvm.JvmInline
import kotlin.math.absoluteValue
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * This class has defined to hold integer and fraction part of a float/double value in a class.
 *
 * Money object could be negative or positive that should be compare with [Zero]
 *
 * [integer] and [fraction] always returns absolute value but [value] keeps the class positive/negativeness.
 *
 * Last digits of [value] is fraction and others is used to store integer part.
 */
@Serializable(with = MoneySerializer::class)
@JvmInline
value class Money private constructor(
    internal val value: Long,
) : Comparable<Money> {
    constructor(
        integer: Int,
        fraction: Int,
    ) : this(
        kotlin.run {
            val sign = if (integer < 0) -1L else 1L
            val fractionDivider = 10.0.pow((fraction.length() - fractionLength).coerceAtLeast(0)).toInt()
            val fractionPart = fraction / fractionDivider
            val integerPart = integer.toLong().absoluteValue * fractionTimes

            sign * (integerPart + fractionPart)
        },
    )

    val integer: Int
        get() = (value / fractionTimes).toInt().absoluteValue

    val fraction: Int
        get() = value.rem(fractionTimes).toInt().absoluteValue

    companion object {
        const val fractionLength = 4
        private val fractionTimes = 10.0.pow(fractionLength).roundToInt()
        internal const val displayFractionLength = 2
        internal val displayFractionDivision = 10.0.pow(fractionLength - displayFractionLength).roundToInt()
        val Zero = Money(0, 0)
    }

    override fun compareTo(other: Money): Int = value.compareTo(other.value)

    operator fun plus(money: Money): Money = Money(value + money.value)

    operator fun minus(money: Money): Money = Money(value - money.value)

    operator fun unaryMinus(): Money = Money(-value)

    operator fun times(other: Int): Money = times(other.toDouble())

    operator fun times(other: Double): Money = Money((value * other).toLong())

    operator fun times(other: Float): Money = Money((value * other).toLong())

    override fun toString(): String = "Money($integer.$fraction($value))"
}

fun Money.format(
    currency: Currency? = null,
    round: Boolean = true,
): String {
    val roundedMoney =
        if (fraction.rem(displayFractionDivision) > 0 && round) {
            if (value > 0) {
                this + Money(0, displayFractionDivision)
            } else {
                this - Money(0, displayFractionDivision)
            }
        } else {
            this
        }

    return roundedMoney.internalFormat(currency)
}

fun Money.format(
    currencySymbol: String,
    round: Boolean = true,
): String {
    val roundedMoney =
        if (fraction.rem(displayFractionDivision) > 0 && round) {
            if (value > 0) {
                this + Money(0, displayFractionDivision)
            } else {
                this - Money(0, displayFractionDivision)
            }
        } else {
            this
        }

    return roundedMoney.internalFormat(currencySymbol)
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun Money.internalFormat(currency: Currency?): String =
    buildString {
        append(integer)
        for (i in length - 3 downTo 1 step 3) {
            insert(i, ',')
        }

        append('.')
        append((fraction / displayFractionDivision).toString().padStart(displayFractionLength, '0'))

        if (currency != null) {
            insert(0, currency.symbol)
        }
        if (value < 0) {
            insert(0, '-')
        }
    }

@Suppress("NOTHING_TO_INLINE")
internal inline fun Money.internalFormat(currencySymbol: String): String =
    buildString {
        append(integer)
        for (i in length - 3 downTo 1 step 3) {
            insert(i, ',')
        }

        append('.')
        append((fraction / displayFractionDivision).toString().padStart(displayFractionLength, '0'))

        insert(0, currencySymbol)
        if (value < 0) {
            insert(0, '-')
        }
    }

fun String.toMoneyOrNull(): Money? {
    if (isBlank()) return null
    val values = split(".")
    return Money(
        integer =
            values
                .getOrNull(0)
                ?.filter { it.isDigit() || it == '-' }
                ?.toIntOrNull() ?: 0,
        fraction =
            values
                .getOrNull(1)
                ?.plus("0".repeat(fractionLength)) // fill with 0 when fraction length is lower than fractionMaxLength
                ?.take(fractionLength) // truncate unused chars
                ?.toIntOrNull() ?: 0,
    )
}

fun Int.length(): Int {
    check(this >= 0)
    return if (this == 0) 1 else (log10(this.toDouble()) + 1).toInt()
}

/**
 * Serializer for Money type that converts to/from String for API compatibility.
 * Serializes as "123.45" format (no currency symbol).
 */
object MoneySerializer : KSerializer<Money> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Money", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Money = decoder.decodeString().toMoneyOrNull() ?: Money.Zero

    override fun serialize(
        encoder: Encoder,
        value: Money,
    ) = encoder.encodeString(value.formatAsString())
}

/**
 * Formats Money as a string without rounding for serialization/API calls.
 * Returns format like "123.45" (no currency symbol).
 */
fun Money.formatAsString(): String = format(round = false)
