package com.suikamovie.app.data.repository

import com.suikamovie.app.data.model.MediaDetail
import com.suikamovie.app.data.model.MediaItem
import com.suikamovie.app.data.model.MediaType
import com.suikamovie.app.data.model.SeasonInfo
import com.suikamovie.app.data.remote.RetrofitClient
import com.suikamovie.app.data.remote.TMDB_IMG
import com.suikamovie.app.data.remote.TMDB_IMG_ORIGINAL
import com.suikamovie.app.data.remote.TmdbDetailResponse
import com.suikamovie.app.data.remote.TmdbResult
import kotlin.random.Random

private const val FALLBACK_POSTER =
    "https://images.unsplash.com/photo-1536440136628-849c177e76a1?auto=format&fit=crop&w=400&q=80"
private const val FALLBACK_BACKDROP =
    "https://images.unsplash.com/photo-1574375927938-d5a98e8ffe85?auto=format&fit=crop&w=1000&q=80"

/**
 * Sumber data film/TV dari TMDB - port langsung dari class SuikaMovieAPI di
 * public/app.js (dulu). Tiap fungsi punya try/catch + fallback biar app
 * nggak nge-crash total kalau TMDB lagi down/limit.
 */
class MediaRepository {

    private val api = RetrofitClient.tmdbApi

    suspend fun search(query: String): List<MediaItem> = runCatching {
        api.searchMulti(query = query).results
            .filter { it.mediaType == "movie" || it.mediaType == "tv" }
            .mapNotNull { it.toMediaItem() }
    }.getOrElse { emptyList() }

    suspend fun trending(): List<MediaItem> = runCatching {
        val page = Random.nextInt(1, 5)
        val results = api.trendingAllWeek(page = page).results
            .filter { it.mediaType == "movie" || it.mediaType == "tv" }
            .mapNotNull { it.toMediaItem() }
        results.ifEmpty { fallbackList() }.shuffled()
    }.getOrElse { fallbackList().shuffled() }

    suspend fun popularMovies(): List<MediaItem> = runCatching {
        val page = Random.nextInt(1, 6)
        val results = api.popularMovies(page = page).results.mapNotNull {
            it.toMediaItem(forcedType = MediaType.MOVIE)
        }
        results.ifEmpty { fallbackList(MediaType.MOVIE) }.shuffled()
    }.getOrElse { fallbackList(MediaType.MOVIE).shuffled() }

    suspend fun popularTv(): List<MediaItem> = runCatching {
        val page = Random.nextInt(1, 6)
        val results = api.popularTv(page = page).results.mapNotNull {
            it.toMediaItem(forcedType = MediaType.TV)
        }
        results.ifEmpty { fallbackList(MediaType.TV) }.shuffled()
    }.getOrElse { fallbackList(MediaType.TV).shuffled() }

    suspend fun detail(id: Int, type: MediaType): MediaDetail? = runCatching {
        val data = if (type == MediaType.MOVIE) api.movieDetail(id) else api.tvDetail(id)
        data.toMediaDetail(id, type)
    }.getOrNull()

    // -------------------- mapper --------------------

    private fun TmdbResult.toMediaItem(forcedType: MediaType? = null): MediaItem? {
        val type = forcedType ?: when (mediaType) {
            "movie" -> MediaType.MOVIE
            "tv" -> MediaType.TV
            else -> return null
        }
        val titleText = title ?: name ?: return null
        val dateStr = releaseDate ?: firstAirDate
        return MediaItem(
            id = id,
            title = titleText,
            type = type,
            year = dateStr?.take(4)?.ifBlank { "2026" } ?: "2026",
            rating = voteAverage?.let { "%.1f".format(it) } ?: "8.5",
            poster = posterPath?.let { "$TMDB_IMG$it" } ?: FALLBACK_POSTER,
            backdrop = backdropPath?.let { "$TMDB_IMG_ORIGINAL$it" },
            overview = overview ?: "Film pilihan penonton.",
        )
    }

