package com.djatslabs.compoundly.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.djatslabs.compoundly.R

@Composable
fun PercentageInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Next
) {
    CompoundlyNumericOutlinedField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        placeholder = placeholder,
        prefixText = null,
        suffixText = stringResource(R.string.percent_symbol),
        keyboardType = KeyboardType.Decimal,
        imeAction = imeAction,
        modifier = modifier
    )
}
