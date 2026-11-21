package com.djatslabs.compoundly.domain.model

import java.util.Locale

enum class AppCurrency(
    val code: String,
    val symbol: String,
    val shortLabel: String,
    val locale: Locale,
    val maxFractionDigits: Int
) {
    IDR(
        code = "IDR",
        symbol = "Rp",
        shortLabel = "Rp",
        locale = Locale.forLanguageTag("id-ID"),
        maxFractionDigits = 0
    ),
    USD(
        code = "USD",
        symbol = "$",
        shortLabel = "$",
        locale = Locale.US,
        maxFractionDigits = 2
    ),
    EUR(
        code = "EUR",
        symbol = "€",
        shortLabel = "€",
        locale = Locale.forLanguageTag("en-IE"),
        maxFractionDigits = 2
    )
}
