package com.suikamovie.app.ui.screens.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.suikamovie.app.data.local.PreferencesManager
import com.suikamovie.app.data.model.MediaDetail
import com.suikamovie.app.data.model.MediaType
import com.suikamovie.app.data.model.toSavedEntry
import com.suikamovie.app.data.remote.STREAM_SERVERS
import com.suikamovie.app.data.remote.buildUrl
import com.suikamovie.app.data.repository.AuthRepository
import com.suikamovie.app.data.repository.MediaRepository
import com.suikamovie.app.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DetailUiState(
    val isLoading: Boolean = true,
    val detail: MediaDetail? = null,
    val errorMessage: String? = null,
    val isFavorite: Boolean = false,
    val isPlaying: Boolean = false,
    val isFullscreen: Boolean = false,
    val activeServerIndex: Int = 0,
    val activeSeason: Int = 1,
    val activeEpisode: Int = 1,
)

class DetailViewModel(application: Application) : AndroidViewModel(application) {

    private val mediaRepository = MediaRepository()
    private val userRepository = UserRepository()
    private val authRepository = AuthRepository(application)
    private val prefs = PreferencesManager(application)

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    val autoRotateEnabled: StateFlow<Boolean> = prefs.autoRotateEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun load(id: Int, type: MediaType) {
        _uiState.value = DetailUiState(isLoading = true)
        viewModelScope.launch {
            val detail = mediaRepository.detail(id, type)
            val isFav = prefs.isFavorite(id, type)
            _uiState.value = if (detail != null) {
                DetailUiState(isLoading = false, detail = detail, isFavorite = isFav)
            } else {
                DetailUiState(isLoading = false, errorMessage = "Gagal memuat detail film.")
            }
        }
    }

    fun currentStreamUrl(): String? {
        val detail = _uiState.value.detail ?: return null
        val server = STREAM_SERVERS.getOrNull(_uiState.value.activeServerIndex) ?: STREAM_SERVERS.first()
        return server.buildUrl(detail.id, detail.type, _uiState.value.activeSeason, _uiState.value.activeEpisode)
    }

    fun selectServer(index: Int) {
        _uiState.value = _uiState.value.copy(activeServerIndex = index)
    }

    fun selectSeasonEpisode(season: Int, episode: Int) {
        _uiState.value = _uiState.value.copy(activeSeason = season, activeEpisode = episode)
    }

    /** Dipanggil begitu user pencet tombol "Putar". */
    fun startPlaying() {
        val detail = _uiState.value.detail ?: return
        _uiState.value = _uiState.value.copy(isPlaying = true)

        // Catet ke Riwayat Nonton + kasih EXP, sama kayak startInlinePlayer() di app.js dulu.
        viewModelScope.launch {
            prefs.addToWatchHistory(detail.toSavedEntry())
            authRepository.currentUser?.let { user ->
                runCatching { userRepository.awardWatchExp(user.uid) }
            }
        }
    }

    fun setFullscreen(fullscreen: Boolean) {
        _uiState.value = _uiState.value.copy(isFullscreen = fullscreen)
    }

    fun toggleFavorite() {
        val detail = _uiState.value.detail ?: return
        viewModelScope.launch {
            val nowFav = prefs.toggleFavorite(detail.toSavedEntry())
            _uiState.value = _uiState.value.copy(isFavorite = nowFav)
        }
    }
}
