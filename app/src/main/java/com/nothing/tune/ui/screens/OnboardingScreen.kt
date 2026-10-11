package com.nothing.tune.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nothing.tune.ui.components.DotMatrixText
import com.nothing.tune.ui.components.NothingArtwork
import com.nothing.tune.ui.theme.PureBlack

@Composable
fun OnboardingScreen(
    onGetStarted: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentPage by remember { mutableIntStateOf(0) }

    val slides = listOf(
        Triple(
            listOf("Your", "Music", "Your Way"),
            "Minimal. Focused. Beautifully simple.",
            0
        ),
        Triple(
            listOf("Hi-Res", "Direct", "Sound"),
            "Lossless bit-perfect audio for pure fidelity.",
            1
        ),
        Triple(
            listOf("Curated", "By Your", "Ears"),
            "Tracks your most-played and recent listening.",
            2
        )
    )

    val currentSlide = slides[currentPage]

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Main Content Area
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                // Dot Matrix Title lines
                currentSlide.first.forEach { line ->
                    DotMatrixText(
                        text = line,
                        dotSize = 3.2.dp,
                        dotSpacing = 1.6.dp,
                        activeColor = Color.White,
                        showDimGrid = true
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = currentSlide.second,
                    color = Color(0xFFAAAAAA),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 0.5.sp
                )
            }

            // Visual Artwork Card
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color(0xFF262628), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                NothingArtwork(
                    track = null,
                    modifier = Modifier.fillMaxSize(),
                    seed = currentSlide.third,
                    isCircle = true
                )
            }

            // Bottom Actions & Pagination
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 3 Page Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { index ->
                        Box(
                            modifier = Modifier
                                .size(if (index == currentPage) 8.dp else 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index == currentPage) Color.White else Color(0xFF333336)
                                )
                                .clickable { currentPage = index }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // "Get Started" White Pill Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(Color.White)
                        .clickable {
                            if (currentPage < 2) {
                                currentPage++
                            } else {
                                onGetStarted()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (currentPage == 2) "Get Started" else "Next",
                        color = Color.Black,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // "Skip / Sign In" button
                Text(
                    text = "Sign In / Skip",
                    color = Color(0xFF888888),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .clickable { onGetStarted() }
                        .padding(8.dp)
                )
            }
        }
    }
}
