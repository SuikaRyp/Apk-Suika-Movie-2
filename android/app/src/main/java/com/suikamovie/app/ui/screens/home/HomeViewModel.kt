package com.suikamovie.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suikamovie.app.data.model.MediaItem
import com.suikamovie.app.data.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val heroItems: List<MediaItem> = emptyList(),
    val trending: List<MediaItem> = emptyList(),
    val popularMovies: List<MediaItem> = emptyList(),
    val popularTv: List<MediaItem> = emptyList(),
    val errorMessage: String? = null,
)

class HomeViewModel : ViewModel() {

    private val repository = MediaRepository()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHome()
    }

    fun loadHome() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val trending = repository.trending()
            val movies = repository.popularMovies()
            val tv = repository.popularTv()

            _uiState.value = HomeUiState(
                isLoading = false,
                heroItems = trending.take(5),
                trending = trending,
                popularMovies = movies,
                popularTv = tv,
            )
        }
    }
}
