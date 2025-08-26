package com.app.jaronboardinganimation.data.repository

import com.app.jaronboardinganimation.data.api.OnboardingApiService
import com.app.jaronboardinganimation.data.model.onboarding.EducationMetadataResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnboardingRepository @Inject constructor(
    private val onboardingApiService: OnboardingApiService
) {
    
    fun getEducationMetadata(): Flow<Result<EducationMetadataResponse>> = flow {
        try {
            val response = onboardingApiService.getEducationMetadata()
            if (response.isSuccessful) {
                response.body()?.let { educationData ->
                    emit(Result.success(educationData))
                } ?: emit(Result.failure(Exception("Empty response")))
            } else {
                emit(Result.failure(Exception("Error: ${response.code()} - ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
