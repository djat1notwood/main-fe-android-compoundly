package com.djatslabs.compoundly.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.djatslabs.compoundly.domain.formatter.ThousandSeparatorVisualTransformation
import com.djatslabs.compoundly.domain.model.AppCurrency

@Composable
fun CurrencyInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    currency: AppCurrency,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Next
) {
    val visualTransformation = remember(currency) {
        ThousandSeparatorVisualTransformation(currency)
    }
    val keyboardType = if (currency.maxFractionDigits == 0) {
        KeyboardType.Number
    } else {
        KeyboardType.Decimal
    }

    CompoundlyNumericOutlinedField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        placeholder = placeholder,
        prefixText = currency.symbol,
        suffixText = null,
        keyboardType = keyboardType,
        imeAction = imeAction,
        visualTransformation = visualTransformation,
        modifier = modifier
    )
}

@Composable
internal fun CompoundlyNumericOutlinedField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    prefixText: String?,
    suffixText: String?,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        label = { Text(text = label) },
        placeholder = {
            Text(
                text = placeholder,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
            )
        },
        prefix = prefixText?.let { symbol ->
            {
                Text(
                    text = "$symbol ",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        suffix = suffixText?.let { unit ->
            {
                Text(
                    text = " $unit",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            fontWeight = FontWeight.Medium
        ),
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onDone = { focusManager.clearFocus() }
        ),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.background
        )
    )
}
