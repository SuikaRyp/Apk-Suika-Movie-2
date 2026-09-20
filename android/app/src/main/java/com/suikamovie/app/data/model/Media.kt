package com.suikamovie.app.data.model

/** Tipe konten: film atau serial TV - dipakai di mana-mana. */
enum class MediaType(val apiValue: String) {
    MOVIE("movie"),
    TV("tv");

    companion object {
        fun from(value: String?): MediaType = if (value == "tv") TV else MOVIE
    }
}

/**
 * Item media buat ditampilin di grid/carousel (Beranda, Pencarian, Riwayat,
 * dll) - versi ringkas, belum lengkap kayak MediaDetail.
 */
data class MediaItem(
    val id: Int,
    val title: String,
    val type: MediaType,
    val year: String,
    val rating: String,
    val poster: String?,
    val backdrop: String? = null,
    val overview: String = "",
)

/** Info satu season serial TV (dipakai di dropdown pemilihan season). */
data class SeasonInfo(
    val seasonNumber: Int,
    val name: String,
    val episodeCount: Int,
)

/** Detail lengkap satu film/serial - dipakai di DetailScreen. */
data class MediaDetail(
    val id: Int,
    val title: String,
    val type: MediaType,
    val year: String,
    val rating: String,
    val runtime: String,
    val genres: List<String>,
    val country: String,
    val overview: String,
    val poster: String?,
    val backdrop: String?,
    val seasons: Int = 0,
    val seasonsDetail: List<SeasonInfo> = emptyList(),
)

/**
 * Entri media yang disimpen ke DataStore (pengganti localStorage dulu) -
 * dipakai buat DUA hal: Riwayat Nonton & Favorit/Daftar Saya, biar nggak
 * dobel bikin data class yang isinya sama persis.
 */
data class SavedMediaEntry(
    val id: Int,
    val type: MediaType,
    val title: String,
    val poster: String?,
    val rating: String,
    val year: String,
    val timestamp: Long,
)

typealias WatchHistoryEntry = SavedMediaEntry

fun MediaDetail.toMediaItem() = MediaItem(
    id = id,
    title = title,
    type = type,
    year = year,
    rating = rating,
    poster = poster,
    backdrop = backdrop,
    overview = overview,
)

fun MediaItem.toSavedEntry(timestamp: Long = System.currentTimeMillis()) = SavedMediaEntry(
    id = id,
    type = type,
    title = title,
    poster = poster,
    rating = rating,
    year = year,
    timestamp = timestamp,
)

fun MediaDetail.toSavedEntry(timestamp: Long = System.currentTimeMillis()) = SavedMediaEntry(
    id = id,
    type = type,
    title = title,
    poster = poster,
    rating = rating,
    year = year,
    timestamp = timestamp,
)

fun SavedMediaEntry.toMediaItem() = MediaItem(
    id = id,
    title = title,
    type = type,
    year = year,
    rating = rating,
    poster = poster,
)
