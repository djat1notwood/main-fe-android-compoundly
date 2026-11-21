package com.djatslabs.compoundly.domain.calculator

import com.djatslabs.compoundly.domain.model.CompoundInterestInput
import com.djatslabs.compoundly.domain.model.CompoundingFrequency
import com.djatslabs.compoundly.domain.model.InterestRatePeriod
import com.djatslabs.compoundly.domain.model.SavingsGoalInput
import com.djatslabs.compoundly.domain.model.SavingsGoalStatus
import com.djatslabs.compoundly.domain.model.TimeframeUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SavingsGoalCalculatorTest {

    private val savingsCalculator = SavingsGoalCalculator()
    private val compoundCalculator = CompoundInterestCalculator()

    @Test
    fun zeroTarget_returnsAwaitingInputStatus() {
        val result = savingsCalculator.calculate(SavingsGoalInput())

        assertEquals(SavingsGoalStatus.AWAITING_INPUT, result.status)
        assertEquals(0.0, result.requiredMonthlySavings, 1e-9)
        assertEquals(0f, result.principalRatio, 1e-6f)
        assertEquals(0f, result.interestRatio, 1e-6f)
    }

    @Test
    fun zeroTimeframeWithPositiveTarget_returnsInvalidTimeframe() {
        val result = savingsCalculator.calculate(
            SavingsGoalInput(
                targetAmount = 10_000_000.0,
                currentSavings = 1_000_000.0,
                returnRatePercent = 0.0,
                timeframeValue = 0,
                timeframeUnit = TimeframeUnit.MONTHS
            )
        )

        assertEquals(SavingsGoalStatus.INVALID_TIMEFRAME, result.status)
        assertEquals(0.0, result.requiredMonthlySavings, 1e-9)
    }

    @Test
    fun zeroPercentReturn_dividesRemainingAmountEvenlyAcrossMonths() {
        val result = savingsCalculator.calculate(
            SavingsGoalInput(
                targetAmount = 24_000_000.0,
                currentSavings = 6_000_000.0,
                returnRatePercent = 0.0,
                timeframeValue = 18,
                timeframeUnit = TimeframeUnit.MONTHS
            )
        )

        assertEquals(SavingsGoalStatus.VALID, result.status)
        assertEquals(1_000_000.0, result.requiredMonthlySavings, 1e-6)
        assertEquals(24_000_000.0, result.totalPersonalPrincipal, 1e-6)
        assertEquals(0.0, result.totalInterestEarned, 1e-6)
        assertEquals(1f, result.principalRatio, 1e-6f)
        assertEquals(0f, result.interestRatio, 1e-6f)
    }

    @Test
    fun positiveReturn_crossValidatesAgainstCompoundInterestCalculator() {
        val target = 50_000.0
        val current = 5_000.0
        val rate = 9.0
        val years = 5

        val goalResult = savingsCalculator.calculate(
            SavingsGoalInput(
                targetAmount = target,
                currentSavings = current,
                returnRatePercent = rate,
                timeframeValue = years,
                timeframeUnit = TimeframeUnit.YEARS
            )
        )

        assertEquals(SavingsGoalStatus.VALID, goalResult.status)
        assertEquals(60, goalResult.totalMonths)
        assertTrue(goalResult.requiredMonthlySavings > 0.0)
        assertTrue(goalResult.totalInterestEarned > 0.0)
        assertEquals(target, goalResult.totalPersonalPrincipal + goalResult.totalInterestEarned, 1e-4)

        val simulated = compoundCalculator.calculate(
            CompoundInterestInput(
                initialInvestment = current,
                monthlyContribution = goalResult.requiredMonthlySavings,
                interestRatePercent = rate,
                years = years,
                frequency = CompoundingFrequency.MONTHLY
            )
        )
        assertEquals(target, simulated.finalBalance, 1e-4)
    }

    @Test
    fun monthlyReturnRatePeriod_matchesEquivalentNominalAnnualRate() {
        val monthlyRateGoal = savingsCalculator.calculate(
            SavingsGoalInput(
                targetAmount = 120_000_000.0,
                currentSavings = 15_000_000.0,
                returnRatePercent = 0.5,
                ratePeriod = InterestRatePeriod.MONTHLY,
                timeframeValue = 5,
                timeframeUnit = TimeframeUnit.YEARS
            )
        )
        val annualRateGoal = savingsCalculator.calculate(
            SavingsGoalInput(
                targetAmount = 120_000_000.0,
                currentSavings = 15_000_000.0,
                returnRatePercent = 6.0,
                ratePeriod = InterestRatePeriod.ANNUALLY,
                timeframeValue = 5,
                timeframeUnit = TimeframeUnit.YEARS
            )
        )

        assertEquals(annualRateGoal.requiredMonthlySavings, monthlyRateGoal.requiredMonthlySavings, 1e-6)
        assertEquals(annualRateGoal.totalInterestEarned, monthlyRateGoal.totalInterestEarned, 1e-6)
    }

    @Test
    fun monthsAndYearsEquivalentTimeframes_matchExactly() {
        val monthsResult = savingsCalculator.calculate(
            SavingsGoalInput(
                targetAmount = 12_000.0,
                currentSavings = 2_000.0,
                returnRatePercent = 6.0,
                timeframeValue = 36,
                timeframeUnit = TimeframeUnit.MONTHS
            )
        )
        val yearsResult = savingsCalculator.calculate(
            SavingsGoalInput(
                targetAmount = 12_000.0,
                currentSavings = 2_000.0,
                returnRatePercent = 6.0,
                timeframeValue = 3,
                timeframeUnit = TimeframeUnit.YEARS
            )
        )

        assertEquals(monthsResult.requiredMonthlySavings, yearsResult.requiredMonthlySavings, 1e-9)
        assertEquals(monthsResult.totalInterestEarned, yearsResult.totalInterestEarned, 1e-9)
    }

    @Test
    fun currentSavingsMeetingOrGrowingPastTarget_returnsGoalAlreadyReached() {
        val immediateReached = savingsCalculator.calculate(
            SavingsGoalInput(
                targetAmount = 10_000.0,
                currentSavings = 12_000.0,
                returnRatePercent = 0.0,
                timeframeValue = 12,
                timeframeUnit = TimeframeUnit.MONTHS
            )
        )
        assertEquals(SavingsGoalStatus.GOAL_ALREADY_REACHED, immediateReached.status)
        assertEquals(0.0, immediateReached.requiredMonthlySavings, 1e-9)

        val growthReached = savingsCalculator.calculate(
            SavingsGoalInput(
                targetAmount = 15_000.0,
                currentSavings = 12_000.0,
                returnRatePercent = 12.0,
                timeframeValue = 3,
                timeframeUnit = TimeframeUnit.YEARS
            )
        )
        assertEquals(SavingsGoalStatus.GOAL_ALREADY_REACHED, growthReached.status)
        assertEquals(0.0, growthReached.requiredMonthlySavings, 1e-9)
        assertTrue(growthReached.totalInterestEarned > 0.0)
        assertTrue(growthReached.projectedFinalBalance > 15_000.0)
    }

    @Test
    fun nonFiniteInputs_areGuardedSafely() {
        val result = savingsCalculator.calculate(
            SavingsGoalInput(
                targetAmount = Double.NaN,
                currentSavings = Double.NEGATIVE_INFINITY,
                returnRatePercent = Double.POSITIVE_INFINITY,
                ratePeriod = InterestRatePeriod.MONTHLY,
                timeframeValue = -10,
                timeframeUnit = TimeframeUnit.MONTHS
            )
        )

        assertEquals(SavingsGoalStatus.AWAITING_INPUT, result.status)
        assertTrue(result.requiredMonthlySavings.isFinite() && result.requiredMonthlySavings == 0.0)
    }
}
