package com.djatslabs.compoundly.domain.calculator

import com.djatslabs.compoundly.domain.model.SavingsGoalInput
import com.djatslabs.compoundly.domain.model.SavingsGoalResult
import com.djatslabs.compoundly.domain.model.SavingsGoalStatus
import com.djatslabs.compoundly.domain.model.TimeframeUnit
import kotlin.math.pow

class SavingsGoalCalculator {

    fun calculate(input: SavingsGoalInput): SavingsGoalResult {
        val target = FinancialMathUtils.sanitizeNonNegative(input.targetAmount)
        val current = FinancialMathUtils.sanitizeNonNegative(input.currentSavings)
        val rates = FinancialMathUtils.resolveRates(
            ratePercent = input.returnRatePercent,
            period = input.ratePeriod
        )
        val totalMonths = when (input.timeframeUnit) {
            TimeframeUnit.MONTHS -> input.timeframeValue.coerceIn(0, FinancialMathUtils.MAX_MONTHS)
            TimeframeUnit.YEARS -> (input.timeframeValue.coerceIn(0, FinancialMathUtils.MAX_YEARS) * MONTHS_PER_YEAR)
        }

        if (target <= 0.0) {
            return SavingsGoalResult(
                startingSavings = current,
                totalMonths = totalMonths,
                status = SavingsGoalStatus.AWAITING_INPUT
            )
        }

        if (totalMonths <= 0) {
            return SavingsGoalResult(
                startingSavings = current,
                totalMonths = 0,
                status = SavingsGoalStatus.INVALID_TIMEFRAME
            )
        }

        val monthlyRate = rates.monthlyRate
        val growthFactor = (1.0 + monthlyRate).pow(totalMonths)
        val futureValueCurrent = if (growthFactor.isFinite()) {
            FinancialMathUtils.sanitizeNonNegative(current * growthFactor, Double.MAX_VALUE)
        } else {
            Double.MAX_VALUE
        }

        if (target <= futureValueCurrent) {
            val interestEarned = FinancialMathUtils.sanitizeNonNegative(
                value = futureValueCurrent - current,
                maxValue = Double.MAX_VALUE
            )
            val (principalRatio, interestRatio) = FinancialMathUtils.computeSplitRatios(
                principal = current,
                total = futureValueCurrent
            )
            return SavingsGoalResult(
                requiredMonthlySavings = 0.0,
                projectedFinalBalance = futureValueCurrent,
                startingSavings = current,
                totalMonthlyDeposits = 0.0,
                totalPersonalPrincipal = current,
                totalInterestEarned = interestEarned,
                principalRatio = principalRatio,
                interestRatio = interestRatio,
                totalMonths = totalMonths,
                status = SavingsGoalStatus.GOAL_ALREADY_REACHED
            )
        }

        val remainingGoal = target - futureValueCurrent
        val requiredMonthly = if (monthlyRate == 0.0) {
            remainingGoal / totalMonths
        } else {
            val annuityFactor = (growthFactor - 1.0) / monthlyRate
            if (annuityFactor > 0.0 && annuityFactor.isFinite()) {
                remainingGoal / annuityFactor
            } else {
                0.0
            }
        }

        val safeMonthly = FinancialMathUtils.sanitizeNonNegative(requiredMonthly, Double.MAX_VALUE)
        val totalMonthlyDeposits = FinancialMathUtils.sanitizeNonNegative(
            value = safeMonthly * totalMonths,
            maxValue = Double.MAX_VALUE
        )
        val totalPersonalPrincipal = FinancialMathUtils.sanitizeNonNegative(
            value = current + totalMonthlyDeposits,
            maxValue = Double.MAX_VALUE
        )
        val totalInterestEarned = FinancialMathUtils.sanitizeNonNegative(
            value = target - totalPersonalPrincipal,
            maxValue = Double.MAX_VALUE
        )
        val (principalRatio, interestRatio) = FinancialMathUtils.computeSplitRatios(
            principal = totalPersonalPrincipal,
            total = target
        )

        return SavingsGoalResult(
            requiredMonthlySavings = safeMonthly,
            projectedFinalBalance = target,
            startingSavings = current,
            totalMonthlyDeposits = totalMonthlyDeposits,
            totalPersonalPrincipal = totalPersonalPrincipal,
            totalInterestEarned = totalInterestEarned,
            principalRatio = principalRatio,
            interestRatio = interestRatio,
            totalMonths = totalMonths,
            status = SavingsGoalStatus.VALID
        )
    }

    private companion object {
        const val MONTHS_PER_YEAR = 12
    }
}
