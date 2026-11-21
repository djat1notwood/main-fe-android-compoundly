package com.djatslabs.compoundly.domain.model

data class CompoundInterestInput(
    val initialInvestment: Double = 0.0,
    val monthlyContribution: Double = 0.0,
    val interestRatePercent: Double = 0.0,
    val ratePeriod: InterestRatePeriod = InterestRatePeriod.ANNUALLY,
    val years: Int = 0,
    val frequency: CompoundingFrequency = CompoundingFrequency.MONTHLY
)

data class YearlyBreakdownItem(
    val year: Int,
    val endingBalance: Double,
    val yearlyPrincipalAdded: Double,
    val yearlyInterestEarned: Double,
    val cumulativePrincipal: Double,
    val cumulativeInterest: Double
)

data class CompoundInterestResult(
    val finalBalance: Double = 0.0,
    val totalPrincipal: Double = 0.0,
    val totalInterest: Double = 0.0,
    val principalRatio: Float = 0f,
    val interestRatio: Float = 0f,
    val yearlyBreakdown: List<YearlyBreakdownItem> = emptyList()
)
