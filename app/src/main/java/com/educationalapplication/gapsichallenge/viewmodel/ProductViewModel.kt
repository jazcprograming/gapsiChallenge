package com.educationalapplication.gapsichallenge.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.educationalapplication.gapsichallenge.data.model.Product
import com.educationalapplication.gapsichallenge.repository.ProductRepository
import com.educationalapplication.gapsichallenge.ui.screens.search.ProductUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val repository: ProductRepository
) : ViewModel() {

    private val _state = MutableStateFlow<ProductUiState>(ProductUiState.Idle)
    val state: StateFlow<ProductUiState> = _state

    private var currentQuery: String = ""
    private var currentPage: Int = 1
    private var isLoading: Boolean = false
    private var hasMoreResults: Boolean = true
    private val accumulatedProducts = mutableListOf<Product>()

    fun search(keyword: String, reset: Boolean = false) {
        if (isLoading || (keyword == currentQuery && !reset && !hasMoreResults)) return

        if (reset || keyword != currentQuery) {
            currentQuery = keyword
            currentPage = 1
            hasMoreResults = true
            accumulatedProducts.clear()
            _state.value = ProductUiState.Loading
        }

        viewModelScope.launch {
            isLoading = true
            try {
                val result = repository.searchProducts(keyword, currentPage)

                result.onSuccess { products ->
                    hasMoreResults = products.isNotEmpty()
                    accumulatedProducts.addAll(products)
                    _state.value = ProductUiState.Success(accumulatedProducts.toList())
                    currentPage++
                }.onFailure { error ->
                    _state.value = ProductUiState.Error("Error: ${error.message}")
                    hasMoreResults = false
                    throw error
                }
            } finally {
                isLoading = false
            }
        }
    }

    fun retry() {
        search(currentQuery, reset = false)
    }

    fun refresh() {
        search(currentQuery, reset = true)
    }
}