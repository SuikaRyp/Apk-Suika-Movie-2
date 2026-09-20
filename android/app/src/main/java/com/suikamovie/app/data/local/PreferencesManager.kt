package com.suikamovie.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.suikamovie.app.data.model.MediaType
import com.suikamovie.app.data.model.SavedMediaEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "suikamovie_prefs")

private const val HISTORY_LIMIT = 30
private const val SEARCH_HISTORY_LIMIT = 10

/**
 * Pengganti localStorage dari versi web dulu. Simpen: riwayat nonton,
 * favorit/daftar saya, riwayat pencarian, dan setting auto-rotate.
 */
class PreferencesManager(private val context: Context) {

    private val gson = Gson()
    private val entryListType = object : TypeToken<List<SavedMediaEntry>>() {}.type
    private val stringListType = object : TypeToken<List<String>>() {}.type

    private object Keys {
        val AUTO_ROTATE = booleanPreferencesKey("auto_rotate_enabled")
        val WATCH_HISTORY = stringPreferencesKey("watch_history_json")
        val FAVORITES = stringPreferencesKey("favorites_json")
        val SEARCH_HISTORY = stringPreferencesKey("search_history_json")
    }

    // -------------------- Auto Rotate (Akun > Preferensi) --------------------

    val autoRotateEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.AUTO_ROTATE] ?: true }

    suspend fun setAutoRotateEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_ROTATE] = enabled }
    }

    // -------------------- Riwayat Nonton --------------------

    val watchHistory: Flow<List<SavedMediaEntry>> = context.dataStore.data.map { prefs ->
        prefs[Keys.WATCH_HISTORY]?.let { runCatching { gson.fromJson<List<SavedMediaEntry>>(it, entryListType) }.getOrNull() } ?: emptyList()
    }

    suspend fun addToWatchHistory(entry: SavedMediaEntry) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.WATCH_HISTORY]
                ?.let { runCatching { gson.fromJson<List<SavedMediaEntry>>(it, entryListType) }.getOrNull() }
                .orEmpty()
            val updated = (listOf(entry) + current.filterNot { it.id == entry.id && it.type == entry.type })
                .take(HISTORY_LIMIT)
            prefs[Keys.WATCH_HISTORY] = gson.toJson(updated)
        }
    }

    suspend fun clearWatchHistory() {
        context.dataStore.edit { it[Keys.WATCH_HISTORY] = gson.toJson(emptyList<SavedMediaEntry>()) }
    }

    // -------------------- Favorit / Daftar Saya --------------------

    val favorites: Flow<List<SavedMediaEntry>> = context.dataStore.data.map { prefs ->
        prefs[Keys.FAVORITES]?.let { runCatching { gson.fromJson<List<SavedMediaEntry>>(it, entryListType) }.getOrNull() } ?: emptyList()
    }

    /** @return true kalau abis di-toggle jadi favorit, false kalau abis dihapus dari favorit. */
    suspend fun toggleFavorite(entry: SavedMediaEntry): Boolean {
        var nowFavorited = false
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.FAVORITES]
                ?.let { runCatching { gson.fromJson<List<SavedMediaEntry>>(it, entryListType) }.getOrNull() }
                .orEmpty()
            val alreadyFav = current.any { it.id == entry.id && it.type == entry.type }
            val updated = if (alreadyFav) {
                current.filterNot { it.id == entry.id && it.type == entry.type }
            } else {
                listOf(entry) + current
            }
            nowFavorited = !alreadyFav
            prefs[Keys.FAVORITES] = gson.toJson(updated)
        }
        return nowFavorited
    }

    suspend fun isFavorite(id: Int, type: MediaType): Boolean {
        val current = favorites.first()
        return current.any { it.id == id && it.type == type }
    }

    // -------------------- Riwayat Pencarian --------------------

    val searchHistory: Flow<List<String>> = context.dataStore.data.map { prefs ->
        prefs[Keys.SEARCH_HISTORY]?.let { runCatching { gson.fromJson<List<String>>(it, stringListType) }.getOrNull() }
            ?: listOf("Avatar 3", "Squid Game 2", "Demon Slayer", "Siksa Kubur")
    }

    suspend fun addSearchQuery(query: String) {
        if (query.isBlank()) return
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.SEARCH_HISTORY]
                ?.let { runCatching { gson.fromJson<List<String>>(it, stringListType) }.getOrNull() }
                .orEmpty()
            val updated = (listOf(query) + current.filterNot { it.equals(query, ignoreCase = true) })
                .take(SEARCH_HISTORY_LIMIT)
            prefs[Keys.SEARCH_HISTORY] = gson.toJson(updated)
        }
    }

    suspend fun clearSearchHistory() {
        context.dataStore.edit { it[Keys.SEARCH_HISTORY] = gson.toJson(emptyList<String>()) }
    }
}
