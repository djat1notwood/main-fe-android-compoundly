package com.djatslabs.compoundly.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.djatslabs.compoundly.data.OnboardingPreferences
import com.djatslabs.compoundly.domain.calculator.CompoundInterestCalculator
import com.djatslabs.compoundly.domain.calculator.FinancialMathUtils
import com.djatslabs.compoundly.domain.calculator.SavingsGoalCalculator
import com.djatslabs.compoundly.domain.formatter.CurrencyFormatter
import com.djatslabs.compoundly.domain.model.AppCurrency
import com.djatslabs.compoundly.domain.model.CompoundInterestInput
import com.djatslabs.compoundly.domain.model.CompoundInterestResult
import com.djatslabs.compoundly.domain.model.CompoundingFrequency
import com.djatslabs.compoundly.domain.model.InterestRatePeriod
import com.djatslabs.compoundly.domain.model.SavingsGoalInput
import com.djatslabs.compoundly.domain.model.SavingsGoalResult
import com.djatslabs.compoundly.domain.model.TimeframeUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.roundToInt

enum class CalculatorTab {
    COMPOUND_INTEREST,
    SAVINGS_GOAL
}

data class CompoundInterestUiState(
    val initialInvestmentInput: String = "",
    val monthlyContributionInput: String = "",
    val interestRateInput: String = "",
    val ratePeriod: InterestRatePeriod = InterestRatePeriod.ANNUALLY,
    val yearsInput: String = "",
    val yearsSliderValue: Float = 0f,
    val frequency: CompoundingFrequency = CompoundingFrequency.MONTHLY,
    val isBreakdownExpanded: Boolean = false,
    val result: CompoundInterestResult = CompoundInterestResult()
)

data class SavingsGoalUiState(
    val targetAmountInput: String = "",
    val currentSavingsInput: String = "",
    val returnRateInput: String = DEFAULT_SAVINGS_RETURN_RATE,
    val ratePeriod: InterestRatePeriod = InterestRatePeriod.ANNUALLY,
    val timeframeInput: String = "",
    val timeframeUnit: TimeframeUnit = TimeframeUnit.YEARS,
    val result: SavingsGoalResult = SavingsGoalResult()
) {
    companion object {
        const val DEFAULT_SAVINGS_RETURN_RATE = "0"
    }
}

data class CompoundlyUiState(
    val showOnboarding: Boolean = false,
    val selectedCurrency: AppCurrency = AppCurrency.IDR,
    val selectedTab: CalculatorTab = CalculatorTab.COMPOUND_INTEREST,
    val compoundState: CompoundInterestUiState = CompoundInterestUiState(),
    val savingsState: SavingsGoalUiState = SavingsGoalUiState()
)

