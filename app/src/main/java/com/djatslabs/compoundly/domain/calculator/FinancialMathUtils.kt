package com.djatslabs.compoundly.domain.calculator

internal object FinancialMathUtils {
    const val MAX_AMOUNT = 1_000_000_000_000_000.0
    const val MAX_ANNUAL_RATE_PERCENT = 200.0
    const val MAX_YEARS = 100
    const val MAX_MONTHS = 1200

    fun sanitizeNonNegative(value: Double, maxValue: Double = MAX_AMOUNT): Double {
        if (!value.isFinite() || value <= 0.0) return 0.0
        return value.coerceAtMost(maxValue)
    }

    fun computeSplitRatios(principal: Double, total: Double): Pair<Float, Float> {
        val safeTotal = sanitizeNonNegative(total, Double.MAX_VALUE)
        if (safeTotal <= 0.0) return 0f to 0f
        val safePrincipal = sanitizeNonNegative(principal, Double.MAX_VALUE).coerceAtMost(safeTotal)
        val principalRatio = (safePrincipal / safeTotal).toFloat().coerceIn(0f, 1f)
        val interestRatio = (1f - principalRatio).coerceIn(0f, 1f)
        return principalRatio to interestRatio
    }
}
