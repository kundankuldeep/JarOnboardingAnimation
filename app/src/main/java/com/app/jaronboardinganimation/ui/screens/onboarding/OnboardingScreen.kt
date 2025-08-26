package com.app.jaronboardinganimation.ui.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.app.jaronboardinganimation.R
import com.app.jaronboardinganimation.ui.theme.primaryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onNavigateToDashboard: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Handle lifecycle events for animation management
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    viewModel.pauseAnimations()
                }

                Lifecycle.Event.ON_RESUME -> {
                    viewModel.resumeAnimations()
                }

                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Animated background color
    val animatedBackgroundColor by animateColorAsState(
        targetValue = if (uiState.currentScreen == OnboardingScreen.MAIN && uiState.educationData != null) {
            uiState.currentBackgroundColor
        } else {
            primaryColor
        },
        animationSpec = tween(durationMillis = 500),
        label = "background_color"
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(animatedBackgroundColor),
        containerColor = Color.Transparent,
        topBar = {
            // Show top bar only for main onboarding screen
            if (uiState.currentScreen == OnboardingScreen.MAIN && uiState.educationData != null) {
                TopAppBar(
                    title = {
                        Text(
                            text = uiState.educationData!!.toolBarText,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        // As this is the first screen of onboarding, no back button is needed,
                        // we can implement here if there is special use case for back button
//                        IconButton(onClick = { /* TODO */ }) {
//                            Icon(
//                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
//                                contentDescription = "Back",
//                                tint = Color.White
//                            )
//                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }

                uiState.error != null -> {
                    val error = uiState.error!! // Extract to local variable for smart cast
                    ErrorContent(
                        error = error,
                        onRetry = { viewModel.retryLoading() },
                        onContinue = onNavigateToDashboard
                    )
                }

                uiState.educationData != null -> {
                    when (uiState.currentScreen) {
                        OnboardingScreen.WELCOME -> {
                            WelcomeContent(educationData = uiState.educationData!!)
                        }

                        OnboardingScreen.MAIN -> {
                            MainOnboardingContent(
                                uiState = uiState,
                                onNavigateToDashboard = onNavigateToDashboard,
                                onCardClick = { cardIndex -> viewModel.onCardClick(cardIndex) }
                            )
                        }
                    }
                }

                else -> {
                    // Fallback UI with null data (will use string resources)
                    WelcomeContent(educationData = null)
                }
            }
        }
    }
}

@Composable
private fun WelcomeContent(
    educationData: com.app.jaronboardinganimation.data.model.onboarding.ManualBuyEducationData?
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = educationData?.introTitle ?: stringResource(R.string.welcome_to),
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            color = Color.White
        )
        Text(
            text = educationData?.introSubtitle ?: stringResource(R.string.onboarding),
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            color = Color(0xFFD4AF37) // Golden color
        )
    }
}

@Composable
private fun MainOnboardingContent(
    uiState: OnboardingUiState,
    onNavigateToDashboard: () -> Unit,
    onCardClick: (Int) -> Unit
) {
    val educationData = uiState.educationData

    if (educationData == null) {
        // Fallback UI when no education data
        FallbackOnboardingContent(onNavigateToDashboard)
        return
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (educationData.educationCardList.isNotEmpty()) {
            // Animated Cards Container
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                educationData.educationCardList.forEachIndexed { index, card ->
                    val cardState = uiState.cardStates.getOrNull(index) ?: CardState.HIDDEN

                    AnimatedEducationCard(
                        educationCard = card,
                        cardState = cardState,
                        cardIndex = index,
                        totalCards = educationData.educationCardList.size,
                        animationPhase = uiState.animationPhase,
                        onClick = { onCardClick(index) }
                    )
                }
            }
        } else {
            // No cards available - show fallback
            FallbackOnboardingContent(onNavigateToDashboard)
        }

        // Animated CTA Button at bottom
        AnimatedVisibility(
            visible = uiState.showButton,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(
                    durationMillis = 600,
                    easing = LinearOutSlowInEasing
                )
            ),
            modifier = Modifier
                .wrapContentWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            if (educationData.saveButtonCta.text.isNotBlank()) {
                CTAButton(
                    saveButtonCta = educationData.saveButtonCta,
                    lottieUrl = educationData.ctaLottie,
                    onClick = onNavigateToDashboard
                )
            } else {
                // Fallback button
                Button(
                    onClick = onNavigateToDashboard,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.onboarding_button))
                }
            }
        }
    }
}

@Composable
private fun FallbackOnboardingContent(onNavigateToDashboard: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.welcome_to),
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.onboarding),
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            color = Color(0xFFD4AF37)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onNavigateToDashboard,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.onboarding_button))
        }
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit,
    onContinue: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Error: $error",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onRetry) {
            Text("Retry")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onContinue) {
            Text("Continue Anyway")
        }
    }
}