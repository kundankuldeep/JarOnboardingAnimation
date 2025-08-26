package com.app.jaronboardinganimation.ui.screens.onboarding

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.app.jaronboardinganimation.data.model.onboarding.EducationCard

/**
 * Extension functions for EducationCard to handle color parsing and gradient creation
 */

fun EducationCard.getBackgroundColor(): Color {
    return parseColor(backgroundColor)
}

fun EducationCard.getStrokeStartColor(): Color {
    return parseColor(strokeStartColor)
}

fun EducationCard.getStrokeEndColor(): Color {
    return parseColor(strokeEndColor)
}

fun EducationCard.getStartGradientColor(): Color {
    return parseColor(startGradient)
}

fun EducationCard.getEndGradientColor(): Color {
    return parseColor(endGradient)
}

/**
 * Creates a vertical gradient brush for the card background
 */
fun EducationCard.getBackgroundBrush(): Brush {
    return Brush.verticalGradient(
        colors = listOf(
            getStartGradientColor(),
            getEndGradientColor()
        )
    )
}

/**
 * Creates a vertical gradient brush for the card stroke
 */
fun EducationCard.getStrokeBrush(): Brush {
    return Brush.verticalGradient(
        colors = listOf(
            getStrokeStartColor(),
            getStrokeEndColor()
        )
    )
}

/**
 * Parses a color string (e.g., "#FFFFFF") to Compose Color
 */
fun parseColor(colorString: String): Color {
    return try {
        val cleanColorString = if (colorString.startsWith("#")) {
            colorString
        } else {
            "#$colorString"
        }
        Color(android.graphics.Color.parseColor(cleanColorString))
    } catch (e: Exception) {
        Color.Black // Fallback color
    }
}

/**
 * Validates if the education card list is valid for animation
 */
fun List<EducationCard>.isValidForAnimation(): Boolean {
    return isNotEmpty() && all { card ->
        card.image.isNotEmpty() && 
        card.collapsedStateText.isNotEmpty() && 
        card.expandStateText.isNotEmpty()
    }
}
