package com.app.jaronboardinganimation.data.api

import com.app.jaronboardinganimation.data.model.onboarding.EducationMetadataResponse
import retrofit2.Response
import retrofit2.http.GET

interface OnboardingApiService {
    
    @GET("_assets/shared/education-metadata.json")
    suspend fun getEducationMetadata(): Response<EducationMetadataResponse>
}
