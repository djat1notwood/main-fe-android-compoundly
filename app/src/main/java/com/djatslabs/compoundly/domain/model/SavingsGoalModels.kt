package com.djatslabs.compoundly.domain.model

data class SavingsGoalInput(
    val targetAmount: Double = 0.0,
    val currentSavings: Double = 0.0,
    val annualReturnPercent: Double = 0.0,
    val timeframeValue: Int = 0,
    val timeframeUnit: TimeframeUnit = TimeframeUnit.YEARS
)

enum class SavingsGoalStatus {
    AWAITING_INPUT,
    VALID,
    GOAL_ALREADY_REACHED,
    INVALID_TIMEFRAME
}

data class SavingsGoalResult(
    val requiredMonthlySavings: Double = 0.0,
    val projectedFinalBalance: Double = 0.0,
    val startingSavings: Double = 0.0,
    val totalMonthlyDeposits: Double = 0.0,
    val totalPersonalPrincipal: Double = 0.0,
    val totalInterestEarned: Double = 0.0,
    val principalRatio: Float = 0f,
    val interestRatio: Float = 0f,
    val totalMonths: Int = 0,
    val status: SavingsGoalStatus = SavingsGoalStatus.AWAITING_INPUT
)
