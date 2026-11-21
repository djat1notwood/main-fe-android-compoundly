package com.djatslabs.compoundly.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

private val CompoundlyLightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = SurfaceWhite,
    primaryContainer = MintSoft,
    onPrimaryContainer = EmeraldDeep,
    secondary = EmeraldDeep,
    onSecondary = SurfaceWhite,
    secondaryContainer = MintSurfaceTint,
    onSecondaryContainer = EmeraldDeep,
    tertiary = MintAccent,
    onTertiary = EmeraldDeep,
    tertiaryContainer = MintSoft,
    onTertiaryContainer = EmeraldDeep,
    background = BackgroundOffWhite,
    onBackground = SlateTextPrimary,
    surface = SurfaceWhite,
    onSurface = SlateTextPrimary,
    surfaceVariant = SurfaceSubtle,
    onSurfaceVariant = SlateTextSecondary,
    surfaceContainerLowest = SurfaceWhite,
    surfaceContainerLow = BackgroundOffWhite,
    surfaceContainer = SurfaceSubtle,
    surfaceContainerHigh = BorderSubtle,
    outline = BorderOutline,
    outlineVariant = BorderSubtle,
    error = ErrorCrimson,
    onError = SurfaceWhite,
    errorContainer = ErrorContainerLight,
    onErrorContainer = ErrorCrimson
)

private val LocalCompoundlyFinancialColors = staticCompositionLocalOf {
    CompoundlyFinancialColors()
}

object CompoundlyTheme {
    val financialColors: CompoundlyFinancialColors
        @Composable
        @ReadOnlyComposable
        get() = LocalCompoundlyFinancialColors.current
}

@Composable
fun CompoundlyTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalCompoundlyFinancialColors provides CompoundlyFinancialColors()
    ) {
        MaterialTheme(
            colorScheme = CompoundlyLightColorScheme,
            typography = Typography,
            content = content
        )
    }
}
