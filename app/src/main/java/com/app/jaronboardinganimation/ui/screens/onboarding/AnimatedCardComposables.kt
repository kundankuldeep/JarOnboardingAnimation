package com.app.jaronboardinganimation.ui.screens.onboarding

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.CachePolicy
import com.app.jaronboardinganimation.R
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
    // Height animation with smooth easing to prevent jerky behavior
    val targetHeight by animateDpAsState(
        targetValue = when (cardState) {
            CardState.EXPANDED -> 448.dp // Figma: 16+340+16+56+16 = 448dp total
            CardState.COLLAPSED -> 68.dp // Figma: collapsed card height
            CardState.HIDDEN -> 448.dp // Same as expanded - card is fully formed when sliding
            CardState.PEEKING -> 448.dp // Full height - positioning controls visibility at 40%
            else -> 448.dp
        },
        animationSpec = when {
            cardState == CardState.HIDDEN -> tween(durationMillis = 0) // No animation for hidden
            else -> {
                // Consistent smooth animation for all state transitions
                tween(
                    durationMillis = 800, // Longer duration for smoother transition
                    easing = LinearOutSlowInEasing
                )
            }
        },
        label = "card_height"
    )

    // Keep cards fully visible (100% alpha) during slide - no fade animation
    val targetAlpha = 1f // Always fully visible for clean slide-in effect

    // Simple positioning - no complex logic
    
    // Vertical offset for slide animations - simple logic
    val targetOffsetY by animateDpAsState(
        targetValue = when (cardState) {
            CardState.HIDDEN -> 500.dp // Well below screen bounds
            CardState.EXPANDED -> 0.dp // Center screen
            CardState.COLLAPSED -> 0.dp // Will be positioned by you later  
            CardState.PEEKING -> 270.dp // Position so 40% of full card is visible from bottom (60% of 448dp = 268dp)
            else -> 0.dp // Default position
        },
        animationSpec = when (cardState) {
            CardState.HIDDEN -> tween(durationMillis = 0) // Instant for hidden
            else -> {
                // Synchronized timing with height animation for smooth transitions
                tween(
                    durationMillis = 800, // Match height animation duration
                    easing = LinearOutSlowInEasing
                )
            }
        },
        label = "card_offset_y"
    )
    
    // No horizontal offset needed for simplified animation

    // Keep cards at 100% scale to avoid popping effect - pure slide animation
    val targetScale by animateFloatAsState(
        targetValue = 1f, // Always 100% scale for smooth slide-in without popping
        animationSpec = tween(
            durationMillis = 800, // Match other animation timings
            easing = LinearOutSlowInEasing
        ),
        label = "card_scale"
    )

    // Remove AnimatedVisibility to avoid additional fade/slide effects - use simple visibility
    if (cardState != CardState.HIDDEN) {
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
                        CardState.EXPANDED, CardState.PEEKING -> Color(0x4D28085C) // rgba(40, 8, 92, 0.3) - same for both
                        CardState.COLLAPSED -> Color(0x5228085C) // rgba(40, 8, 92, 0.32) - Figma collapsed
                        else -> Color.Transparent
                    },
                    shape = when (cardState) {
                        CardState.EXPANDED, CardState.PEEKING -> RoundedCornerShape(28.dp) // Figma: 28px radius - same for both
                        CardState.COLLAPSED -> RoundedCornerShape(28.dp) // Figma: 28px radius for collapsed
                        else -> RoundedCornerShape(16.dp)
                    }
                )
                .border(
                    width = 1.dp,
                    brush = when (cardState) {
                        CardState.EXPANDED, CardState.PEEKING -> {
                            // Figma: Linear gradient border for expanded state - same for both
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
                        CardState.EXPANDED, CardState.PEEKING -> RoundedCornerShape(28.dp) // Same shape for both
                        CardState.COLLAPSED -> RoundedCornerShape(28.dp) // Figma: 28px for collapsed
                        else -> RoundedCornerShape(16.dp)
                    }
                )
                .clickable { onClick() }
                .padding(
                    when (cardState) {
                        CardState.EXPANDED, CardState.PEEKING -> 16.dp // Figma: 16px padding - same for both
                        else -> 0.dp
                    }
                )
        ) {
            // Shared element transition content - single composable with animated elements
            SharedElementCardContent(
                educationCard = educationCard,
                cardState = cardState
            )
        }
    }
} // Close the if statement

