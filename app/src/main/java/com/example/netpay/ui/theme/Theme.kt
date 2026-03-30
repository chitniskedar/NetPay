package com.example.netpay.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val NetPayColorScheme = lightColorScheme(
    primary           = NetPayBlue,
    onPrimary         = NetPaySurface,
    primaryContainer  = NetPayBlueSurface,
    onPrimaryContainer = NetPayBlueDark,
    secondary         = NetPayGreen,
    onSecondary       = NetPaySurface,
    secondaryContainer = NetPayGreenLight,
    onSecondaryContainer = NetPayGreen,
    error             = NetPayRed,
    onError           = NetPaySurface,
    errorContainer    = NetPayRedLight,
    background        = NetPayBackground,
    onBackground      = NetPayTextPrimary,
    surface           = NetPaySurface,
    onSurface         = NetPayTextPrimary,
    surfaceVariant    = NetPayBlueSurface,
    onSurfaceVariant  = NetPayTextSecondary,
    outline           = NetPayCardBorder
)

@Composable
fun NetpayTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NetPayColorScheme,
        typography  = Typography,
        content     = content
    )
}