package com.djatslabs.compoundly.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.djatslabs.compoundly.R
import com.djatslabs.compoundly.domain.model.AppCurrency
import com.djatslabs.compoundly.ui.theme.CompoundlyTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompoundlyTopBar(
    selectedCurrency: AppCurrency,
    onCurrencySelected: (AppCurrency) -> Unit,
    onResetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            ),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            actions = {
                CurrencyTogglePill(
                    selectedCurrency = selectedCurrency,
                    onCurrencySelected = onCurrencySelected
                )
                IconButton(
                    onClick = onResetClick,
                    modifier = Modifier.testTag("reset_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.RestartAlt,
                        contentDescription = stringResource(R.string.cd_reset_tab),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )
        HorizontalDivider(
            thickness = 1.dp,
            color = CompoundlyTheme.financialColors.cardBorder
        )
    }
}

@Composable
private fun CurrencyTogglePill(
    selectedCurrency: AppCurrency,
    onCurrencySelected: (AppCurrency) -> Unit
) {
    val selectorDesc = stringResource(R.string.cd_currency_selector)
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = CompoundlyTheme.financialColors.subtleSurface,
        modifier = Modifier
            .padding(end = 2.dp)
            .semantics { contentDescription = selectorDesc }
    ) {
        Row(
            modifier = Modifier.padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppCurrency.entries.forEach { currency ->
                val isSelected = currency == selectedCurrency
                val containerColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        Color.Transparent
                    },
                    label = "currencyPillColor"
                )
                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    label = "currencyTextColor"
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(32.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(containerColor)
                        .clickable(role = Role.RadioButton) {
                            onCurrencySelected(currency)
                        }
                        .semantics { selected = isSelected }
                        .testTag("currency_${currency.code}")
                        .padding(horizontal = 10.dp)
                ) {
                    Text(
                        text = currency.shortLabel,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = contentColor,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
