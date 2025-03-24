package com.educationalapplication.gapsichallenge.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults

import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.educationalapplication.gapsichallenge.ui.screens.search.ProductUiState
import com.educationalapplication.gapsichallenge.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(viewModel: ProductViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current

    val isSearchFocused = remember { mutableStateOf(false) }

    var query by rememberSaveable { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val recentSearches by viewModel.recentSearches.collectAsState()

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Row(){
            ExposedDropdownMenuBox(
                expanded = expanded && recentSearches.isNotEmpty(),
                modifier = Modifier.weight(1f),
                onExpandedChange = {
                    expanded = it
                }
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = {
                        query = it
                        expanded = true
                    },
                    label = { Text("Buscar producto") },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )

                ExposedDropdownMenu(
                    expanded = expanded && recentSearches.isNotEmpty(),
                    onDismissRequest = {
                        expanded = false
                    }
                ) {
                    recentSearches.forEach { search ->
                        DropdownMenuItem(
                            text = { Text(search) },
                            onClick = {
                                query = search
                                viewModel.search(search, reset = true)
                                expanded = false
                                focusManager.clearFocus()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.search(query.trim(), reset = true)
                },
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Text("Buscar")
            }
        }



        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.fillMaxSize()) {
            when (state) {
                is ProductUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "Cargando Datos")
                    //CircularProgressIndicator()
                    }
                }

                is ProductUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            (state as ProductUiState.Error).message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                is ProductUiState.Success -> {
                    val products = (state as ProductUiState.Success).products
                    val productPairs = products.chunked(2)
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState
                    ) {
                        items(productPairs) { pair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                pair.forEach { product ->
                                    Card(
                                        modifier = Modifier.weight(1f)
                                            .fillMaxWidth()
                                            .padding(4.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                    ) {
                                        Column(Modifier.padding(10.dp)) {
                                            AsyncImage(
                                                model = product.thumbnail,
                                                contentDescription = product.title,
                                                modifier = Modifier
                                                    .align(Alignment.CenterHorizontally)
                                                    .fillMaxWidth()
                                                    .aspectRatio(1f)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .padding(bottom = 8.dp),
                                                contentScale = ContentScale.Crop
                                            )
                                            Text(modifier=Modifier.padding(bottom = 8.dp),
                                                text = product.title,
                                                style = MaterialTheme.typography.titleMedium,
                                                maxLines = 2, overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "$"+product.price.toString(),
                                                style = MaterialTheme.typography.bodyMedium
                                            )

                                        }
                                    }

                                }

                                if (pair.size < 2) {
                                    Spacer(modifier = Modifier.weight(1f)) // Ponerlo a la izquierda
                                }
                            }
                            //Divider(modifier = Modifier.padding(vertical = 8.dp))//nose ve bien, cambiar a Cards
                        }


                        // Loader de carga extra
                        item {
                            //validación para cuando ya no hay más productos, quitar loader o poner anuncio de es el fin
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text(text = "Cargando más productos, aguarde...")
                                //CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        }
                    }

                    // Detectar fin de scroll
                    val shouldLoadMore = remember {
                        derivedStateOf {
                            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()
                            val totalItems = listState.layoutInfo.totalItemsCount
                            lastVisible != null && lastVisible.index >= totalItems - 2
                        }
                    }

                    LaunchedEffect(shouldLoadMore.value) {
                        if (shouldLoadMore.value) {
                            viewModel.search(query.trim(), reset = false)
                        }
                    }
                }

                ProductUiState.Idle -> Unit
            }
        }
    }
}