    private fun TmdbDetailResponse.toMediaDetail(id: Int, type: MediaType): MediaDetail {
        val titleText = title ?: name ?: "Film Populer"
        val dateStr = releaseDate ?: firstAirDate ?: "2026"
        val year = dateStr.take(4).ifBlank { "2026" }
        val runtimeText = if (type == MediaType.MOVIE) {
            runtime?.let { "$it min" } ?: "120 min"
        } else {
            episodeRunTime?.firstOrNull()?.let { "$it min/eps" } ?: "45 min/eps"
        }
        val genreNames = genres?.map { it.name }?.ifEmpty { null }
            ?: listOf("Action", "Drama", "Petualangan")
        val countryCode = productionCountries?.firstOrNull()?.iso
            ?: originCountry?.firstOrNull() ?: "US"
        val countryLabel = when (countryCode) {
            "ID" -> "Indonesia"
            "KR" -> "Korea"
            "JP" -> "Jepang"
            else -> "Barat"
        }
        val seasonsDetail = if (type == MediaType.TV) {
            seasons.orEmpty()
                .filter { it.seasonNumber > 0 }
                .map { SeasonInfo(it.seasonNumber, it.name ?: "Season ${it.seasonNumber}", it.episodeCount ?: 10) }
        } else emptyList()

        return MediaDetail(
            id = id,
            title = titleText,
            type = type,
            year = year,
            rating = voteAverage?.let { "%.1f".format(it) } ?: "8.7",
            runtime = runtimeText,
            genres = genreNames,
            country = countryLabel,
            overview = overview ?: "Film spektakuler dengan alur cerita mendalam dan efek visual mengagumkan.",
            poster = posterPath?.let { "$TMDB_IMG$it" } ?: FALLBACK_POSTER,
            backdrop = backdropPath?.let { "$TMDB_IMG_ORIGINAL$it" } ?: FALLBACK_BACKDROP,
            seasons = if (type == MediaType.TV) (numberOfSeasons ?: seasonsDetail.size.coerceAtLeast(1)) else 0,
            seasonsDetail = seasonsDetail,
        )
    }

    /** Daftar cadangan kalau TMDB gagal diakses sama sekali (offline/limit). */
    private fun fallbackList(onlyType: MediaType? = null): List<MediaItem> {
        val list = listOf(
            MediaItem(653346, "Kingdom of Planet Apes", MediaType.MOVIE, "2026", "8.7", "https://image.tmdb.org/t/p/w500/gKkl37BQuKTanygYQG1pyYgLVgf.jpg"),
            MediaItem(823464, "Godzilla x Kong", MediaType.MOVIE, "2026", "8.9", "https://image.tmdb.org/t/p/w500/b0PlSFdDwbyK0cfOiBZaOfHQfeK.jpg"),
            MediaItem(573435, "Bad Boys: Ride or Die", MediaType.MOVIE, "2026", "8.4", "https://image.tmdb.org/t/p/w500/nP6RliHjxH2uUjYqMZioHovLgvu.jpg"),
            MediaItem(1022789, "Inside Out 2", MediaType.MOVIE, "2026", "9.0", "https://image.tmdb.org/t/p/w500/vpnP19zLqVGlOx1VoY8YeeOi9W5.jpg"),
            MediaItem(533535, "Deadpool & Wolverine", MediaType.MOVIE, "2026", "9.1", "https://image.tmdb.org/t/p/w500/8cdWjvZQUExUUTzyp4t6EDMubfO.jpg"),
            MediaItem(693134, "Dune: Part Two", MediaType.MOVIE, "2026", "8.8", "https://image.tmdb.org/t/p/w500/1pdfLPoLMag8St8faOhvNUj9GlL.jpg"),
            MediaItem(872585, "Oppenheimer", MediaType.MOVIE, "2025", "8.9", "https://image.tmdb.org/t/p/w500/8Gxv8gSFCU0XGDykEGvC2t78dG.jpg"),
            MediaItem(933260, "The Substance", MediaType.MOVIE, "2026", "8.6", "https://image.tmdb.org/t/p/w500/l117yeUdMGRIFF3QvY6fTsjKZYx.jpg"),
            MediaItem(1184918, "The Wild Robot", MediaType.MOVIE, "2026", "8.7", "https://image.tmdb.org/t/p/w500/v9L21IioP1uY9R91456uT1k31u4.jpg"),
            MediaItem(912649, "Venom: The Last Dance", MediaType.MOVIE, "2026", "8.3", "https://image.tmdb.org/t/p/w500/k221nm0wDTHSTm2wqLQ8rLg2WvF.jpg"),
            MediaItem(93405, "Squid Game", MediaType.TV, "2026", "8.9", "https://image.tmdb.org/t/p/w500/dDlEmu3EZ0Pgg93K2SVNen3G82L.jpg"),
            MediaItem(94605, "Arcane", MediaType.TV, "2026", "9.2", "https://image.tmdb.org/t/p/w500/fqld2yobYU2FODohw4A42uLIwwh.jpg"),
            MediaItem(126308, "Shogun", MediaType.TV, "2026", "8.8", "https://image.tmdb.org/t/p/w500/7O4iVf26YScHaWFL9wFiYmBxMiK.jpg"),
            MediaItem(1396, "Breaking Bad", MediaType.TV, "2024", "9.5", "https://image.tmdb.org/t/p/w500/ztSlKpyE2zL4YLxmL2oB207iSpP.jpg"),
            MediaItem(92830, "Demon Slayer", MediaType.TV, "2026", "8.9", "https://image.tmdb.org/t/p/w500/xUfVKlMSpfasLdF1j28BwPlbStb.jpg"),
            MediaItem(114479, "Agatha All Along", MediaType.TV, "2026", "8.4", "https://image.tmdb.org/t/p/w500/p487LllQ25nNf7oGZzM2vLp1PjS.jpg"),
        )
        if (onlyType == null) return list
        val filtered = list.filter { it.type == onlyType }
        return filtered.ifEmpty { list }
    }
}
