package com.djatslabs.compoundly.domain.calculator

import com.djatslabs.compoundly.domain.model.CompoundInterestInput
import com.djatslabs.compoundly.domain.model.CompoundInterestResult
import com.djatslabs.compoundly.domain.model.CompoundingFrequency
import com.djatslabs.compoundly.domain.model.YearlyBreakdownItem

class CompoundInterestCalculator {

    fun calculate(input: CompoundInterestInput): CompoundInterestResult {
        val initial = FinancialMathUtils.sanitizeNonNegative(input.initialInvestment)
        val monthly = FinancialMathUtils.sanitizeNonNegative(input.monthlyContribution)
        val rates = FinancialMathUtils.resolveRates(
            ratePercent = input.interestRatePercent,
            period = input.ratePeriod
        )
        val years = input.years.coerceIn(0, FinancialMathUtils.MAX_YEARS)
        val annualRate = rates.annualRate

        if (years == 0) {
            val (principalRatio, interestRatio) = FinancialMathUtils.computeSplitRatios(initial, initial)
            return CompoundInterestResult(
                finalBalance = initial,
                totalPrincipal = initial,
                totalInterest = 0.0,
                principalRatio = principalRatio,
                interestRatio = interestRatio,
                yearlyBreakdown = emptyList()
            )
        }

        val hasContributions = initial > 0.0 || monthly > 0.0
        if (!hasContributions) {
            return CompoundInterestResult()
        }

        var balance = initial
        var cumulativePrincipal = initial
        var cumulativeInterest = 0.0
        val breakdown = ArrayList<YearlyBreakdownItem>(years)
        val monthlyRate = rates.monthlyRate

        for (year in 1..years) {
            var yearlyPrincipalAdded = 0.0
            var yearlyInterestEarned = 0.0

            when (input.frequency) {
                CompoundingFrequency.MONTHLY -> {
                    repeat(MONTHS_PER_YEAR) {
                        val monthInterest = FinancialMathUtils.sanitizeNonNegative(
                            value = balance * monthlyRate,
                            maxValue = Double.MAX_VALUE
                        )
                        balance = FinancialMathUtils.sanitizeNonNegative(
                            value = balance + monthInterest + monthly,
                            maxValue = Double.MAX_VALUE
                        )
                        yearlyInterestEarned += monthInterest
                        yearlyPrincipalAdded += monthly
                    }
                }

                CompoundingFrequency.ANNUALLY -> {
                    yearlyInterestEarned = FinancialMathUtils.sanitizeNonNegative(
                        value = balance * annualRate,
                        maxValue = Double.MAX_VALUE
                    )
                    yearlyPrincipalAdded = monthly * MONTHS_PER_YEAR
                    balance = FinancialMathUtils.sanitizeNonNegative(
                        value = balance + yearlyInterestEarned + yearlyPrincipalAdded,
                        maxValue = Double.MAX_VALUE
                    )
                }
            }

            cumulativePrincipal += yearlyPrincipalAdded
            cumulativeInterest += yearlyInterestEarned

            breakdown.add(
                YearlyBreakdownItem(
                    year = year,
                    endingBalance = FinancialMathUtils.sanitizeNonNegative(balance, Double.MAX_VALUE),
                    yearlyPrincipalAdded = FinancialMathUtils.sanitizeNonNegative(yearlyPrincipalAdded, Double.MAX_VALUE),
                    yearlyInterestEarned = FinancialMathUtils.sanitizeNonNegative(yearlyInterestEarned, Double.MAX_VALUE),
                    cumulativePrincipal = FinancialMathUtils.sanitizeNonNegative(cumulativePrincipal, Double.MAX_VALUE),
                    cumulativeInterest = FinancialMathUtils.sanitizeNonNegative(cumulativeInterest, Double.MAX_VALUE)
                )
            )
        }

        val finalBalance = FinancialMathUtils.sanitizeNonNegative(balance, Double.MAX_VALUE)
        val totalPrincipal = FinancialMathUtils.sanitizeNonNegative(cumulativePrincipal, Double.MAX_VALUE)
        val totalInterest = FinancialMathUtils.sanitizeNonNegative(finalBalance - totalPrincipal, Double.MAX_VALUE)
        val (principalRatio, interestRatio) = FinancialMathUtils.computeSplitRatios(totalPrincipal, finalBalance)

        return CompoundInterestResult(
            finalBalance = finalBalance,
            totalPrincipal = totalPrincipal,
            totalInterest = totalInterest,
            principalRatio = principalRatio,
            interestRatio = interestRatio,
            yearlyBreakdown = breakdown
        )
    }

    private companion object {
        const val MONTHS_PER_YEAR = 12
    }
}
