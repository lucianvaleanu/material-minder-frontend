package com.lucianvaleanu.materialminder.service.api

import com.lucianvaleanu.materialminder.model.Project
import retrofit2.http.*

interface ProjectApiService {
    @GET("projects/user/{userId}")
    suspend fun getAllProjectsByUserId(@Path("id") id:Int): List<Project>

    @GET("projects/{id}")
    suspend fun getProjectById(@Path("id") id: Int): Project?

    @POST("projects")
    suspend fun addProject(@Body project: Project): Project

    @PUT("projects/{id}")
    suspend fun updateProject(@Path("id") id: Int, @Body project: Project): Project

    @DELETE("projects/{id}")
    suspend fun deleteProject(@Path("id") id: Int): Boolean
}