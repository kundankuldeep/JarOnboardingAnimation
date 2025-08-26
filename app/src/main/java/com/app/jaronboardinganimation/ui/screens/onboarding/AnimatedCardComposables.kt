package com.app.jaronboardinganimation.ui.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.app.jaronboardinganimation.data.model.onboarding.EducationCard

/**
 * Animated card that handles all state transitions with smooth animations
 */
@Composable
fun AnimatedEducationCard(
    educationCard: EducationCard,
    cardState: CardState,
    cardIndex: Int,
    totalCards: Int,
    animationPhase: AnimationPhase,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Animation values based on card state - Smooth, non-bouncy animations
    val targetHeight by animateDpAsState(
        targetValue = when (cardState) {
            CardState.EXPANDED -> 448.dp // Figma: 16+340+16+56+16 = 448dp total
            CardState.COLLAPSED -> 68.dp // Figma: collapsed card height
            CardState.PEEKING -> 120.dp
            CardState.HIDDEN -> 0.dp
        },
        animationSpec = tween(
            durationMillis = 400,
            easing = LinearOutSlowInEasing
        ),
        label = "card_height"
    )

    val targetAlpha by animateFloatAsState(
        targetValue = when (cardState) {
            CardState.HIDDEN -> 0f
            else -> 1f
        },
        animationSpec = tween(
            durationMillis = 300,
            easing = LinearOutSlowInEasing
        ),
        label = "card_alpha"
    )

    // Vertical offset for slide animations
    val targetOffsetY by animateDpAsState(
        targetValue = when (cardState) {
            CardState.PEEKING -> 100.dp // Peek from bottom
            CardState.HIDDEN -> 200.dp // Hidden below screen
            else -> 0.dp // Normal position
        },
        animationSpec = tween(
            durationMillis = 350,
            easing = LinearOutSlowInEasing
        ),
        label = "card_offset_y"
    )

    // Scale animation for smooth transitions
    val targetScale by animateFloatAsState(
        targetValue = when (cardState) {
            CardState.HIDDEN -> 0.8f
            CardState.PEEKING -> 0.95f
            else -> 1f
        },
        animationSpec = tween(
            durationMillis = 350,
            easing = LinearOutSlowInEasing
        ),
        label = "card_scale"
    )

    AnimatedVisibility(
        visible = cardState != CardState.HIDDEN,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(
                durationMillis = 500,
                easing = LinearOutSlowInEasing
            )
        ),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(durationMillis = 300)
        )
    ) {
        // Outer container with Figma design structure
        Box(
            modifier = modifier
                .width(328.dp) // Figma: 328px width for all states
                .height(targetHeight)
                .offset(y = targetOffsetY)
                .graphicsLayer(
                    scaleX = targetScale,
                    scaleY = targetScale,
                    alpha = targetAlpha
                )
                .background(
                    // Figma backgrounds for different states
                    color = when (cardState) {
                        CardState.EXPANDED -> Color(0x4D28085C) // rgba(40, 8, 92, 0.3)
                        CardState.COLLAPSED -> Color(0x5228085C) // rgba(40, 8, 92, 0.32) - Figma collapsed
                        else -> Color.Transparent
                    },
                    shape = when (cardState) {
                        CardState.EXPANDED -> RoundedCornerShape(28.dp) // Figma: 28px radius
                        CardState.COLLAPSED -> RoundedCornerShape(28.dp) // Figma: 28px radius for collapsed
                        CardState.PEEKING -> RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp
                        )

                        CardState.HIDDEN -> RoundedCornerShape(16.dp)
                    }
                )
                .border(
                    width = 1.dp,
                    brush = when (cardState) {
                        CardState.EXPANDED -> {
                            // Figma: Linear gradient border for expanded state
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0x33FFFFFF), // rgba(255, 255, 255, 0.2)
                                    Color(0xFFFFFFFF)  // rgba(255, 255, 255, 1.0)
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(0f, Float.POSITIVE_INFINITY)
                            )
                        }

                        CardState.COLLAPSED -> {
                            // Figma: Collapsed border - rgba(255, 255, 255, 0.12)
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0x1FFFFFFF), // rgba(255, 255, 255, 0.12)
                                    Color(0x1FFFFFFF)  // rgba(255, 255, 255, 0.12)
                                )
                            )
                        }

                        else -> educationCard.getStrokeBrush()
                    },
                    shape = when (cardState) {
                        CardState.EXPANDED -> RoundedCornerShape(28.dp)
                        CardState.COLLAPSED -> RoundedCornerShape(28.dp) // Figma: 28px for collapsed
                        CardState.PEEKING -> RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp
                        )

                        CardState.HIDDEN -> RoundedCornerShape(16.dp)
                    }
                )
                .clickable { onClick() }
                .animateContentSize(
                    animationSpec = tween(
                        durationMillis = 400,
                        easing = LinearOutSlowInEasing
                    )
                )
                .padding(
                    when (cardState) {
                        CardState.EXPANDED -> 16.dp // Figma: 16px padding
                        else -> 0.dp
                    }
                )
        ) {
            // Inner content with state-specific styling
            when (cardState) {
                CardState.EXPANDED -> {
                    ExpandedCardContent(educationCard)
                }

                CardState.COLLAPSED -> {
                    // Figma-exact collapsed card structure
                    CollapsedCardContentFigma(educationCard)
                }

                CardState.PEEKING -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = educationCard.getBackgroundBrush(),
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp
                                )
                            )
                    ) {
                        PeekingCardContent(educationCard)
                    }
                }

                CardState.HIDDEN -> {
                    // No content for hidden state
                }
            }
        }
    }
}

