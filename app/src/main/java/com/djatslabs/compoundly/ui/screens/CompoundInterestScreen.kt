package com.djatslabs.compoundly.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.djatslabs.compoundly.R
import com.djatslabs.compoundly.domain.formatter.CurrencyFormatter
import com.djatslabs.compoundly.domain.model.AppCurrency
import com.djatslabs.compoundly.domain.model.CompoundingFrequency
import com.djatslabs.compoundly.domain.model.InterestRatePeriod
import com.djatslabs.compoundly.domain.model.YearlyBreakdownItem
import com.djatslabs.compoundly.ui.components.CompoundlyNumericOutlinedField
import com.djatslabs.compoundly.ui.components.CompoundlySegmentedSelector
import com.djatslabs.compoundly.ui.components.CurrencyInputField
import com.djatslabs.compoundly.ui.components.HeroResultCard
import com.djatslabs.compoundly.ui.components.RateInputWithPeriodToggle
import com.djatslabs.compoundly.ui.components.StackedProportionBar
import com.djatslabs.compoundly.ui.components.SubtleInfoCaption
import com.djatslabs.compoundly.ui.state.CompoundInterestUiState
import com.djatslabs.compoundly.ui.state.CompoundlyViewModel
import com.djatslabs.compoundly.ui.theme.CompoundlyTheme
import java.util.Locale

