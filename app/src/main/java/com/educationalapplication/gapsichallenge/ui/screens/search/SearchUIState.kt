package com.educationalapplication.gapsichallenge.ui.screens.search

import com.educationalapplication.gapsichallenge.data.model.Product

sealed class SearchUiState {
    object Idle : SearchUiState()
    object Loading : SearchUiState()
    data class Success(val products: List<Product>) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
} //lo puse acá porque hay una sola pantalla, es específico, si crece, cambiar a "utils"