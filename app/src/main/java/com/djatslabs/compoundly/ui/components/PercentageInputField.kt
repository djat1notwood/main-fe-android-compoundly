package com.djatslabs.compoundly.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.djatslabs.compoundly.R
import com.djatslabs.compoundly.domain.calculator.FinancialMathUtils
import com.djatslabs.compoundly.domain.formatter.CurrencyFormatter
import com.djatslabs.compoundly.domain.model.InterestRatePeriod

@Composable
fun PercentageInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    suffixText: String? = null,
    imeAction: ImeAction = ImeAction.Next
) {
    CompoundlyNumericOutlinedField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        placeholder = placeholder,
        prefixText = null,
        suffixText = suffixText ?: stringResource(R.string.percent_symbol),
        keyboardType = KeyboardType.Decimal,
        imeAction = imeAction,
        modifier = modifier
    )
}

@Composable
fun RateInputWithPeriodToggle(
    value: String,
    ratePeriod: InterestRatePeriod,
    onValueChange: (String) -> Unit,
    onRatePeriodChanged: (InterestRatePeriod) -> Unit,
    label: String,
    yearlyPlaceholder: String,
    monthlyPlaceholder: String,
    inputTestTag: String,
    yearlyToggleTestTag: String,
    monthlyToggleTestTag: String,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Next
) {
    val suffixText = stringResource(
        when (ratePeriod) {
            InterestRatePeriod.ANNUALLY -> R.string.rate_suffix_yearly
            InterestRatePeriod.MONTHLY -> R.string.rate_suffix_monthly
        }
    )
    val placeholder = when (ratePeriod) {
        InterestRatePeriod.ANNUALLY -> yearlyPlaceholder
        InterestRatePeriod.MONTHLY -> monthlyPlaceholder
    }
    val parsedRate = CurrencyFormatter.parseDouble(value)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PercentageInputField(
            value = value,
            onValueChange = onValueChange,
            label = label,
            placeholder = placeholder,
            suffixText = suffixText,
            imeAction = imeAction,
            modifier = Modifier.testTag(inputTestTag)
        )

        CompoundlySegmentedSelector(
            options = InterestRatePeriod.entries,
            selectedOption = ratePeriod,
            onOptionSelected = onRatePeriodChanged,
            labelProvider = { period ->
                when (period) {
                    InterestRatePeriod.ANNUALLY -> stringResource(R.string.rate_period_yearly)
                    InterestRatePeriod.MONTHLY -> stringResource(R.string.rate_period_monthly)
                }
            },
            testTagProvider = { period ->
                when (period) {
                    InterestRatePeriod.ANNUALLY -> yearlyToggleTestTag
                    InterestRatePeriod.MONTHLY -> monthlyToggleTestTag
                }
            }
        )

        if (ratePeriod == InterestRatePeriod.MONTHLY && parsedRate > 0.0) {
            val equivalentAnnualPercent = FinancialMathUtils.resolveRates(
                ratePercent = parsedRate,
                period = InterestRatePeriod.MONTHLY
            ).annualRate * 100.0
            Text(
                text = stringResource(
                    R.string.rate_equivalent_yearly,
                    CurrencyFormatter.formatPercent(equivalentAnnualPercent)
                ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
fun <T> CompoundlySegmentedSelector(
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    labelProvider: @Composable (T) -> String,
    testTagProvider: (T) -> String,
    modifier: Modifier = Modifier
) {
    SingleChoiceSegmentedButtonRow(
        modifier = modifier.fillMaxWidth()
    ) {
        options.forEachIndexed { index, option ->
            val selected = option == selectedOption
            SegmentedButton(
                selected = selected,
                onClick = { onOptionSelected(option) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = options.size
                ),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    inactiveContainerColor = MaterialTheme.colorScheme.surface,
                    inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag(testTagProvider(option))
            ) {
                Text(
                    text = labelProvider(option),
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    maxLines = 1
                )
            }
        }
    }
}
