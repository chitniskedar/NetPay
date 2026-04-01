package com.example.netpay.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val PremiumDarkColorScheme = darkColorScheme(
    primary           = NeonViolet,
    onPrimary         = SlateBg,
    primaryContainer  = SlateCard,
    onPrimaryContainer = NeonViolet,
    secondary         = NeonMint,
    onSecondary       = SlateBg,
    secondaryContainer = SlateSurface,
    onSecondaryContainer = NeonMint,
    tertiary          = NeonBlue,
    background        = SlateBg,
    onBackground      = TextHigh,
    surface           = SlateSurface,
    onSurface         = TextHigh,
    surfaceVariant    = SlateCard,
    onSurfaceVariant  = TextMed,
    outline           = SlateOutline,
    error             = NeonError,
    onError           = SlateBg
)

private val PremiumLightColorScheme = lightColorScheme(
    primary           = NeonViolet,
    onPrimary         = TextHigh,
    background        = TextHigh,
    surface           = TextHigh
    // Simplified light theme for now, as the primary vibe is Premium Dark.
)

@Composable
fun NetpayTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    // Force Dark Theme for "Premium" experience across the app as requested
    val colorScheme = PremiumDarkColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}