package com.app.jaronboardinganimation.ui.screens.onboarding

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.jaronboardinganimation.data.model.onboarding.ManualBuyEducationData
import com.app.jaronboardinganimation.data.repository.OnboardingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboardingRepository: OnboardingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()
    
    private var animationJob: Job? = null

    init {
        loadEducationMetadata()
    }

    private fun loadEducationMetadata() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            onboardingRepository.getEducationMetadata().collect { result ->
                result.fold(
                    onSuccess = { response ->
                        if (response.success) {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                educationData = response.data.manualBuyEducationData,
                                error = null,
                                currentScreen = OnboardingScreen.WELCOME
                            )
                            // Auto transition to main onboarding after 800ms
                            startWelcomeTimer()
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = "Failed to load onboarding data"
                            )
                        }
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = exception.message ?: "Unknown error occurred"
                        )
                    }
                )
            }
        }
    }

    private fun startWelcomeTimer() {
        viewModelScope.launch {
            delay(800) // 800ms delay
            
            // Check if we have education cards
            val educationData = _uiState.value.educationData
            if (educationData != null && educationData.educationCardList.isNotEmpty()) {
                // Validate cards have required content
                val validCards = educationData.educationCardList.filter { card ->
                    card.image.isNotBlank() && 
                    card.collapsedStateText.isNotBlank() && 
                    card.expandStateText.isNotBlank()
                }
                
                if (validCards.isNotEmpty()) {
                    // Initialize all cards as hidden initially - they'll slide in from bottom
                    val cardStates = validCards.map { CardState.HIDDEN }
                    
                    _uiState.value = _uiState.value.copy(
                        currentScreen = OnboardingScreen.MAIN,
                        animationPhase = AnimationPhase.INITIAL,
                        cardStates = cardStates,
                        currentCardIndex = 0,
                        currentBackgroundColor = parseColor(validCards.first().backgroundColor),
                        isUserInteractionEnabled = false
                    )
                    
                    // Start card animation sequence - all cards will slide in from bottom
                    startCardAnimationSequence()
                } else {
                    // No valid cards, show fallback
                    _uiState.value = _uiState.value.copy(
                        currentScreen = OnboardingScreen.MAIN,
                        animationPhase = AnimationPhase.SEQUENCE_COMPLETE,
                        showButton = true,
                        isUserInteractionEnabled = true
                    )
                }
            } else {
                // No cards, show fallback
                _uiState.value = _uiState.value.copy(
                    currentScreen = OnboardingScreen.MAIN,
                    animationPhase = AnimationPhase.SEQUENCE_COMPLETE,
                    showButton = true,
                    isUserInteractionEnabled = true
                )
            }
        }
    }
    
    private fun startCardAnimationSequence() {
        animationJob?.cancel()
        animationJob = viewModelScope.launch {
            val educationData = _uiState.value.educationData ?: return@launch
            val cardCount = educationData.educationCardList.size
            
            // Initial 200ms delay before first card appears
            delay(200)
            
            // Simple sequential animation: each card slides in, collapses to position, next card comes
            
            for (cardIndex in 0 until cardCount) {
                // Update current card and background
                _uiState.value = _uiState.value.copy(
                    currentCardIndex = cardIndex,
                    currentBackgroundColor = parseColor(educationData.educationCardList[cardIndex].backgroundColor),
                    animationPhase = AnimationPhase.CARD_SLIDING_UP
                )
                
                val cardStates = _uiState.value.cardStates.toMutableList()
                
                // Check if card is already peeking from previous iteration
                if (cardStates.getOrNull(cardIndex) == CardState.PEEKING) {
                    // Card is already peeking - just scroll it up to expanded position
                    cardStates[cardIndex] = CardState.EXPANDED
                    _uiState.value = _uiState.value.copy(
                        cardStates = cardStates,
                        animationPhase = AnimationPhase.CARD_EXPANDED
                    )
                } else {
                    // Card slides in from bottom and expands
                    cardStates[cardIndex] = CardState.EXPANDED
                    _uiState.value = _uiState.value.copy(
                        cardStates = cardStates,
                        animationPhase = AnimationPhase.CARD_EXPANDED
                    )
                }
                
                // Wait (card stays expanded)
                delay(1400)
                
                if (cardIndex < cardCount - 1) {
                    // Not the last card - collapse current + peek next simultaneously
                    _uiState.value = _uiState.value.copy(animationPhase = AnimationPhase.CARD_COLLAPSING)
                    
                    // Collapse current card AND peek next card simultaneously  
                    cardStates[cardIndex] = CardState.COLLAPSED
                    cardStates[cardIndex + 1] = CardState.PEEKING // Next card peeks at 40%
                    _uiState.value = _uiState.value.copy(cardStates = cardStates)
                    
                    // Wait 800ms for collapse + peek animation
                    delay(800)
                } else {
                    // Last card - stays expanded, enable user interaction
                    _uiState.value = _uiState.value.copy(
                        animationPhase = AnimationPhase.SEQUENCE_COMPLETE,
                        showButton = true,
                        isUserInteractionEnabled = true
                    )
                }
            }
        }
    }
    
    fun onCardClick(cardIndex: Int) {
        if (!_uiState.value.isUserInteractionEnabled) return
        
        val educationData = _uiState.value.educationData ?: return
        val newCardStates = _uiState.value.cardStates.toMutableList()
        
        // Expand clicked card, collapse all others (maintaining order)
        newCardStates.forEachIndexed { index, _ ->
            newCardStates[index] = if (index == cardIndex) CardState.EXPANDED else CardState.COLLAPSED
        }
        
        _uiState.value = _uiState.value.copy(
            cardStates = newCardStates,
            currentCardIndex = cardIndex,
            currentBackgroundColor = parseColor(educationData.educationCardList[cardIndex].backgroundColor),
            animationPhase = AnimationPhase.CARD_EXPANDED
        )
    }
    
    private fun parseColor(colorString: String): Color {
        return try {
            Color(android.graphics.Color.parseColor(colorString))
        } catch (e: Exception) {
            Color.Black // Fallback color
        }
    }

    fun retryLoading() {
        loadEducationMetadata()
    }
    
    fun pauseAnimations() {
        animationJob?.cancel()
    }
    
    fun resumeAnimations() {
        if (_uiState.value.animationPhase != AnimationPhase.SEQUENCE_COMPLETE) {
            startCardAnimationSequence()
        }
    }

    
    override fun onCleared() {
        super.onCleared()
        animationJob?.cancel()
    }
}

enum class OnboardingScreen {
    WELCOME,
    MAIN
}

enum class AnimationPhase {
    INITIAL,
    CARD_SLIDING_UP,
    CARD_EXPANDED,
    CARD_COLLAPSING,
    SEQUENCE_COMPLETE
}

enum class CardState {
    HIDDEN,
    PEEKING,
    EXPANDED,
    COLLAPSED
}

data class OnboardingUiState(
    val isLoading: Boolean = false,
    val educationData: ManualBuyEducationData? = null,
    val error: String? = null,
    val currentScreen: OnboardingScreen = OnboardingScreen.WELCOME,
    val animationPhase: AnimationPhase = AnimationPhase.INITIAL,
    val cardStates: List<CardState> = emptyList(),
    val currentCardIndex: Int = 0,
    val currentBackgroundColor: Color = Color.Black,
    val showButton: Boolean = false,
    val isUserInteractionEnabled: Boolean = false
)
