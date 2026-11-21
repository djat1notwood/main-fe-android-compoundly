package com.djatslabs.compoundly.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

val EmeraldPrimary = Color(0xFF059669)
val EmeraldDeep = Color(0xFF064E3B)
val EmeraldDarkContainer = Color(0xFF065F46)
val MintAccent = Color(0xFF34D399)
val MintSoft = Color(0xFFD1FAE5)
val MintSurfaceTint = Color(0xFFECFDF5)

val SurfaceWhite = Color(0xFFFFFFFF)
val BackgroundOffWhite = Color(0xFFF8FAFC)
val SurfaceSubtle = Color(0xFFF1F5F9)

val SlateTextPrimary = Color(0xFF0F172A)
val SlateTextSecondary = Color(0xFF475569)
val SlateTextMuted = Color(0xFF64748B)
val BorderSubtle = Color(0xFFE2E8F0)
val BorderOutline = Color(0xFFCBD5E1)

val ErrorCrimson = Color(0xFFDC2626)
val ErrorContainerLight = Color(0xFFFEE2E2)
val WarningAmberText = Color(0xFFB45309)
val WarningAmberContainer = Color(0xFFFEF3C7)

@Immutable
data class CompoundlyFinancialColors(
    val heroSurface: Color = EmeraldDeep,
    val heroSurfaceAccent: Color = EmeraldDarkContainer,
    val onHeroSurface: Color = SurfaceWhite,
    val onHeroMuted: Color = Color(0xFFCBD5E1),
    val heroHighlight: Color = MintAccent,
    val heroTileSurface: Color = Color(0x1CFFFFFF),
    val principalBar: Color = EmeraldDeep,
    val interestBar: Color = MintAccent,
    val emptyBarTrack: Color = BorderSubtle,
    val cardBorder: Color = BorderSubtle,
    val subtleSurface: Color = SurfaceSubtle,
    val infoBackground: Color = MintSurfaceTint,
    val warningContainer: Color = WarningAmberContainer,
    val onWarningContainer: Color = WarningAmberText
)
