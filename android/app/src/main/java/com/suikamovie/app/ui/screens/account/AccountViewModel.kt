package com.suikamovie.app.ui.screens.account

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.suikamovie.app.data.local.PreferencesManager
import com.suikamovie.app.data.model.LevelInfo
import com.suikamovie.app.data.model.UserProfile
import com.suikamovie.app.data.model.getLevelInfo
import com.suikamovie.app.data.repository.AuthRepository
import com.suikamovie.app.data.repository.UserRepository
import com.suikamovie.app.data.repository.isAdminEmail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AccountUiState(
    val isLoading: Boolean = true,
    val profile: UserProfile? = null,
    val levelInfo: LevelInfo = getLevelInfo(0),
    val isAdmin: Boolean = false,
    val autoRotateEnabled: Boolean = true,
)

class AccountViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(application)
    private val userRepository = UserRepository()
    private val prefs = PreferencesManager(application)

    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
        viewModelScope.launch {
            prefs.autoRotateEnabled.collect { enabled ->
                _uiState.value = _uiState.value.copy(autoRotateEnabled = enabled)
            }
        }
    }

    fun loadProfile() {
        val user = authRepository.currentUser ?: run {
            _uiState.value = _uiState.value.copy(isLoading = false, profile = null)
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            val profile = runCatching { userRepository.ensureUserProfile(user) }.getOrNull()
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                profile = profile,
                levelInfo = getLevelInfo(profile?.exp ?: 0),
                isAdmin = isAdminEmail(user.email),
            )
        }
    }

    fun setAutoRotate(enabled: Boolean) {
        viewModelScope.launch { prefs.setAutoRotateEnabled(enabled) }
    }

    fun signOut(onSignedOut: () -> Unit) {
        authRepository.signOut()
        _uiState.value = AccountUiState(isLoading = false, profile = null)
        onSignedOut()
    }
}
