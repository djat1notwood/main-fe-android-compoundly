package com.djatslabs.compoundly.domain.formatter

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.djatslabs.compoundly.domain.model.AppCurrency
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Currency

object CurrencyFormatter {

    private const val MAX_INTEGER_DIGITS_IDR = 15
    private const val MAX_INTEGER_DIGITS_DECIMAL = 13

    fun formatCurrency(amount: Double, currency: AppCurrency): String {
        val safeAmount = if (!amount.isFinite() || amount <= 0.0) 0.0 else amount
        val formatter = NumberFormat.getCurrencyInstance(currency.locale).apply {
            this.currency = Currency.getInstance(currency.code)
            minimumFractionDigits = currency.maxFractionDigits
            maximumFractionDigits = currency.maxFractionDigits
        }
        return formatter.format(safeAmount)
    }

    fun sanitizeCurrencyInput(raw: String, currency: AppCurrency): String {
        if (raw.isEmpty()) return ""
        val allowDecimal = currency.maxFractionDigits > 0
        if (!allowDecimal) {
            val digitsOnly = buildString {
                for (ch in raw) {
                    if (ch in '0'..'9') append(ch)
                }
            }
            return trimLeadingZeros(digitsOnly).take(MAX_INTEGER_DIGITS_IDR)
        }

        val normalized = normalizeDecimalInput(
            raw = raw,
            maxIntegerDigits = MAX_INTEGER_DIGITS_DECIMAL,
            maxFractionDigits = currency.maxFractionDigits
        )
        return normalized
    }

    fun sanitizeRateInput(raw: String): String {
        if (raw.isEmpty()) return ""
        return normalizeDecimalInput(
            raw = raw,
            maxIntegerDigits = 3,
            maxFractionDigits = 2
        )
    }

    fun sanitizeWholeNumberInput(raw: String, maxDigits: Int = 4): String {
        if (raw.isEmpty()) return ""
        val digitsOnly = buildString {
            for (ch in raw) {
                if (ch in '0'..'9') append(ch)
            }
        }
        return trimLeadingZeros(digitsOnly).take(maxDigits)
    }

    fun parseDouble(raw: String): Double {
        if (raw.isBlank() || raw == ".") return 0.0
        val parsed = raw.toDoubleOrNull() ?: return 0.0
        return if (parsed.isFinite() && parsed > 0.0) parsed else 0.0
    }

    fun parseInt(raw: String): Int {
        if (raw.isBlank()) return 0
        return raw.toIntOrNull()?.coerceAtLeast(0) ?: 0
    }

    private fun normalizeDecimalInput(
        raw: String,
        maxIntegerDigits: Int,
        maxFractionDigits: Int
    ): String {
        val cleanedRaw = preprocessGroupingSeparators(raw)
        val sb = StringBuilder()
        var hasDot = false
        var intCount = 0
        var fracCount = 0

        for (ch in cleanedRaw) {
            when {
                ch in '0'..'9' -> {
                    if (!hasDot) {
                        if (intCount < maxIntegerDigits) {
                            sb.append(ch)
                            intCount++
                        }
                    } else if (fracCount < maxFractionDigits) {
                        sb.append(ch)
                        fracCount++
                    }
                }

                (ch == '.' || ch == ',') && !hasDot -> {
                    if (sb.isEmpty()) {
                        sb.append('0')
                        intCount = 1
                    }
                    sb.append('.')
                    hasDot = true
                }
            }
        }

        val result = sb.toString()
        val dotIdx = result.indexOf('.')
        return if (dotIdx >= 0) {
            val intPart = trimLeadingZeros(result.substring(0, dotIdx)).ifEmpty { "0" }
            intPart + result.substring(dotIdx)
        } else {
            trimLeadingZeros(result)
        }
    }

    private fun preprocessGroupingSeparators(raw: String): String {
        val lastDot = raw.lastIndexOf('.')
        val lastComma = raw.lastIndexOf(',')
        val dotCount = raw.count { it == '.' }
        val commaCount = raw.count { it == ',' }

        return when {
            lastDot >= 0 && lastComma >= 0 -> {
                if (lastComma > lastDot) {
                    // Format like 1.234,56 -> strip grouping dots, keep comma as decimal
                    raw.replace(".", "")
                } else {
                    // Format like 1,234.56 -> strip grouping commas, keep dot as decimal
                    raw.replace(",", "")
                }
            }

            commaCount > 1 -> raw.replace(",", "")
            dotCount > 1 -> raw.replace(".", "")
            commaCount == 1 && raw.substringAfter(',').length == 3 && raw.substringBefore(',').isNotEmpty() -> {
                raw.replace(",", "")
            }
            else -> raw
        }
    }

    private fun trimLeadingZeros(digits: String): String {
        if (digits.isEmpty()) return ""
        val nonZeroIndex = digits.indexOfFirst { it != '0' }
        return if (nonZeroIndex < 0) "0" else digits.substring(nonZeroIndex)
    }
}

class ThousandSeparatorVisualTransformation(
    private val currency: AppCurrency
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val original = text.text
        if (original.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val symbols = DecimalFormatSymbols.getInstance(currency.locale)
        val groupingSeparator = symbols.groupingSeparator
        val decimalSeparator = symbols.decimalSeparator

        val dotIndex = original.indexOf('.')
        val intPart = if (dotIndex >= 0) original.substring(0, dotIndex) else original
        val fracPart = if (dotIndex >= 0) original.substring(dotIndex) else ""

        val origToTrans = IntArray(original.length + 1)
        val transToOrig = ArrayList<Int>(original.length + 8)
        val transformed = StringBuilder(original.length + 8)

        for (i in intPart.indices) {
            origToTrans[i] = transformed.length
            transformed.append(intPart[i])
            transToOrig.add(i)

            val remainingDigits = intPart.length - 1 - i
            if (remainingDigits > 0 && remainingDigits % 3 == 0) {
                transformed.append(groupingSeparator)
                transToOrig.add(i + 1)
            }
        }

        for (j in fracPart.indices) {
            val origIndex = intPart.length + j
            origToTrans[origIndex] = transformed.length
            val ch = fracPart[j]
            transformed.append(if (ch == '.') decimalSeparator else ch)
            transToOrig.add(origIndex)
        }

        origToTrans[original.length] = transformed.length
        transToOrig.add(original.length)

        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val clamped = offset.coerceIn(0, original.length)
                return origToTrans[clamped]
            }

            override fun transformedToOriginal(offset: Int): Int {
                val clamped = offset.coerceIn(0, transformed.length)
                return transToOrig[clamped]
            }
        }

        return TransformedText(AnnotatedString(transformed.toString()), mapping)
    }
}
