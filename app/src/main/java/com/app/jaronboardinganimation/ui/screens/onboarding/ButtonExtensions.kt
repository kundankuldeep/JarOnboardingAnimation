package com.app.jaronboardinganimation.ui.screens.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.app.jaronboardinganimation.data.model.onboarding.SaveButtonCta

/**
 * Extension functions for SaveButtonCta to handle button styling
 */

fun SaveButtonCta.getBackgroundColor(): Color {
    return parseColor(backgroundColor)
}

fun SaveButtonCta.getTextColor(): Color {
    return parseColor(textColor)
}

fun SaveButtonCta.getStrokeColor(): Color {
    return parseColor(strokeColor)
}

/**
 * Creates BorderStroke for Material3 Button based on SaveButtonCta
 */
fun SaveButtonCta.getBorderStroke(): BorderStroke {
    return BorderStroke(
        width = 1.dp,
        color = getStrokeColor()
    )
}
