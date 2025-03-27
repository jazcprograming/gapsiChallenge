package com.educationalapplication.gapsichallenge.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.educationalapplication.gapsichallenge.data.local.RecentSearchRepository
import com.educationalapplication.gapsichallenge.data.model.Product
import com.educationalapplication.gapsichallenge.repository.ProductRepository
import com.educationalapplication.gapsichallenge.ui.screens.search.ProductScreenState
import com.educationalapplication.gapsichallenge.ui.screens.search.ProductUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val repository: ProductRepository,
    private val recentSearchRepo: RecentSearchRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProductScreenState())
    val state: StateFlow<ProductScreenState> = _state


    private var currentQuery: String = ""
    private var currentPage: Int = 1
    private var isLoading: Boolean = false
    private var hasMoreResults: Boolean = true
    private val accumulatedProducts = mutableListOf<Product>()

    init {
        viewModelScope.launch {
            recentSearchRepo.getRecentSearches().collect {
                updateRecentSearches(it)
            }
        }
    }
    fun search(keyword: String, reset: Boolean = false) {
        if (isLoading || (keyword == currentQuery && !reset && !hasMoreResults)) return

        if (reset || keyword != currentQuery) {
            currentQuery = keyword
            currentPage = 1
            hasMoreResults = true
            accumulatedProducts.clear()
            updateResultState(ProductUiState.Loading)
        }

        viewModelScope.launch {
            isLoading = true
            try {
                val currentList = _state.value.recentSearches
                if (!currentList.contains(keyword)) {
                    recentSearchRepo.saveSearch(keyword)
                }
                val result = repository.searchProducts(keyword, currentPage)

                result.onSuccess { products ->
                    hasMoreResults = products.isNotEmpty()
                    accumulatedProducts.addAll(products)
                    updateResultState(ProductUiState.Success(accumulatedProducts.toList()))
                    currentPage++
                }.onFailure { error ->
                    updateResultState(ProductUiState.Error("Error: ${error.message}"))
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

    fun updateQuery(newQuery:String){
        _state.value=_state.value.copy(query = newQuery)
    }
    fun updateIsDropdownExpanded(newisDropdownExpanded:Boolean){
        _state.value=_state.value.copy(isDropdownExpanded = newisDropdownExpanded)
    }
    fun updateRecentSearches(newrecentSearches:List<String>){
        _state.value=_state.value.copy(recentSearches = newrecentSearches)
    }
    fun updateResultState(newresultState:ProductUiState){
        _state.value=_state.value.copy(resultState = newresultState)
    }
}