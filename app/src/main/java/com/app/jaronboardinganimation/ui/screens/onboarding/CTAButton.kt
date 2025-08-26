package com.app.jaronboardinganimation.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionResult
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.app.jaronboardinganimation.data.model.onboarding.SaveButtonCta

/**
 * CTA Button with Lottie animation - Exact Figma Design Implementation
 * Based on: https://www.figma.com/design/ampUCP1qi5pGxZvmiG7jh1/Jar_External?node-id=1-6027
 */
@Composable
fun CTAButton(
    saveButtonCta: SaveButtonCta,
    lottieUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    forceLocalFallback: Boolean = false // Add parameter to force local testing
) {
    Row(
        modifier = modifier
            .background(
                color = saveButtonCta.getBackgroundColor(),
                shape = RoundedCornerShape(31.dp) // Figma: 31px border radius
            )
            .border(
                border = saveButtonCta.getBorderStroke(),
                shape = RoundedCornerShape(31.dp)
            )
            .clickable { onClick() }
            .padding(
                start = 24.dp,    // Figma: 24px left padding
                top = 2.dp,       // Figma: 2px top padding
                end = 16.dp,      // Figma: 16px right padding
                bottom = 2.dp     // Figma: 2px bottom padding
            ),
        horizontalArrangement = Arrangement.spacedBy((-4).dp), // Figma: -4px gap (overlap)
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Button text with exact Figma styling
        Text(
            text = saveButtonCta.text,
            style = TextStyle(
                fontFamily = FontFamily.Default, // Inter font (using system default)
                fontWeight = FontWeight.Bold,    // Figma: 700 weight
                fontSize = 14.sp,               // Figma: 14px
                lineHeight = (14 * 1.4285714285714286).sp, // Figma: specific line height
                color = saveButtonCta.getTextColor()
            )
        )

        // Lottie animation (if available)
        if (!lottieUrl.isNullOrBlank()) {
            LottieAnimationWithFallback(
                lottieUrl = lottieUrl,
                forceLocalFallback = forceLocalFallback,
                modifier = Modifier.size(44.dp)
            )
        }
    }
}

/**
 * Lottie Animation with comprehensive error handling and fallbacks
 * Handles both .json and .lottie format URLs
 */
@Composable
private fun LottieAnimationWithFallback(
    lottieUrl: String,
    forceLocalFallback: Boolean = false,
    modifier: Modifier = Modifier
) {
    var useLocalFallback by remember { mutableStateOf(true) } // Start with local always

    // Always load local first for immediate display
    val localCompositionResult: LottieCompositionResult = rememberLottieComposition(
        spec = LottieCompositionSpec.Asset("swipe_up_lottie.json")
    )

    // Load remote in background (only if not forcing local fallback)
    val remoteCompositionResult: LottieCompositionResult? = if (!forceLocalFallback) {
        rememberLottieComposition(
            spec = LottieCompositionSpec.Url(lottieUrl)
        )
    } else {
        null
    }

    // Determine which composition to use based on remote loading state
    val activeCompositionResult: LottieCompositionResult = when {
        forceLocalFallback -> localCompositionResult
        remoteCompositionResult?.value != null -> {
            // Remote loaded successfully, use it
            useLocalFallback = false
            remoteCompositionResult
        }

        remoteCompositionResult?.error != null -> {
            // Remote failed, stick with local
            useLocalFallback = true
            localCompositionResult
        }

        else -> {
            // Remote still loading or hasn't been attempted, use local
            useLocalFallback = true
            localCompositionResult
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = activeCompositionResult.value,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier.size(44.dp)
        )
    }
}