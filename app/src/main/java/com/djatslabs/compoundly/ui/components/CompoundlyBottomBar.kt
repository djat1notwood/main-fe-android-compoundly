package com.djatslabs.compoundly.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.djatslabs.compoundly.R
import com.djatslabs.compoundly.ui.state.CalculatorTab
import com.djatslabs.compoundly.ui.theme.CompoundlyTheme

@Composable
fun CompoundlyBottomBar(
    selectedTab: CalculatorTab,
    onTabSelected: (CalculatorTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        HorizontalDivider(
            thickness = 1.dp,
            color = CompoundlyTheme.financialColors.cardBorder
        )
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            val compoundLabel = stringResource(R.string.tab_compound_interest)
            val savingsLabel = stringResource(R.string.tab_savings_goal)

            val itemColors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )

            NavigationBarItem(
                selected = selectedTab == CalculatorTab.COMPOUND_INTEREST,
                onClick = { onTabSelected(CalculatorTab.COMPOUND_INTEREST) },
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.TrendingUp,
                        contentDescription = compoundLabel
                    )
                },
                label = {
                    Text(
                        text = compoundLabel,
                        fontWeight = if (selectedTab == CalculatorTab.COMPOUND_INTEREST) {
                            FontWeight.SemiBold
                        } else {
                            FontWeight.Medium
                        },
                        maxLines = 1
                    )
                },
                colors = itemColors,
                modifier = Modifier.testTag("nav_compound")
            )

            NavigationBarItem(
                selected = selectedTab == CalculatorTab.SAVINGS_GOAL,
                onClick = { onTabSelected(CalculatorTab.SAVINGS_GOAL) },
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.Savings,
                        contentDescription = savingsLabel
                    )
                },
                label = {
                    Text(
                        text = savingsLabel,
                        fontWeight = if (selectedTab == CalculatorTab.SAVINGS_GOAL) {
                            FontWeight.SemiBold
                        } else {
                            FontWeight.Medium
                        },
                        maxLines = 1
                    )
                },
                colors = itemColors,
                modifier = Modifier.testTag("nav_savings")
            )
        }
    }
}