@Composable
private fun SharedElementCardContent(
    educationCard: EducationCard,
    cardState: CardState
) {
    // Animated image size for shared element transition
    val imageSize by animateDpAsState(
        targetValue = when (cardState) {
            CardState.EXPANDED -> 200.dp // Large image in expanded state
            CardState.COLLAPSED -> 24.dp // Small image in collapsed state
            CardState.PEEKING -> 200.dp // Full size image - same as expanded (fully loaded card)
            else -> 200.dp
        },
        animationSpec = tween(
            durationMillis = 800,
            easing = LinearOutSlowInEasing
        ),
        label = "image_size"
    )
    
    // Animated background frame size
    val backgroundFrameHeight by animateDpAsState(
        targetValue = when (cardState) {
            CardState.EXPANDED -> 340.dp // Large frame height
            CardState.COLLAPSED -> 36.dp // Small frame height
            CardState.PEEKING -> 340.dp // Full frame height - same as expanded (fully loaded card)
            else -> 340.dp
        },
        animationSpec = tween(
            durationMillis = 800,
            easing = LinearOutSlowInEasing
        ),
        label = "background_frame_height"
    )
    
    val backgroundFrameWidth by animateDpAsState(
        targetValue = when (cardState) {
            CardState.EXPANDED -> 296.dp // fillMaxWidth - 32.dp padding
            CardState.COLLAPSED -> 31.dp // Small frame width
            CardState.PEEKING -> 296.dp // Full frame width - same as expanded (fully loaded card)
            else -> 296.dp
        },
        animationSpec = tween(
            durationMillis = 800,
            easing = LinearOutSlowInEasing
        ),
        label = "background_frame_width"
    )
    
    // Animated text size
    val textSize by animateFloatAsState(
        targetValue = when (cardState) {
            CardState.EXPANDED -> 20f // Large text
            CardState.COLLAPSED -> 14f // Small text
            CardState.PEEKING -> 20f // Full size text - same as expanded (fully loaded card)
            else -> 20f
        },
        animationSpec = tween(
            durationMillis = 800,
            easing = LinearOutSlowInEasing
        ),
        label = "text_size"
    )
    
    // Crossfade animation for text transition
    val expandedTextAlpha by animateFloatAsState(
        targetValue = when (cardState) {
            CardState.EXPANDED -> 1f
            CardState.COLLAPSED -> 0f
            CardState.PEEKING -> 1f // Show expanded text in peeking state
            else -> 1f
        },
        animationSpec = tween(
            durationMillis = 400,
            easing = LinearOutSlowInEasing
        ),
        label = "expanded_text_alpha"
    )
    
    val collapsedTextAlpha by animateFloatAsState(
        targetValue = when (cardState) {
            CardState.COLLAPSED -> 1f
            CardState.PEEKING -> 0f // Hide collapsed text in peeking state
            else -> 0f
        },
        animationSpec = tween(
            durationMillis = 400,
            easing = LinearOutSlowInEasing
        ),
        label = "collapsed_text_alpha"
    )
    
    when (cardState) {
        CardState.EXPANDED, CardState.PEEKING -> {
            // Expanded layout with animated elements
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Animated background frame
                Box(
                    modifier = Modifier
                        .width(backgroundFrameWidth)
                        .height(backgroundFrameHeight)
                        .background(
                            color = Color(0xFF7029CC),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Shared animated image with optimized loading
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(educationCard.image)
                            .crossfade(true)
                            .placeholder(R.drawable.ic_image_placeholder)
                            .error(R.drawable.ic_image_error)
                            .fallback(R.drawable.ic_image_placeholder)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .allowHardware(true)
                            .build(),
                        contentDescription = educationCard.expandStateText,
                        modifier = Modifier
                            .size(imageSize)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
                
                // Crossfade text animation
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    // Expanded state text
                    Text(
                        text = educationCard.expandStateText,
                        style = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Bold,
                            fontSize = textSize.sp,
                            lineHeight = (textSize * 1.4).sp,
                            textAlign = TextAlign.Center
                        ),
                        color = Color.White.copy(alpha = expandedTextAlpha),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Collapsed state text (overlayed)
                    Text(
                        text = educationCard.collapsedStateText,
                        style = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Bold,
                            fontSize = textSize.sp,
                            lineHeight = (textSize * 1.4285714285714286).sp,
                            textAlign = TextAlign.Center
                        ),
                        color = Color.White.copy(alpha = collapsedTextAlpha),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        
        CardState.COLLAPSED -> {
            // Collapsed layout with same animated elements
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Animated background frame (same element, different layout)
                Box(
                    modifier = Modifier
                        .width(backgroundFrameWidth)
                        .height(backgroundFrameHeight)
                        .background(
                            color = Color(0xFF7029CC),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Same shared animated image with optimized loading
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(educationCard.image)
                            .crossfade(true)
                            .placeholder(R.drawable.ic_image_placeholder)
                            .error(R.drawable.ic_image_error)
                            .fallback(R.drawable.ic_image_placeholder)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .allowHardware(true)
                            .build(),
                        contentDescription = educationCard.collapsedStateText,
                        modifier = Modifier
                            .size(imageSize)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
                
                // Crossfade text animation (same as expanded, but in row layout)
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    // Expanded state text
                    Text(
                        text = educationCard.expandStateText,
                        style = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Bold,
                            fontSize = textSize.sp,
                            lineHeight = (textSize * 1.4).sp,
                            textAlign = TextAlign.Center
                        ),
                        color = Color.White.copy(alpha = expandedTextAlpha),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Collapsed state text (overlayed)
                    Text(
                        text = educationCard.collapsedStateText,
                        style = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Bold,
                            fontSize = textSize.sp,
                            lineHeight = (textSize * 1.4285714285714286).sp,
                            textAlign = TextAlign.Center
                        ),
                        color = Color.White.copy(alpha = collapsedTextAlpha),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // Down arrow
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        else -> {
            // Hidden state - no content
        }
    }
}


