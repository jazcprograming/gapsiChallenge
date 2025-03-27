package com.educationalapplication.gapsichallenge.ui.screens.search

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.educationalapplication.gapsichallenge.data.model.Product
import com.educationalapplication.gapsichallenge.viewmodel.ProductViewModel


@Composable
fun ProductScreen(viewModel: ProductViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        ProductSearchSection( viewModel= viewModel, state= state)
        Spacer(modifier = Modifier.height(16.dp))
        ProductListSection(viewModel= viewModel,state=state,listState=listState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductSearchSection(viewModel: ProductViewModel, state: ProductScreenState){
    val focusManager = LocalFocusManager.current
    Row(){
        ExposedDropdownMenuBox(
            expanded = state.isDropdownExpanded && state.recentSearches.isNotEmpty(),
            modifier = Modifier.weight(1f),
            onExpandedChange = {
                viewModel.updateIsDropdownExpanded(it)
            }
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = {
                    viewModel.updateQuery(it)
                    viewModel.updateIsDropdownExpanded(true)
                },
                label = { Text("Buscar producto") },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )
            ExposedDropdownMenu(
                expanded = state.isDropdownExpanded && state.recentSearches.isNotEmpty(),
                onDismissRequest = {
                    viewModel.updateIsDropdownExpanded(false)
                }
            ) {
                state.recentSearches.forEach { search ->
                    DropdownMenuItem(
                        text = { Text(search) },
                        onClick = {
                            viewModel.updateQuery(search)
                            viewModel.search(search, reset = true)
                            viewModel.updateIsDropdownExpanded(false)
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
                viewModel.search(state.query.trim(), reset = true)
            },
            modifier = Modifier.align(Alignment.CenterVertically)
        ) {
            Text("Buscar")
        }
    }
}

@Composable
fun ProductListSection(viewModel: ProductViewModel, state: ProductScreenState, listState: LazyListState){
    Box(modifier = Modifier.fillMaxSize()) {
        when (state.resultState) {
            is ProductUiState.Loading ->
                LoadingMessage(message="Cargando Datos")

            is ProductUiState.Error ->
                LoadingMessage(message=state.resultState.message,color=MaterialTheme.colorScheme.error)

            is ProductUiState.Success ->
                ProductGrid(viewModel= viewModel,state=state,listState=listState)

            ProductUiState.Idle -> Unit
        }
    }
}

@Composable
fun ProductGrid(viewModel: ProductViewModel, state: ProductScreenState, listState: LazyListState){
    val products = (state.resultState as ProductUiState.Success).products
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
                    ProductCard(product)
                }
                if (pair.size < 2) {
                    Spacer(modifier = Modifier.weight(1f)) // Ponerlo a la izquierda
                }
            }
        }
        // Loader de carga extra
        item {
            //validación para cuando ya no hay más productos, quitar loader o poner anuncio de es el fin
            Spacer(modifier = Modifier.height(12.dp))
            LoadingMessage(message = "Cargando más productos, aguarde...")
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
            viewModel.search(state.query.trim(), reset = false)
        }
    }
}

@Composable
fun RowScope.ProductCard(product: Product){
    Card(
        modifier = Modifier
            .weight(1f)
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


@Composable
fun LoadingMessage(message:String,color: Color=Color.Unspecified){
    //CircularProgressIndicator(modifier = Modifier.size(24.dp))
    //Separar en tipos de loadings
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            message,
            color = color
        )
    }
}