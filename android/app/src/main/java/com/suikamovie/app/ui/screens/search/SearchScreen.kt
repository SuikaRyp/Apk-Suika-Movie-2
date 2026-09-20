package com.suikamovie.app.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.suikamovie.app.ui.components.MediaCard
import com.suikamovie.app.ui.theme.BgBody
import com.suikamovie.app.ui.theme.BgSurface
import com.suikamovie.app.ui.theme.PrimaryBlue
import com.suikamovie.app.ui.theme.TextLight
import com.suikamovie.app.ui.theme.TextMain

@Composable
fun SearchScreen(onOpenDetail: (id: Int, type: String) -> Unit) {
    val viewModel: SearchViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val history by viewModel.searchHistory.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().background(BgBody).padding(16.dp)) {
        OutlinedTextField(
            value = uiState.query,
            onValueChange = viewModel::onQueryChange,
            placeholder = { Text("Cari judul film...", color = TextLight) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = TextLight) },
            singleLine = true,
            shape = RoundedCornerShape(50),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { viewModel.search() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextMain,
                unfocusedTextColor = TextMain,
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = TextLight,
                focusedContainerColor = BgSurface,
                unfocusedContainerColor = BgSurface,
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        Box(modifier = Modifier.fillMaxSize().padding(top = 16.dp)) {
            val stateKey = when {
                uiState.isSearching -> "loading"
                uiState.hasSearched && uiState.results.isEmpty() -> "empty"
                uiState.hasSearched -> "results"
                else -> "history"
            }

            androidx.compose.animation.Crossfade(targetState = stateKey, label = "search-state-crossfade") { key ->
                when (key) {
                    "loading" -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            CircularProgressIndicator(color = PrimaryBlue, modifier = Modifier.align(Alignment.Center))
                        }
                    }
                    "empty" -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Text(
                                "Nggak ketemu hasil buat \"${uiState.query}\"",
                                color = TextLight,
                                modifier = Modifier.align(Alignment.Center),
                            )
                        }
                    }
                    "results" -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(bottom = 24.dp),
                        ) {
                            items(uiState.results, key = { "${it.type.apiValue}-${it.id}" }) { media ->
                                MediaCard(
                                    item = media,
                                    modifier = Modifier.animateItem(),
                                    fillWidth = true,
                                    onClick = { onOpenDetail(media.id, media.type.apiValue) },
                                )
                            }
                        }
                    }
                    else -> {
                        Column {
                            Text("Pencarian Terakhir", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            history.forEach { q ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.search(q) }
                                        .padding(vertical = 10.dp),
                                ) {
                                    Icon(Icons.Filled.Search, contentDescription = null, tint = TextLight)
                                    Text(q, color = TextMain, modifier = Modifier.padding(start = 10.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
