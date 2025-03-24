package com.educationalapplication.gapsichallenge.ui.screens.search

import com.educationalapplication.gapsichallenge.data.model.Product

sealed class ProductUiState {
    object Idle : ProductUiState()
    object Loading : ProductUiState()
    data class Success(val products: List<Product>) : ProductUiState()
    data class Error(val message: String) : ProductUiState()
} //lo puse acá porque hay una sola pantalla, es específico, si crece, cambiar a "utils"