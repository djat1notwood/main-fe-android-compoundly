package com.djatslabs.compoundly.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.djatslabs.compoundly.ui.theme.CompoundlyTheme
import kotlin.math.roundToInt

@Composable
fun StackedProportionBar(
    principalRatio: Float,
    interestRatio: Float,
    principalLegendWithPct: (Int) -> String,
    interestLegendWithPct: (Int) -> String,
    principalLegendZero: String,
    interestLegendZero: String,
    modifier: Modifier = Modifier
) {
    val financialColors = CompoundlyTheme.financialColors
    val hasData = principalRatio + interestRatio > 0.0001f

    val targetPrincipal = if (hasData) principalRatio.coerceIn(0f, 1f) else 0f
    val animatedPrincipal by animateFloatAsState(
        targetValue = targetPrincipal,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "principalBarWeight"
    )
    val animatedInterest = if (hasData) (1f - animatedPrincipal).coerceIn(0f, 1f) else 0f

    val principalPct = (targetPrincipal * 100f).roundToInt().coerceIn(0, 100)
    val interestPct = (100 - principalPct).coerceIn(0, 100)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, financialColors.cardBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(financialColors.emptyBarTrack)
            ) {
                if (hasData) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (animatedPrincipal > 0.005f) {
                            Box(
                                modifier = Modifier
                                    .weight(animatedPrincipal)
                                    .fillMaxHeight()
                                    .background(financialColors.principalBar)
                            )
                        }
                        if (animatedPrincipal > 0.005f && animatedInterest > 0.005f) {
                            Spacer(
                                modifier = Modifier
                                    .width(2.dp)
                                    .fillMaxHeight()
                                    .background(MaterialTheme.colorScheme.surface)
                            )
                        }
                        if (animatedInterest > 0.005f) {
                            Box(
                                modifier = Modifier
                                    .weight(animatedInterest)
                                    .fillMaxHeight()
                                    .background(financialColors.interestBar)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(
                    dotColor = financialColors.principalBar,
                    label = if (hasData) {
                        principalLegendWithPct(principalPct)
                    } else {
                        principalLegendZero
                    }
                )
                LegendItem(
                    dotColor = financialColors.interestBar,
                    label = if (hasData) {
                        interestLegendWithPct(interestPct)
                    } else {
                        interestLegendZero
                    }
                )
            }
        }
    }
}

@Composable
private fun LegendItem(
    dotColor: Color,
    label: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
