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
                    // Initialize all cards as hidden initially
                    val cardStates = validCards.map { CardState.HIDDEN }
                    
                    _uiState.value = _uiState.value.copy(
                        currentScreen = OnboardingScreen.MAIN,
                        animationPhase = AnimationPhase.INITIAL,
                        cardStates = cardStates,
                        currentCardIndex = 0,
                        currentBackgroundColor = parseColor(validCards.first().backgroundColor),
                        isUserInteractionEnabled = false
                    )
                    
                    // Start card animation sequence
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
            
            for (cardIndex in 0 until cardCount) {
                // Update current card index and background color
                _uiState.value = _uiState.value.copy(
                    currentCardIndex = cardIndex,
                    currentBackgroundColor = parseColor(educationData.educationCardList[cardIndex].backgroundColor)
                )
                
                // Phase 1: Card slides up from bottom to center (PEEKING → EXPANDED)
                val newCardStates = _uiState.value.cardStates.toMutableList()
                
                // First make card peek if it's not already
                if (newCardStates[cardIndex] != CardState.PEEKING) {
                    newCardStates[cardIndex] = CardState.PEEKING
                    _uiState.value = _uiState.value.copy(
                        cardStates = newCardStates,
                        animationPhase = AnimationPhase.CARD_SLIDING_UP
                    )
                    delay(300) // Brief peek moment
                }
                
                // Now expand the card
                newCardStates[cardIndex] = CardState.EXPANDED
                _uiState.value = _uiState.value.copy(
                    cardStates = newCardStates,
                    animationPhase = AnimationPhase.CARD_EXPANDED
                )
                
                // Phase 2: Card stays expanded for 1400ms
                delay(1400)
                
                // If not the last card, collapse current card and show next card peeking
                if (cardIndex < cardCount - 1) {
                    // Phase 3: Current card collapses
                    _uiState.value = _uiState.value.copy(
                        animationPhase = AnimationPhase.CARD_COLLAPSING
                    )
                    
                    // Collapse current card and show next card peeking
                    newCardStates[cardIndex] = CardState.COLLAPSED
                    if (cardIndex + 1 < cardCount) {
                        newCardStates[cardIndex + 1] = CardState.PEEKING
                    }
                    
                    _uiState.value = _uiState.value.copy(cardStates = newCardStates)
                    
                    // Wait 800ms before next card slides up fully
                    delay(800)
                } else {
                    // Last card - show button after 1400ms
                    delay(1400)
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
        
        val newCardStates = _uiState.value.cardStates.toMutableList()
        newCardStates.forEachIndexed { index, _ ->
            newCardStates[index] = if (index == cardIndex) CardState.EXPANDED else CardState.COLLAPSED
        }
        
        val educationData = _uiState.value.educationData ?: return
        
        _uiState.value = _uiState.value.copy(
            cardStates = newCardStates,
            currentCardIndex = cardIndex,
            currentBackgroundColor = parseColor(educationData.educationCardList[cardIndex].backgroundColor)
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
