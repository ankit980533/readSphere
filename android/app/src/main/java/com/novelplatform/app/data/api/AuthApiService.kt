package com.novelplatform.app.data.api

import com.novelplatform.app.data.model.AuthResponse
import com.novelplatform.app.data.model.LoginRequest
import com.novelplatform.app.data.model.SignupRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    
    @POST("api/auth/signup")
    suspend fun signup(@Body request: SignupRequest): AuthResponse
    
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse
}
