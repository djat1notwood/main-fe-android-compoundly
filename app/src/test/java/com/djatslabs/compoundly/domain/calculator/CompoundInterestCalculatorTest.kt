package com.djatslabs.compoundly.domain.calculator

import com.djatslabs.compoundly.domain.model.CompoundInterestInput
import com.djatslabs.compoundly.domain.model.CompoundingFrequency
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.pow

class CompoundInterestCalculatorTest {

    private val calculator = CompoundInterestCalculator()

    @Test
    fun emptyInputs_returnsAllZerosAndEmptyBreakdown() {
        val result = calculator.calculate(CompoundInterestInput())

        assertEquals(0.0, result.finalBalance, 1e-9)
        assertEquals(0.0, result.totalPrincipal, 1e-9)
        assertEquals(0.0, result.totalInterest, 1e-9)
        assertEquals(0f, result.principalRatio, 1e-6f)
        assertEquals(0f, result.interestRatio, 1e-6f)
        assertTrue(result.yearlyBreakdown.isEmpty())
    }

    @Test
    fun zeroYears_returnsInitialInvestmentWithoutInterest() {
        val result = calculator.calculate(
            CompoundInterestInput(
                initialInvestment = 5_000_000.0,
                monthlyContribution = 500_000.0,
                annualRatePercent = 10.0,
                years = 0,
                frequency = CompoundingFrequency.MONTHLY
            )
        )

        assertEquals(5_000_000.0, result.finalBalance, 1e-9)
        assertEquals(5_000_000.0, result.totalPrincipal, 1e-9)
        assertEquals(0.0, result.totalInterest, 1e-9)
        assertEquals(1f, result.principalRatio, 1e-6f)
        assertEquals(0f, result.interestRatio, 1e-6f)
        assertTrue(result.yearlyBreakdown.isEmpty())
    }

    @Test
    fun zeroInterestRate_sumsPrincipalAccurately() {
        val result = calculator.calculate(
            CompoundInterestInput(
                initialInvestment = 1_000.0,
                monthlyContribution = 100.0,
                annualRatePercent = 0.0,
                years = 3,
                frequency = CompoundingFrequency.MONTHLY
            )
        )

        val expectedPrincipal = 1_000.0 + (100.0 * 36)
        assertEquals(expectedPrincipal, result.finalBalance, 1e-6)
        assertEquals(expectedPrincipal, result.totalPrincipal, 1e-6)
        assertEquals(0.0, result.totalInterest, 1e-6)
        assertEquals(3, result.yearlyBreakdown.size)
        assertEquals(2_200.0, result.yearlyBreakdown[0].endingBalance, 1e-6)
        assertEquals(3_400.0, result.yearlyBreakdown[1].endingBalance, 1e-6)
        assertEquals(4_600.0, result.yearlyBreakdown[2].endingBalance, 1e-6)
    }

    @Test
    fun monthlyCompounding_matchesClosedFormFormula() {
        val initial = 10_000.0
        val monthly = 250.0
        val annualRate = 0.08
        val years = 10
        val monthlyRate = annualRate / 12.0
        val months = years * 12

        val expectedFinal = initial * (1.0 + monthlyRate).pow(months) +
            monthly * (((1.0 + monthlyRate).pow(months) - 1.0) / monthlyRate)
        val expectedPrincipal = initial + monthly * months
        val expectedInterest = expectedFinal - expectedPrincipal

        val result = calculator.calculate(
            CompoundInterestInput(
                initialInvestment = initial,
                monthlyContribution = monthly,
                annualRatePercent = 8.0,
                years = years,
                frequency = CompoundingFrequency.MONTHLY
            )
        )

        assertEquals(expectedFinal, result.finalBalance, 1e-4)
        assertEquals(expectedPrincipal, result.totalPrincipal, 1e-4)
        assertEquals(expectedInterest, result.totalInterest, 1e-4)
        assertEquals(1f, result.principalRatio + result.interestRatio, 1e-5f)
        assertEquals(years, result.yearlyBreakdown.size)
        assertEquals(result.finalBalance, result.yearlyBreakdown.last().endingBalance, 1e-4)
    }

    @Test
    fun monthlyCompounding_exceedsAnnualCompoundingAtSameNominalRate() {
        val monthlyResult = calculator.calculate(
            CompoundInterestInput(
                initialInvestment = 10_000.0,
                monthlyContribution = 100.0,
                annualRatePercent = 10.0,
                years = 10,
                frequency = CompoundingFrequency.MONTHLY
            )
        )
        val annualResult = calculator.calculate(
            CompoundInterestInput(
                initialInvestment = 10_000.0,
                monthlyContribution = 100.0,
                annualRatePercent = 10.0,
                years = 10,
                frequency = CompoundingFrequency.ANNUALLY
            )
        )

        assertTrue(monthlyResult.finalBalance > annualResult.finalBalance)
        assertEquals(monthlyResult.totalPrincipal, annualResult.totalPrincipal, 1e-6)
        assertEquals(10, annualResult.yearlyBreakdown.size)
    }

    @Test
    fun extremeAndInvalidInputs_neverProduceNaNOrInfinity() {
        val invalid = calculator.calculate(
            CompoundInterestInput(
                initialInvestment = Double.NaN,
                monthlyContribution = Double.POSITIVE_INFINITY,
                annualRatePercent = -50.0,
                years = -5,
                frequency = CompoundingFrequency.MONTHLY
            )
        )
        assertTrue(invalid.finalBalance.isFinite() && invalid.finalBalance == 0.0)
        assertTrue(invalid.totalPrincipal.isFinite() && invalid.totalPrincipal == 0.0)
        assertTrue(invalid.totalInterest.isFinite() && invalid.totalInterest == 0.0)

        val huge = calculator.calculate(
            CompoundInterestInput(
                initialInvestment = 1e18,
                monthlyContribution = 1e16,
                annualRatePercent = 9999.0,
                years = 500,
                frequency = CompoundingFrequency.MONTHLY
            )
        )
        assertTrue(huge.finalBalance.isFinite())
        assertTrue(huge.totalPrincipal.isFinite())
        assertTrue(huge.totalInterest.isFinite())
        assertEquals(100, huge.yearlyBreakdown.size)
    }
}
