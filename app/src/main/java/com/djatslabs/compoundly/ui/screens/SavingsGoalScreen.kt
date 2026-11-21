package com.djatslabs.compoundly.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.djatslabs.compoundly.R
import com.djatslabs.compoundly.domain.formatter.CurrencyFormatter
import com.djatslabs.compoundly.domain.model.AppCurrency
import com.djatslabs.compoundly.domain.model.InterestRatePeriod
import com.djatslabs.compoundly.domain.model.SavingsGoalStatus
import com.djatslabs.compoundly.domain.model.TimeframeUnit
import com.djatslabs.compoundly.ui.components.CompoundlyNumericOutlinedField
import com.djatslabs.compoundly.ui.components.CompoundlySegmentedSelector
import com.djatslabs.compoundly.ui.components.CurrencyInputField
import com.djatslabs.compoundly.ui.components.HeroResultCard
import com.djatslabs.compoundly.ui.components.RateInputWithPeriodToggle
import com.djatslabs.compoundly.ui.components.StackedProportionBar
import com.djatslabs.compoundly.ui.components.SubtleInfoCaption
import com.djatslabs.compoundly.ui.state.SavingsGoalUiState
import com.djatslabs.compoundly.ui.theme.CompoundlyTheme
import java.util.Locale

@Composable
fun SavingsGoalScreen(
    currency: AppCurrency,
    state: SavingsGoalUiState,
    onTargetAmountChanged: (String) -> Unit,
    onCurrentSavingsChanged: (String) -> Unit,
    onReturnRateChanged: (String) -> Unit,
    onRatePeriodChanged: (InterestRatePeriod) -> Unit,
    onTimeframeChanged: (String) -> Unit,
    onTimeframeUnitChanged: (TimeframeUnit) -> Unit,
    onViewGuideClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val result = state.result
    val badgeText = when (result.status) {
        SavingsGoalStatus.AWAITING_INPUT -> stringResource(R.string.savings_badge_awaiting)
        SavingsGoalStatus.VALID -> stringResource(R.string.savings_badge_months, result.totalMonths)
        SavingsGoalStatus.GOAL_ALREADY_REACHED -> stringResource(R.string.savings_badge_reached)
        SavingsGoalStatus.INVALID_TIMEFRAME -> stringResource(R.string.savings_badge_invalid_time)
    }

    val helperMessage = when (result.status) {
        SavingsGoalStatus.AWAITING_INPUT -> stringResource(R.string.savings_note_awaiting)
        SavingsGoalStatus.GOAL_ALREADY_REACHED -> stringResource(R.string.savings_note_reached)
        SavingsGoalStatus.INVALID_TIMEFRAME -> stringResource(R.string.savings_note_invalid_time)
        SavingsGoalStatus.VALID -> null
    }

    val targetPlaceholder = stringResource(
        if (currency == AppCurrency.IDR) R.string.hint_amount_idr else R.string.hint_amount_usd_eur
    )
    val currentPlaceholder = stringResource(
        if (currency == AppCurrency.IDR) R.string.hint_monthly_idr else R.string.hint_monthly_usd_eur
    )
    val timeframePlaceholder = stringResource(
        if (state.timeframeUnit == TimeframeUnit.MONTHS) {
            R.string.hint_timeframe_months
        } else {
            R.string.hint_timeframe_years
        }
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("savings_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item(key = "savings_hero") {
            HeroResultCard(
                title = stringResource(R.string.savings_hero_title),
                badgeText = badgeText,
                heroAmount = CurrencyFormatter.formatCurrency(result.requiredMonthlySavings, currency),
                heroSuffix = stringResource(R.string.savings_per_month_suffix),
                helperMessage = helperMessage,
                primaryMetricLabel = stringResource(R.string.savings_metric_deposits),
                primaryMetricValue = CurrencyFormatter.formatCurrency(result.totalPersonalPrincipal, currency),
                secondaryMetricLabel = stringResource(R.string.savings_metric_interest),
                secondaryMetricValue = CurrencyFormatter.formatCurrency(result.totalInterestEarned, currency),
                heroValueTestTag = "savings_hero_value"
            )
        }

        item(key = "savings_bar") {
            val depositsLabelFormat = stringResource(R.string.savings_legend_deposits_pct)
            val interestLabelFormat = stringResource(R.string.savings_legend_interest_pct)
            StackedProportionBar(
                principalRatio = result.principalRatio,
                interestRatio = result.interestRatio,
                principalLegendWithPct = { pct ->
                    String.format(Locale.getDefault(), depositsLabelFormat, pct)
                },
                interestLegendWithPct = { pct ->
                    String.format(Locale.getDefault(), interestLabelFormat, pct)
                },
                principalLegendZero = stringResource(R.string.savings_legend_deposits),
                interestLegendZero = stringResource(R.string.savings_legend_interest)
            )
        }

        item(key = "savings_inputs") {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, CompoundlyTheme.financialColors.cardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.savings_section_inputs),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    CurrencyInputField(
                        value = state.targetAmountInput,
                        onValueChange = onTargetAmountChanged,
                        label = stringResource(R.string.savings_input_target),
                        placeholder = targetPlaceholder,
                        currency = currency,
                        imeAction = ImeAction.Next,
                        modifier = Modifier.testTag("input_target")
                    )

                    CurrencyInputField(
                        value = state.currentSavingsInput,
                        onValueChange = onCurrentSavingsChanged,
                        label = stringResource(R.string.savings_input_current),
                        placeholder = currentPlaceholder,
                        currency = currency,
                        imeAction = ImeAction.Next,
                        modifier = Modifier.testTag("input_current")
                    )

                    RateInputWithPeriodToggle(
                        value = state.returnRateInput,
                        ratePeriod = state.ratePeriod,
                        onValueChange = onReturnRateChanged,
                        onRatePeriodChanged = onRatePeriodChanged,
                        label = stringResource(R.string.savings_input_rate),
                        yearlyPlaceholder = "0",
                        monthlyPlaceholder = "0",
                        inputTestTag = "input_savings_rate",
                        yearlyToggleTestTag = "savings_rate_annually",
                        monthlyToggleTestTag = "savings_rate_monthly",
                        imeAction = ImeAction.Next
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        CompoundlyNumericOutlinedField(
                            value = state.timeframeInput,
                            onValueChange = onTimeframeChanged,
                            label = stringResource(R.string.savings_input_timeframe),
                            placeholder = timeframePlaceholder,
                            prefixText = null,
                            suffixText = stringResource(
                                if (state.timeframeUnit == TimeframeUnit.MONTHS) {
                                    R.string.timeframe_months
                                } else {
                                    R.string.timeframe_years
                                }
                            ),
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                            modifier = Modifier.testTag("input_timeframe")
                        )

                        CompoundlySegmentedSelector(
                            options = TimeframeUnit.entries,
                            selectedOption = state.timeframeUnit,
                            onOptionSelected = onTimeframeUnitChanged,
                            labelProvider = { unit ->
                                when (unit) {
                                    TimeframeUnit.MONTHS -> stringResource(R.string.timeframe_months)
                                    TimeframeUnit.YEARS -> stringResource(R.string.timeframe_years)
                                }
                            },
                            testTagProvider = { unit ->
                                if (unit == TimeframeUnit.MONTHS) "unit_months" else "unit_years"
                            }
                        )
                    }
                }
            }
        }

        item(key = "savings_info") {
            SubtleInfoCaption(
                text = stringResource(R.string.savings_info_caption)
            )
        }

        item(key = "savings_guide_link") {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TextButton(
                    onClick = onViewGuideClick,
                    modifier = Modifier.testTag("view_guide_button")
                ) {
                    Text(
                        text = stringResource(R.string.view_quick_guide),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
