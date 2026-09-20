package com.suikamovie.app.ui.screens.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.suikamovie.app.data.local.PreferencesManager
import com.suikamovie.app.data.model.MediaItem
import com.suikamovie.app.data.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val isSearching: Boolean = false,
    val results: List<MediaItem> = emptyList(),
    val hasSearched: Boolean = false,
)

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MediaRepository()
    private val prefs = PreferencesManager(application)

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    val searchHistory: StateFlow<List<String>> = prefs.searchHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
    }

    fun search(query: String = _uiState.value.query) {
        if (query.isBlank()) return
        _uiState.value = _uiState.value.copy(isSearching = true, query = query)
        viewModelScope.launch {
            prefs.addSearchQuery(query.trim())
            val results = repository.search(query.trim())
            _uiState.value = _uiState.value.copy(isSearching = false, results = results, hasSearched = true)
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch { prefs.clearSearchHistory() }
    }
}