@Composable
fun CompoundInterestScreen(
    currency: AppCurrency,
    state: CompoundInterestUiState,
    onInitialInvestmentChanged: (String) -> Unit,
    onMonthlyContributionChanged: (String) -> Unit,
    onInterestRateChanged: (String) -> Unit,
    onRatePeriodChanged: (InterestRatePeriod) -> Unit,
    onYearsChanged: (String) -> Unit,
    onYearsSliderChanged: (Float) -> Unit,
    onFrequencyChanged: (CompoundingFrequency) -> Unit,
    onToggleBreakdownExpanded: () -> Unit,
    modifier: Modifier = Modifier
) {
    val result = state.result
    val breakdown = result.yearlyBreakdown
    val yearsCount = CurrencyFormatter.parseInt(state.yearsInput)
    val badgeText = if (yearsCount > 0 && breakdown.isNotEmpty()) {
        stringResource(R.string.compound_hero_badge_years, yearsCount)
    } else {
        stringResource(R.string.compound_hero_badge_empty)
    }

    val amountPlaceholder = stringResource(
        if (currency == AppCurrency.IDR) R.string.hint_amount_idr else R.string.hint_amount_usd_eur
    )
    val monthlyPlaceholder = stringResource(
        if (currency == AppCurrency.IDR) R.string.hint_monthly_idr else R.string.hint_monthly_usd_eur
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("compound_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item(key = "compound_hero") {
            HeroResultCard(
                title = stringResource(R.string.compound_hero_title),
                badgeText = badgeText,
                heroAmount = CurrencyFormatter.formatCurrency(result.finalBalance, currency),
                primaryMetricLabel = stringResource(R.string.compound_metric_principal),
                primaryMetricValue = CurrencyFormatter.formatCurrency(result.totalPrincipal, currency),
                secondaryMetricLabel = stringResource(R.string.compound_metric_interest),
                secondaryMetricValue = CurrencyFormatter.formatCurrency(result.totalInterest, currency),
                heroValueTestTag = "compound_hero_value"
            )
        }

        item(key = "compound_bar") {
            val principalLabelFormat = stringResource(R.string.compound_legend_principal_pct)
            val interestLabelFormat = stringResource(R.string.compound_legend_interest_pct)
            StackedProportionBar(
                principalRatio = result.principalRatio,
                interestRatio = result.interestRatio,
                principalLegendWithPct = { pct ->
                    String.format(Locale.getDefault(), principalLabelFormat, pct)
                },
                interestLegendWithPct = { pct ->
                    String.format(Locale.getDefault(), interestLabelFormat, pct)
                },
                principalLegendZero = stringResource(R.string.compound_legend_principal),
                interestLegendZero = stringResource(R.string.compound_legend_interest)
            )
        }

        item(key = "compound_inputs") {
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
                        text = stringResource(R.string.compound_section_inputs),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    CurrencyInputField(
                        value = state.initialInvestmentInput,
                        onValueChange = onInitialInvestmentChanged,
                        label = stringResource(R.string.compound_input_initial),
                        placeholder = amountPlaceholder,
                        currency = currency,
                        imeAction = ImeAction.Next,
                        modifier = Modifier.testTag("input_initial")
                    )

                    CurrencyInputField(
                        value = state.monthlyContributionInput,
                        onValueChange = onMonthlyContributionChanged,
                        label = stringResource(R.string.compound_input_monthly),
                        placeholder = monthlyPlaceholder,
                        currency = currency,
                        imeAction = ImeAction.Next,
                        modifier = Modifier.testTag("input_monthly")
                    )

                    RateInputWithPeriodToggle(
                        value = state.interestRateInput,
                        ratePeriod = state.ratePeriod,
                        onValueChange = onInterestRateChanged,
                        onRatePeriodChanged = onRatePeriodChanged,
                        label = stringResource(R.string.compound_input_rate),
                        yearlyPlaceholder = stringResource(R.string.hint_rate),
                        monthlyPlaceholder = stringResource(R.string.hint_rate_monthly),
                        inputTestTag = "input_rate",
                        yearlyToggleTestTag = "compound_rate_annually",
                        monthlyToggleTestTag = "compound_rate_monthly",
                        imeAction = ImeAction.Next
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        CompoundlyNumericOutlinedField(
                            value = state.yearsInput,
                            onValueChange = onYearsChanged,
                            label = stringResource(R.string.compound_input_years),
                            placeholder = stringResource(R.string.hint_years),
                            prefixText = null,
                            suffixText = null,
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                            modifier = Modifier.testTag("input_years")
                        )

                        Slider(
                            value = state.yearsSliderValue,
                            onValueChange = onYearsSliderChanged,
                            valueRange = 0f..CompoundlyViewModel.SLIDER_MAX_YEARS.toFloat(),
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = CompoundlyTheme.financialColors.emptyBarTrack
                            ),
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .testTag("slider_years")
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.compound_slider_min),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (yearsCount > 0) {
                                    stringResource(R.string.compound_slider_years_value, yearsCount)
                                } else {
                                    ""
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = stringResource(R.string.compound_slider_max),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.compound_input_frequency),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        CompoundlySegmentedSelector(
                            options = CompoundingFrequency.entries,
                            selectedOption = state.frequency,
                            onOptionSelected = onFrequencyChanged,
                            labelProvider = { frequency ->
                                when (frequency) {
                                    CompoundingFrequency.MONTHLY -> stringResource(R.string.frequency_monthly)
                                    CompoundingFrequency.ANNUALLY -> stringResource(R.string.frequency_annually)
                                }
                            },
                            testTagProvider = { frequency ->
                                if (frequency == CompoundingFrequency.MONTHLY) {
                                    "freq_monthly"
                                } else {
                                    "freq_annually"
                                }
                            }
                        )
                    }
                }
            }
        }

        item(key = "compound_info") {
            SubtleInfoCaption(
                text = stringResource(R.string.compound_info_caption)
            )
        }

        item(key = "compound_breakdown_header") {
            BreakdownHeaderCard(
                yearCount = breakdown.size,
                isExpanded = state.isBreakdownExpanded,
                onToggle = onToggleBreakdownExpanded
            )
        }

        if (state.isBreakdownExpanded) {
            if (breakdown.isEmpty()) {
                item(key = "compound_breakdown_empty") {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, CompoundlyTheme.financialColors.cardBorder)
                    ) {
                        Text(
                            text = stringResource(R.string.compound_breakdown_empty),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            } else {
                items(
                    items = breakdown,
                    key = { item -> item.year }
                ) { item ->
                    YearlyBreakdownRowCard(
                        item = item,
                        currency = currency
                    )
                }
            }
        }

        item(key = "compound_bottom_spacer") {
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@Composable
private fun BreakdownHeaderCard(
    yearCount: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "breakdownChevron"
    )
    val contentDesc = stringResource(
        if (isExpanded) R.string.cd_collapse_breakdown else R.string.cd_expand_breakdown
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("breakdown_toggle")
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, CompoundlyTheme.financialColors.cardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.compound_breakdown_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (yearCount > 0) {
                    Text(
                        text = stringResource(R.string.compound_breakdown_subtitle, yearCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Icon(
                imageVector = Icons.Rounded.ExpandMore,
                contentDescription = contentDesc,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.rotate(chevronRotation)
            )
        }
    }
}

@Composable
private fun YearlyBreakdownRowCard(
    item: YearlyBreakdownItem,
    currency: AppCurrency
) {
    val principalFormatted = CurrencyFormatter.formatCurrency(item.cumulativePrincipal, currency)
    val interestFormatted = CurrencyFormatter.formatCurrency(item.cumulativeInterest, currency)
    val endingFormatted = CurrencyFormatter.formatCurrency(item.endingBalance, currency)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("breakdown_row_${item.year}"),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, CompoundlyTheme.financialColors.cardBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = stringResource(R.string.compound_breakdown_year_label, item.year),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Text(
                    text = endingFormatted,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.compound_breakdown_principal_short, principalFormatted),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.compound_breakdown_interest_short, interestFormatted),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
