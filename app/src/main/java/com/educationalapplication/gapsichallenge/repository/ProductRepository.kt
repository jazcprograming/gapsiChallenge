package com.educationalapplication.gapsichallenge.repository

import com.educationalapplication.gapsichallenge.data.model.Product
import com.educationalapplication.gapsichallenge.data.model.toProducts
import com.educationalapplication.gapsichallenge.data.remote.ApiService
import retrofit2.Response

class ProductRepository(private val api: ApiService) {

    suspend fun searchProducts(keyword: String,page:Int): Result<List<Product>> {
        return try {
            val response = api.searchProducts(keyword=keyword,page=page)
            if (response.isSuccessful && response.body() != null) {
                val products = response.body()!!.toProducts()
                Result.success(products)
            } else {
                Result.failure(Exception("Error ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}