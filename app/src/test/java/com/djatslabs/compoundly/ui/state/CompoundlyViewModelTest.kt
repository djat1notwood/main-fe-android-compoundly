package com.djatslabs.compoundly.ui.state

import com.djatslabs.compoundly.data.OnboardingPreferences
import com.djatslabs.compoundly.domain.model.AppCurrency
import com.djatslabs.compoundly.domain.model.CompoundingFrequency
import com.djatslabs.compoundly.domain.model.InterestRatePeriod
import com.djatslabs.compoundly.domain.model.SavingsGoalStatus
import com.djatslabs.compoundly.domain.model.TimeframeUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeOnboardingPreferences(
    initialCompleted: Boolean = false
) : OnboardingPreferences {
    var completed: Boolean = initialCompleted

    override fun isOnboardingCompleted(): Boolean = completed

    override fun setOnboardingCompleted(completed: Boolean) {
        this.completed = completed
    }
}

class CompoundlyViewModelTest {

    @Test
    fun initialState_showsOnboardingWhenIncompleteAndStartsWithEmptyInputs() {
        val prefs = FakeOnboardingPreferences(initialCompleted = false)
        val viewModel = CompoundlyViewModel(onboardingPreferences = prefs)

        val state = viewModel.uiState.value
        assertTrue(state.showOnboarding)
        assertEquals(AppCurrency.IDR, state.selectedCurrency)
        assertEquals(CalculatorTab.COMPOUND_INTEREST, state.selectedTab)
        assertEquals("", state.compoundState.initialInvestmentInput)
        assertEquals("", state.compoundState.monthlyContributionInput)
        assertEquals("", state.compoundState.interestRateInput)
        assertEquals(InterestRatePeriod.ANNUALLY, state.compoundState.ratePeriod)
        assertEquals("", state.compoundState.yearsInput)
        assertEquals("", state.savingsState.targetAmountInput)
        assertEquals("", state.savingsState.currentSavingsInput)
        assertEquals("0", state.savingsState.returnRateInput)
        assertEquals(InterestRatePeriod.ANNUALLY, state.savingsState.ratePeriod)
        assertEquals("", state.savingsState.timeframeInput)
    }

    @Test
    fun completeAndReopenOnboarding_updatesStateAndPersists() {
        val prefs = FakeOnboardingPreferences(initialCompleted = false)
        val viewModel = CompoundlyViewModel(onboardingPreferences = prefs)

        viewModel.completeOnboarding()
        assertFalse(viewModel.uiState.value.showOnboarding)
        assertTrue(prefs.completed)

        viewModel.reopenOnboarding()
        assertTrue(viewModel.uiState.value.showOnboarding)
    }

    @Test
    fun compoundInterestUpdatesInRealTimeAndSyncsYearsSlider() {
        val viewModel = CompoundlyViewModel(FakeOnboardingPreferences(true))

        viewModel.onInitialInvestmentChanged("10000000")
        viewModel.onMonthlyContributionChanged("500000")
        viewModel.onCompoundInterestRateChanged("8")
        viewModel.onCompoundYearsSliderChanged(10f)

        val state = viewModel.uiState.value.compoundState
        assertEquals("10", state.yearsInput)
        assertEquals(10f, state.yearsSliderValue, 0.01f)
        assertEquals(10, state.result.yearlyBreakdown.size)
        assertTrue(state.result.finalBalance > state.result.totalPrincipal)

        viewModel.onCompoundYearsChanged("60")
        assertEquals("60", viewModel.uiState.value.compoundState.yearsInput)
        assertEquals(50f, viewModel.uiState.value.compoundState.yearsSliderValue, 0.01f)

        viewModel.onCompoundingFrequencyChanged(CompoundingFrequency.ANNUALLY)
        assertEquals(CompoundingFrequency.ANNUALLY, viewModel.uiState.value.compoundState.frequency)
    }