class CompoundlyViewModel(
    private val onboardingPreferences: OnboardingPreferences,
    private val compoundCalculator: CompoundInterestCalculator = CompoundInterestCalculator(),
    private val savingsCalculator: SavingsGoalCalculator = SavingsGoalCalculator()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CompoundlyUiState(
            showOnboarding = !onboardingPreferences.isOnboardingCompleted()
        )
    )
    val uiState: StateFlow<CompoundlyUiState> = _uiState.asStateFlow()

    fun completeOnboarding() {
        onboardingPreferences.setOnboardingCompleted(true)
        _uiState.update { it.copy(showOnboarding = false) }
    }

    fun reopenOnboarding() {
        _uiState.update { it.copy(showOnboarding = true) }
    }

    fun onTabSelected(tab: CalculatorTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun onCurrencySelected(currency: AppCurrency) {
        _uiState.update { current ->
            if (current.selectedCurrency == currency) return@update current

            val adaptedCompound = current.compoundState.copy(
                initialInvestmentInput = adaptInputForCurrency(
                    current.compoundState.initialInvestmentInput,
                    currency
                ),
                monthlyContributionInput = adaptInputForCurrency(
                    current.compoundState.monthlyContributionInput,
                    currency
                )
            )
            val adaptedSavings = current.savingsState.copy(
                targetAmountInput = adaptInputForCurrency(
                    current.savingsState.targetAmountInput,
                    currency
                ),
                currentSavingsInput = adaptInputForCurrency(
                    current.savingsState.currentSavingsInput,
                    currency
                )
            )

            current.copy(
                selectedCurrency = currency,
                compoundState = recalculateCompound(adaptedCompound),
                savingsState = recalculateSavings(adaptedSavings)
            )
        }
    }

    fun onResetActiveTab() {
        _uiState.update { current ->
            when (current.selectedTab) {
                CalculatorTab.COMPOUND_INTEREST -> current.copy(
                    compoundState = CompoundInterestUiState()
                )

                CalculatorTab.SAVINGS_GOAL -> current.copy(
                    savingsState = SavingsGoalUiState()
                )
            }
        }
    }

    // --- Tab 1: Compound Interest Intents ---

    fun onInitialInvestmentChanged(raw: String) {
        _uiState.update { current ->
            val sanitized = CurrencyFormatter.sanitizeCurrencyInput(raw, current.selectedCurrency)
            val updated = current.compoundState.copy(initialInvestmentInput = sanitized)
            current.copy(compoundState = recalculateCompound(updated))
        }
    }

    fun onMonthlyContributionChanged(raw: String) {
        _uiState.update { current ->
            val sanitized = CurrencyFormatter.sanitizeCurrencyInput(raw, current.selectedCurrency)
            val updated = current.compoundState.copy(monthlyContributionInput = sanitized)
            current.copy(compoundState = recalculateCompound(updated))
        }
    }

    fun onCompoundInterestRateChanged(raw: String) {
        _uiState.update { current ->
            val sanitized = CurrencyFormatter.sanitizeRateInput(raw)
            val updated = current.compoundState.copy(interestRateInput = sanitized)
            current.copy(compoundState = recalculateCompound(updated))
        }
    }

    fun onCompoundRatePeriodChanged(period: InterestRatePeriod) {
        _uiState.update { current ->
            if (current.compoundState.ratePeriod == period) return@update current
            val updated = current.compoundState.copy(ratePeriod = period)
            current.copy(compoundState = recalculateCompound(updated))
        }
    }

    fun onCompoundYearsChanged(raw: String) {
        _uiState.update { current ->
            val sanitized = CurrencyFormatter.sanitizeWholeNumberInput(raw, maxDigits = 3)
            val parsedYears = CurrencyFormatter.parseInt(sanitized)
                .coerceIn(0, FinancialMathUtils.MAX_YEARS)
            val normalizedText = if (sanitized.isEmpty()) "" else parsedYears.toString()
            val updated = current.compoundState.copy(
                yearsInput = normalizedText,
                yearsSliderValue = parsedYears.coerceAtMost(SLIDER_MAX_YEARS).toFloat()
            )
            current.copy(compoundState = recalculateCompound(updated))
        }
    }

    fun onCompoundYearsSliderChanged(value: Float) {
        _uiState.update { current ->
            val snappedYears = value.roundToInt().coerceIn(0, SLIDER_MAX_YEARS)
            val updated = current.compoundState.copy(
                yearsInput = if (snappedYears == 0) "" else snappedYears.toString(),
                yearsSliderValue = snappedYears.toFloat()
            )
            current.copy(compoundState = recalculateCompound(updated))
        }
    }

    fun onCompoundingFrequencyChanged(frequency: CompoundingFrequency) {
        _uiState.update { current ->
            val updated = current.compoundState.copy(frequency = frequency)
            current.copy(compoundState = recalculateCompound(updated))
        }
    }

    fun onToggleBreakdownExpanded() {
        _uiState.update { current ->
            val updated = current.compoundState.copy(
                isBreakdownExpanded = !current.compoundState.isBreakdownExpanded
            )
            current.copy(compoundState = updated)
        }
    }

    // --- Tab 2: Savings Goal Intents ---

    fun onTargetAmountChanged(raw: String) {
        _uiState.update { current ->
            val sanitized = CurrencyFormatter.sanitizeCurrencyInput(raw, current.selectedCurrency)
            val updated = current.savingsState.copy(targetAmountInput = sanitized)
            current.copy(savingsState = recalculateSavings(updated))
        }
    }

    fun onCurrentSavingsChanged(raw: String) {
        _uiState.update { current ->
            val sanitized = CurrencyFormatter.sanitizeCurrencyInput(raw, current.selectedCurrency)
            val updated = current.savingsState.copy(currentSavingsInput = sanitized)
            current.copy(savingsState = recalculateSavings(updated))
        }
    }

    fun onSavingsReturnRateChanged(raw: String) {
        _uiState.update { current ->
            val sanitized = CurrencyFormatter.sanitizeRateInput(raw)
            val updated = current.savingsState.copy(returnRateInput = sanitized)
            current.copy(savingsState = recalculateSavings(updated))
        }
    }

    fun onSavingsRatePeriodChanged(period: InterestRatePeriod) {
        _uiState.update { current ->
            if (current.savingsState.ratePeriod == period) return@update current
            val updated = current.savingsState.copy(ratePeriod = period)
            current.copy(savingsState = recalculateSavings(updated))
        }
    }

    fun onSavingsTimeframeChanged(raw: String) {
        _uiState.update { current ->
            val sanitized = CurrencyFormatter.sanitizeWholeNumberInput(raw, maxDigits = 4)
            val maxAllowed = when (current.savingsState.timeframeUnit) {
                TimeframeUnit.MONTHS -> FinancialMathUtils.MAX_MONTHS
                TimeframeUnit.YEARS -> FinancialMathUtils.MAX_YEARS
            }
            val normalizedText = if (sanitized.isEmpty()) {
                ""
            } else {
                CurrencyFormatter.parseInt(sanitized).coerceIn(0, maxAllowed).toString()
            }
            val updated = current.savingsState.copy(timeframeInput = normalizedText)
            current.copy(savingsState = recalculateSavings(updated))
        }
    }

    fun onTimeframeUnitChanged(unit: TimeframeUnit) {
        _uiState.update { current ->
            if (current.savingsState.timeframeUnit == unit) return@update current
            val currentVal = CurrencyFormatter.parseInt(current.savingsState.timeframeInput)
            val maxForUnit = when (unit) {
                TimeframeUnit.MONTHS -> FinancialMathUtils.MAX_MONTHS
                TimeframeUnit.YEARS -> FinancialMathUtils.MAX_YEARS
            }
            val clampedText = if (current.savingsState.timeframeInput.isEmpty()) {
                ""
            } else {
                currentVal.coerceIn(0, maxForUnit).toString()
            }
            val updated = current.savingsState.copy(
                timeframeInput = clampedText,
                timeframeUnit = unit
            )
            current.copy(savingsState = recalculateSavings(updated))
        }
    }

    private fun recalculateCompound(state: CompoundInterestUiState): CompoundInterestUiState {
        val result = compoundCalculator.calculate(
            CompoundInterestInput(
                initialInvestment = CurrencyFormatter.parseDouble(state.initialInvestmentInput),
                monthlyContribution = CurrencyFormatter.parseDouble(state.monthlyContributionInput),
                interestRatePercent = CurrencyFormatter.parseDouble(state.interestRateInput),
                ratePeriod = state.ratePeriod,
                years = CurrencyFormatter.parseInt(state.yearsInput),
                frequency = state.frequency
            )
        )
        return state.copy(result = result)
    }

    private fun recalculateSavings(state: SavingsGoalUiState): SavingsGoalUiState {
        val result = savingsCalculator.calculate(
            SavingsGoalInput(
                targetAmount = CurrencyFormatter.parseDouble(state.targetAmountInput),
                currentSavings = CurrencyFormatter.parseDouble(state.currentSavingsInput),
                returnRatePercent = CurrencyFormatter.parseDouble(state.returnRateInput),
                ratePeriod = state.ratePeriod,
                timeframeValue = CurrencyFormatter.parseInt(state.timeframeInput),
                timeframeUnit = state.timeframeUnit
            )
        )
        return state.copy(result = result)
    }

    private fun adaptInputForCurrency(raw: String, currency: AppCurrency): String {
        if (raw.isEmpty()) return ""
        val integerTrimmed = if (currency.maxFractionDigits == 0 && raw.contains('.')) {
            raw.substringBefore('.').ifEmpty { "0" }
        } else {
            raw
        }
        return CurrencyFormatter.sanitizeCurrencyInput(integerTrimmed, currency)
    }

    companion object {
        const val SLIDER_MAX_YEARS = 50
    }
}

class CompoundlyViewModelFactory(
    private val onboardingPreferences: OnboardingPreferences
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CompoundlyViewModel::class.java)) {
            return CompoundlyViewModel(onboardingPreferences = onboardingPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
