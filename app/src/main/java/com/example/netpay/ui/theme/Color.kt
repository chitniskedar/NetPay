package com.example.netpay.ui.theme

import androidx.compose.ui.graphics.Color

// FINAL PREMIUM DESIGN PALETTE (Based on Reference)
val DeepBlack     = Color(0xFF0E0E0E) // Pure Deep Background
val SoftBlack     = Color(0xFF1C1C1E) // Surface/Card Background
val DarkGray      = Color(0xFF2C2C2E) // Secondary Surface
val BorderGray    = Color(0xFF38383A) // Subtle Dividers

val MintGreen     = Color(0xFF58D68D) // Accent - Positive / Primary
val SalmonRed     = Color(0xFFE74C3C) // Accent - Negative / Error
val PureWhite     = Color(0xFFFDFEFE) // High Contrast Text
val MutedWhite    = Color(0xFF8E8E93) // Low Contrast Text

// Semantic Mapping
val SlateBg      = DeepBlack
val SlateSurface = SoftBlack
val SlateCard    = DarkGray
val SlateOutline = BorderGray

val OrangeAccent = MintGreen // Swapping "Orange" from previous request to "Mint" as per latest UI ref
val NetSuccess   = MintGreen
val NetError     = SalmonRed
val NetWarning   = Color(0xFFFFCC00) // Standard Apple-style Yellow

val TextHigh     = PureWhite
val TextMed      = MutedWhite
val TextLow      = Color(0xFF48484A) // Darkest Gray Text

// Legacy compat
val NeonViolet = MintGreen
val NeonMint = MintGreen
val NeonBlue = MintGreen
val NeonError = SalmonRed
val NeonSuccess = MintGreen
val NeonWarning = NetWarning