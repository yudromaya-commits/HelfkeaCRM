package com.helfkea.crm.ui.theme

import androidx.compose.ui.graphics.Color

// Красная тема компании
val PrimaryRed = Color(0xFFD32F2F)    // Основной красный
val PrimaryRedDark = Color(0xFFB71C1C) // Темный красный
val PrimaryRedLight = Color(0xFFF44336) // Светлый красный

val SecondaryRed = Color(0xFFEF5350)   // Вторичный красный
val TertiaryRed = Color(0xFFE57373)    // Третичный красный

// Нейтральные цвета
val White = Color(0xFFFFFFFF)
val Gray50 = Color(0xFFFAFAFA)
val Gray100 = Color(0xFFF5F5F5)
val Gray200 = Color(0xFFEEEEEE)
val Gray300 = Color(0xFFE0E0E0)
val Gray700 = Color(0xFF616161)
val Gray800 = Color(0xFF424242)
val Gray900 = Color(0xFF212121)
val Black = Color(0xFF000000)

// Акцентные цвета
val AccentAmber = Color(0xFFFFC107)    // Акцентный янтарный
val AccentBlue = Color(0xFF2196F3)     // Акцентный синий

// Для Material 3 ColorScheme
val Primary40 = PrimaryRed
val OnPrimary40 = White
val PrimaryContainer40 = PrimaryRedLight
val OnPrimaryContainer40 = PrimaryRedDark

val Secondary40 = Gray100
val OnSecondary40 = Gray900
val SecondaryContainer40 = Gray200
val OnSecondaryContainer40 = Gray800

val Tertiary40 = AccentAmber
val OnTertiary40 = Gray900

val Error40 = Color(0xFFBA1A1A)
val OnError40 = White
val ErrorContainer40 = Color(0xFFFFDAD6)
val OnErrorContainer40 = Color(0xFF410002)

val Background40 = White
val OnBackground40 = Gray900

val Surface40 = White
val OnSurface40 = Gray900
val SurfaceVariant40 = Gray100
val OnSurfaceVariant40 = Gray700

val Outline40 = Gray300
val OutlineVariant40 = Gray200

// Для темной темы (если понадобится)
val Primary80 = PrimaryRedLight
val OnPrimary80 = Black
val PrimaryContainer80 = PrimaryRedDark
val OnPrimaryContainer80 = PrimaryRedLight