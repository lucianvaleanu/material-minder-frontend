package com.lucianvaleanu.materialminder.service.api

import com.lucianvaleanu.materialminder.model.ConstructionItem
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface ConstructionItemApiService{
    @GET("construction-items")
    suspend fun getAllConstructionItems(): List<ConstructionItem>

    @GET("construction-items/{id}")
    suspend fun getConstructionItemById(id: Int): ConstructionItem?

}