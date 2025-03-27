package com.educationalapplication.gapsichallenge.ui.screens.search

data class ProductScreenState(
    val query: String = "",
    val isDropdownExpanded: Boolean = false,
    val recentSearches: List<String> = emptyList(),
    val resultState: ProductUiState = ProductUiState.Idle
)