@Composable
private fun ExpandedCardContent(educationCard: EducationCard) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp) // Figma: 16px gap
    ) {
        // Inner frame with exact Figma styling
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp) // Figma: 340px height
                .background(
                    color = Color(0xFF7029CC), // Figma: #7029CC solid purple
                    shape = RoundedCornerShape(16.dp) // Figma: 16px inner radius
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Image content - centered in the purple frame
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(educationCard.image)
                    .crossfade(true)
                    .build(),
                contentDescription = educationCard.expandStateText,
                modifier = Modifier
                    .size(200.dp) // Reasonable size for the central illustration
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Fit // Changed to Fit to preserve aspect ratio
            )
        }

        // Text at bottom with exact Figma typography
        Text(
            text = educationCard.expandStateText,
            style = TextStyle(
                fontFamily = FontFamily.Default, // Inter font
                fontWeight = FontWeight.Bold,    // Figma: 700 weight
                fontSize = 20.sp,               // Figma: 20px
                lineHeight = (20 * 1.4).sp,     // Figma: 1.4em line height
                textAlign = TextAlign.Center    // Figma: center alignment
            ),
            color = Color.White, // Figma: #FFFFFF
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CollapsedCardContentFigma(educationCard: EducationCard) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), // Figma: 16px padding
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp) // Figma: 16px gap
    ) {
        // Image frame with exact Figma styling
        Box(
            modifier = Modifier
                .size(width = 31.dp, height = 36.dp) // Figma: 31.34px × 36px
                .background(
                    color = Color(0xFF7029CC), // Figma: #7029CC
                    shape = RoundedCornerShape(16.dp) // Figma: 16px radius
                ),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(educationCard.image)
                    .crossfade(true)
                    .build(),
                contentDescription = educationCard.collapsedStateText,
                modifier = Modifier
                    .size(24.dp) // Reasonable size within the frame
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Fit
            )
        }

        // Text with exact Figma typography
        Text(
            text = educationCard.collapsedStateText,
            style = TextStyle(
                fontFamily = FontFamily.Default, // Inter font
                fontWeight = FontWeight.Bold,    // Figma: 700 weight
                fontSize = 14.sp,               // Figma: 14px
                lineHeight = (14 * 1.4285714285714286).sp, // Figma: specific line height
                textAlign = TextAlign.Center    // Figma: center alignment
            ),
            color = Color.White, // Figma: #FFFFFF
            modifier = Modifier.weight(1f)
        )

        // Down arrow with exact Figma size
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = "Expand",
            tint = Color.White, // Figma: white
            modifier = Modifier.size(24.dp) // Figma: 24×24px
        )
    }
}

@Composable
private fun CollapsedCardContent(educationCard: EducationCard) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Image (32x36dp)
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(educationCard.image)
                .crossfade(true)
                .build(),
            contentDescription = educationCard.collapsedStateText,
            modifier = Modifier
                .size(width = 32.dp, height = 36.dp)
                .clip(RoundedCornerShape(6.dp)),
            contentScale = ContentScale.Crop
        )

        // Text
        Text(
            text = educationCard.collapsedStateText,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        // Down arrow
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = "Expand",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun PeekingCardContent(educationCard: EducationCard) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = educationCard.expandStateText,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2
        )
    }
}
