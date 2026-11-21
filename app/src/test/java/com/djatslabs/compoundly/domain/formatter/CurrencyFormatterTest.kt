package com.djatslabs.compoundly.domain.formatter

import androidx.compose.ui.text.AnnotatedString
import com.djatslabs.compoundly.domain.model.AppCurrency
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CurrencyFormatterTest {

    @Test
    fun formatCurrency_handlesAllCurrenciesAndInvalidValues() {
        val idr = CurrencyFormatter.formatCurrency(10_000_000.0, AppCurrency.IDR)
        val usd = CurrencyFormatter.formatCurrency(1234.5, AppCurrency.USD)
        val eur = CurrencyFormatter.formatCurrency(1234.5, AppCurrency.EUR)

        assertTrue("Expected Rp in $idr", idr.contains("Rp"))
        assertTrue("Expected grouped 10.000.000 in $idr", idr.contains("10.000.000"))
        assertEquals("$1,234.50", usd)
        assertEquals("€1,234.50", eur)

        val nanFormatted = CurrencyFormatter.formatCurrency(Double.NaN, AppCurrency.USD)
        val negFormatted = CurrencyFormatter.formatCurrency(-50.0, AppCurrency.USD)
        assertEquals("$0.00", nanFormatted)
        assertEquals("$0.00", negFormatted)
    }

    @Test
    fun sanitizeCurrencyInput_handlesIdrAndDecimalCurrenciesSafely() {
        assertEquals("1000000", CurrencyFormatter.sanitizeCurrencyInput("1.000.000", AppCurrency.IDR))
        assertEquals("0", CurrencyFormatter.sanitizeCurrencyInput("000", AppCurrency.IDR))
        assertEquals("1234.56", CurrencyFormatter.sanitizeCurrencyInput("1,234.56", AppCurrency.USD))
        assertEquals("1234.56", CurrencyFormatter.sanitizeCurrencyInput("1.234,56", AppCurrency.EUR))
        assertEquals("1234", CurrencyFormatter.sanitizeCurrencyInput("1,234", AppCurrency.USD))
        assertEquals("1.02", CurrencyFormatter.sanitizeCurrencyInput("1.023", AppCurrency.USD))
        assertEquals("0.5", CurrencyFormatter.sanitizeCurrencyInput(",5", AppCurrency.USD))
    }

    @Test
    fun sanitizeRateInput_normalizesCommaAndDecimalPlaces() {
        assertEquals("7.5", CurrencyFormatter.sanitizeRateInput("7,5"))
        assertEquals("7.25", CurrencyFormatter.sanitizeRateInput("007.259"))
        assertEquals("0.75", CurrencyFormatter.sanitizeRateInput(".75"))
    }

    @Test
    fun visualTransformation_maintainsValidMonotonicOffsetMapping() {
        val samples = listOf(
            "",
            "0",
            "5",
            "12",
            "123",
            "1234",
            "1000000",
            "1234567890",
            "1234.5",
            "1234567.89"
        )

        for (currency in AppCurrency.entries) {
            val transformation = ThousandSeparatorVisualTransformation(currency)
            for (raw in samples) {
                val input = if (currency == AppCurrency.IDR && raw.contains('.')) {
                    raw.substringBefore('.')
                } else {
                    raw
                }
                val result = transformation.filter(AnnotatedString(input))
                val transformedLength = result.text.length
                val mapping = result.offsetMapping

                var prevTransformed = -1
                for (origOffset in 0..input.length) {
                    val mapped = mapping.originalToTransformed(origOffset)
                    assertTrue(
                        "originalToTransformed($origOffset) out of range 0..$transformedLength for '$input' ($currency)",
                        mapped in 0..transformedLength
                    )
                    assertTrue(
                        "originalToTransformed not monotonic at $origOffset for '$input' ($currency)",
                        mapped >= prevTransformed
                    )
                    prevTransformed = mapped
                }

                var prevOriginal = -1
                for (transOffset in 0..transformedLength) {
                    val mapped = mapping.transformedToOriginal(transOffset)
                    assertTrue(
                        "transformedToOriginal($transOffset) out of range 0..${input.length} for '$input' ($currency)",
                        mapped in 0..input.length
                    )
                    assertTrue(
                        "transformedToOriginal not monotonic at $transOffset for '$input' ($currency)",
                        mapped >= prevOriginal
                    )
                    prevOriginal = mapped
                }
            }
        }
    }
}