    @Test
    fun monthlyInterestPeriod_recalculatesInRealTimeOnBothTabs() {
        val viewModel = CompoundlyViewModel(FakeOnboardingPreferences(true))

        viewModel.onInitialInvestmentChanged("10000000")
        viewModel.onCompoundInterestRateChanged("1")
        viewModel.onCompoundYearsChanged("1")
        val yearlyRateBalance = viewModel.uiState.value.compoundState.result.finalBalance

        viewModel.onCompoundRatePeriodChanged(InterestRatePeriod.MONTHLY)
        val monthlyRateBalance = viewModel.uiState.value.compoundState.result.finalBalance
        assertEquals(InterestRatePeriod.MONTHLY, viewModel.uiState.value.compoundState.ratePeriod)
        assertTrue(monthlyRateBalance > yearlyRateBalance)

        viewModel.onTabSelected(CalculatorTab.SAVINGS_GOAL)
        viewModel.onTargetAmountChanged("12000000")
        viewModel.onSavingsReturnRateChanged("1")
        viewModel.onSavingsTimeframeChanged("12")
        viewModel.onTimeframeUnitChanged(TimeframeUnit.MONTHS)
        val annualReturnMonthlySaving = viewModel.uiState.value.savingsState.result.requiredMonthlySavings

        viewModel.onSavingsRatePeriodChanged(InterestRatePeriod.MONTHLY)
        val monthlyReturnMonthlySaving = viewModel.uiState.value.savingsState.result.requiredMonthlySavings
        assertEquals(InterestRatePeriod.MONTHLY, viewModel.uiState.value.savingsState.ratePeriod)
        assertTrue(monthlyReturnMonthlySaving < annualReturnMonthlySaving)
    }

    @Test
    fun currencySwitch_adaptsDecimalsWithoutScalingNumbers() {
        val viewModel = CompoundlyViewModel(FakeOnboardingPreferences(true))
        viewModel.onCurrencySelected(AppCurrency.USD)
        viewModel.onInitialInvestmentChanged("1000.75")
        viewModel.onTargetAmountChanged("5000.50")

        viewModel.onCurrencySelected(AppCurrency.IDR)

        val state = viewModel.uiState.value
        assertEquals(AppCurrency.IDR, state.selectedCurrency)
        assertEquals("1000", state.compoundState.initialInvestmentInput)
        assertEquals("5000", state.savingsState.targetAmountInput)
    }

    @Test
    fun resetActiveTab_clearsOnlyCurrentTab() {
        val viewModel = CompoundlyViewModel(FakeOnboardingPreferences(true))

        // Populate Tab 1
        viewModel.onInitialInvestmentChanged("1000000")
        viewModel.onCompoundRatePeriodChanged(InterestRatePeriod.MONTHLY)
        viewModel.onCompoundYearsChanged("5")

        // Populate Tab 2
        viewModel.onTabSelected(CalculatorTab.SAVINGS_GOAL)
        viewModel.onTargetAmountChanged("12000000")
        viewModel.onSavingsReturnRateChanged("1")
        viewModel.onSavingsRatePeriodChanged(InterestRatePeriod.MONTHLY)
        viewModel.onSavingsTimeframeChanged("12")
        viewModel.onTimeframeUnitChanged(TimeframeUnit.MONTHS)

        assertEquals(SavingsGoalStatus.VALID, viewModel.uiState.value.savingsState.result.status)

        // Reset Tab 2 -> Tab 1 must stay untouched; Tab 2 rate resets to "0" and ANNUALLY
        viewModel.onResetActiveTab()
        assertEquals("", viewModel.uiState.value.savingsState.targetAmountInput)
        assertEquals("0", viewModel.uiState.value.savingsState.returnRateInput)
        assertEquals(InterestRatePeriod.ANNUALLY, viewModel.uiState.value.savingsState.ratePeriod)
        assertEquals("", viewModel.uiState.value.savingsState.timeframeInput)
        assertEquals("1000000", viewModel.uiState.value.compoundState.initialInvestmentInput)
        assertEquals(InterestRatePeriod.MONTHLY, viewModel.uiState.value.compoundState.ratePeriod)
        assertEquals("5", viewModel.uiState.value.compoundState.yearsInput)

        // Reset Tab 1
        viewModel.onTabSelected(CalculatorTab.COMPOUND_INTEREST)
        viewModel.onResetActiveTab()
        assertEquals("", viewModel.uiState.value.compoundState.initialInvestmentInput)
        assertEquals(InterestRatePeriod.ANNUALLY, viewModel.uiState.value.compoundState.ratePeriod)
        assertEquals("", viewModel.uiState.value.compoundState.yearsInput)
        assertEquals(0.0, viewModel.uiState.value.compoundState.result.finalBalance, 1e-9)
    }
}
