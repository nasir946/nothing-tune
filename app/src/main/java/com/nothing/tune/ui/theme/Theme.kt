package com.nothing.tune.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val PureBlack = Color(0xFF000000)
val DarkSurface = Color(0xFF141416)
val DotBorder = Color(0xFF262628)
val TextWhite = Color(0xFFEEEEEE)
val TextMuted = Color(0xFF888888)
val AccentRed = Color(0xFFD71920) // Nothing OS signature red accent
val AccentYellow = Color(0xFFFEE12B)
val NothingCardBg = Color(0xFF141416)
val NothingCardBorder = Color(0xFF262628)

fun getDarkColorScheme(accentColor: Color = Color.White) = darkColorScheme(
    primary = accentColor,
    onPrimary = PureBlack,
    surface = DarkSurface,
    onSurface = TextWhite,
    background = PureBlack,
    onBackground = TextWhite,
    outline = DotBorder
)

fun Modifier.nothingCard(cornerRadius: Int = 18) = this
    .clip(RoundedCornerShape(cornerRadius.dp))
    .background(NothingCardBg)
    .border(1.dp, NothingCardBorder, RoundedCornerShape(cornerRadius.dp))

fun Modifier.nothingPill(selected: Boolean, accentColor: Color = Color.White) = this
    .clip(CircleShape)
    .then(
        if (selected) {
            Modifier.background(Color.White)
        } else {
            Modifier
                .background(Color(0xFF18181A))
                .border(1.dp, Color(0xFF2A2A2E), CircleShape)
        }
    )

fun Modifier.glassCard(cornerRadius: Int = 16) = this.nothingCard(cornerRadius)

@Composable
fun NothingTuneTheme(
    accentColor: Color = Color.White,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = getDarkColorScheme(accentColor),
        content = content
    )
}
