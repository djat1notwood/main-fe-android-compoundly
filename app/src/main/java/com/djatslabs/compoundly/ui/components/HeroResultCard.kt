package com.djatslabs.compoundly.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.djatslabs.compoundly.ui.theme.CompoundlyTheme

@Composable
fun HeroResultCard(
    title: String,
    badgeText: String,
    heroAmount: String,
    primaryMetricLabel: String,
    primaryMetricValue: String,
    secondaryMetricLabel: String,
    secondaryMetricValue: String,
    modifier: Modifier = Modifier,
    heroSuffix: String? = null,
    helperMessage: String? = null,
    heroValueTestTag: String = "hero_result_value"
) {
    val financialColors = CompoundlyTheme.financialColors
    val heroFontSize = when {
        heroAmount.length <= 13 -> 32.sp
        heroAmount.length <= 18 -> 26.sp
        else -> 21.sp
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            financialColors.heroSurface,
                            financialColors.heroSurfaceAccent
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge,
                        color = financialColors.onHeroMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = financialColors.heroTileSurface
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = financialColors.heroHighlight,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = heroAmount,
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontSize = heroFontSize,
                            lineHeight = heroFontSize * 1.18f
                        ),
                        fontWeight = FontWeight.Bold,
                        color = financialColors.onHeroSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .testTag(heroValueTestTag)
                    )
                    if (!heroSuffix.isNullOrBlank()) {
                        Text(
                            text = heroSuffix,
                            style = MaterialTheme.typography.titleSmall,
                            color = financialColors.onHeroMuted,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }

                if (!helperMessage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = helperMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = financialColors.onHeroMuted
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HeroMetricTile(
                        label = primaryMetricLabel,
                        value = primaryMetricValue,
                        valueColor = financialColors.onHeroSurface,
                        modifier = Modifier.weight(1f)
                    )
                    HeroMetricTile(
                        label = secondaryMetricLabel,
                        value = secondaryMetricValue,
                        valueColor = financialColors.heroHighlight,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroMetricTile(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    val financialColors = CompoundlyTheme.financialColors
    val tileFontSize = if (value.length > 14) 13.sp else 15.sp

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = financialColors.heroTileSurface
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = financialColors.onHeroMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = tileFontSize),
                fontWeight = FontWeight.Bold,
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
