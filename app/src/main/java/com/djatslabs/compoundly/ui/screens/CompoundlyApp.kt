package com.djatslabs.compoundly.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.djatslabs.compoundly.ui.components.CompoundlyBottomBar
import com.djatslabs.compoundly.ui.components.CompoundlyTopBar
import com.djatslabs.compoundly.ui.state.CalculatorTab
import com.djatslabs.compoundly.ui.state.CompoundlyUiState
import com.djatslabs.compoundly.ui.state.CompoundlyViewModel

@Composable
fun CompoundlyApp(
    viewModel: CompoundlyViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CompoundlyAppContent(
        uiState = uiState,
        onCompleteOnboarding = viewModel::completeOnboarding,
        onReopenOnboarding = viewModel::reopenOnboarding,
        onTabSelected = viewModel::onTabSelected,
        onCurrencySelected = viewModel::onCurrencySelected,
        onResetActiveTab = viewModel::onResetActiveTab,
        onInitialInvestmentChanged = viewModel::onInitialInvestmentChanged,
        onMonthlyContributionChanged = viewModel::onMonthlyContributionChanged,
        onCompoundAnnualRateChanged = viewModel::onCompoundAnnualRateChanged,
        onCompoundYearsChanged = viewModel::onCompoundYearsChanged,
        onCompoundYearsSliderChanged = viewModel::onCompoundYearsSliderChanged,
        onCompoundingFrequencyChanged = viewModel::onCompoundingFrequencyChanged,
        onToggleBreakdownExpanded = viewModel::onToggleBreakdownExpanded,
        onTargetAmountChanged = viewModel::onTargetAmountChanged,
        onCurrentSavingsChanged = viewModel::onCurrentSavingsChanged,
        onSavingsAnnualReturnChanged = viewModel::onSavingsAnnualReturnChanged,
        onSavingsTimeframeChanged = viewModel::onSavingsTimeframeChanged,
        onTimeframeUnitChanged = viewModel::onTimeframeUnitChanged,
        modifier = modifier
    )
}

@Composable
fun CompoundlyAppContent(
    uiState: CompoundlyUiState,
    onCompleteOnboarding: () -> Unit,
    onReopenOnboarding: () -> Unit,
    onTabSelected: (CalculatorTab) -> Unit,
    onCurrencySelected: (com.djatslabs.compoundly.domain.model.AppCurrency) -> Unit,
    onResetActiveTab: () -> Unit,
    onInitialInvestmentChanged: (String) -> Unit,
    onMonthlyContributionChanged: (String) -> Unit,
    onCompoundAnnualRateChanged: (String) -> Unit,
    onCompoundYearsChanged: (String) -> Unit,
    onCompoundYearsSliderChanged: (Float) -> Unit,
    onCompoundingFrequencyChanged: (com.djatslabs.compoundly.domain.model.CompoundingFrequency) -> Unit,
    onToggleBreakdownExpanded: () -> Unit,
    onTargetAmountChanged: (String) -> Unit,
    onCurrentSavingsChanged: (String) -> Unit,
    onSavingsAnnualReturnChanged: (String) -> Unit,
    onSavingsTimeframeChanged: (String) -> Unit,
    onTimeframeUnitChanged: (com.djatslabs.compoundly.domain.model.TimeframeUnit) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = uiState.showOnboarding,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "onboardingOrCalculator",
        modifier = modifier.fillMaxSize()
    ) { showOnboarding ->
        if (showOnboarding) {
            OnboardingScreen(onFinishOnboarding = onCompleteOnboarding)
        } else {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                topBar = {
                    CompoundlyTopBar(
                        selectedCurrency = uiState.selectedCurrency,
                        onCurrencySelected = onCurrencySelected,
                        onResetClick = onResetActiveTab
                    )
                },
                bottomBar = {
                    CompoundlyBottomBar(
                        selectedTab = uiState.selectedTab,
                        onTabSelected = onTabSelected
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .consumeWindowInsets(innerPadding)
                        .imePadding()
                ) {
                    AnimatedContent(
                        targetState = uiState.selectedTab,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "activeCalculatorTab"
                    ) { activeTab ->
                        when (activeTab) {
                            CalculatorTab.COMPOUND_INTEREST -> {
                                CompoundInterestScreen(
                                    currency = uiState.selectedCurrency,
                                    state = uiState.compoundState,
                                    onInitialInvestmentChanged = onInitialInvestmentChanged,
                                    onMonthlyContributionChanged = onMonthlyContributionChanged,
                                    onAnnualRateChanged = onCompoundAnnualRateChanged,
                                    onYearsChanged = onCompoundYearsChanged,
                                    onYearsSliderChanged = onCompoundYearsSliderChanged,
                                    onFrequencyChanged = onCompoundingFrequencyChanged,
                                    onToggleBreakdownExpanded = onToggleBreakdownExpanded
                                )
                            }

                            CalculatorTab.SAVINGS_GOAL -> {
                                SavingsGoalScreen(
                                    currency = uiState.selectedCurrency,
                                    state = uiState.savingsState,
                                    onTargetAmountChanged = onTargetAmountChanged,
                                    onCurrentSavingsChanged = onCurrentSavingsChanged,
                                    onAnnualReturnChanged = onSavingsAnnualReturnChanged,
                                    onTimeframeChanged = onSavingsTimeframeChanged,
                                    onTimeframeUnitChanged = onTimeframeUnitChanged,
                                    onViewGuideClick = onReopenOnboarding
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
