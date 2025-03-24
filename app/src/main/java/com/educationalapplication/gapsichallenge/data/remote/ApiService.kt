package com.educationalapplication.gapsichallenge.data.remote

import com.educationalapplication.gapsichallenge.data.model.Product
import com.educationalapplication.gapsichallenge.data.model.ProductsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("wlm/walmart-search-by-keyword")
    suspend fun searchProducts(
        @Query("keyword") keyword: String,
        @Query("page") page: Int = 1,
        @Query("sortBy") sortBy: String = "best_match"
    ): Response<ProductsResponse>
}