package com.suikamovie.app.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// ---- Konstanta TMDB (sama persis kayak yang dulu ada di public/app.js) ----
const val TMDB_API_KEY = "82524e2faef91706a2d52d52496130ac"
const val TMDB_BASE_URL = "https://api.themoviedb.org/3/"
const val TMDB_IMG = "https://image.tmdb.org/t/p/w500"
const val TMDB_IMG_ORIGINAL = "https://image.tmdb.org/t/p/original"

interface TmdbApi {

    @GET("search/multi")
    suspend fun searchMulti(
        @Query("api_key") apiKey: String = TMDB_API_KEY,
        @Query("language") language: String = "id-ID",
        @Query("query") query: String,
        @Query("page") page: Int = 1,
    ): TmdbSearchResponse

    @GET("trending/all/week")
    suspend fun trendingAllWeek(
        @Query("api_key") apiKey: String = TMDB_API_KEY,
        @Query("language") language: String = "id-ID",
        @Query("page") page: Int,
    ): TmdbSearchResponse

    @GET("movie/popular")
    suspend fun popularMovies(
        @Query("api_key") apiKey: String = TMDB_API_KEY,
        @Query("language") language: String = "id-ID",
        @Query("page") page: Int,
    ): TmdbSearchResponse

    @GET("tv/popular")
    suspend fun popularTv(
        @Query("api_key") apiKey: String = TMDB_API_KEY,
        @Query("language") language: String = "id-ID",
        @Query("page") page: Int,
    ): TmdbSearchResponse

    @GET("movie/{id}")
    suspend fun movieDetail(
        @Path("id") id: Int,
        @Query("api_key") apiKey: String = TMDB_API_KEY,
        @Query("language") language: String = "id-ID",
    ): TmdbDetailResponse

    @GET("tv/{id}")
    suspend fun tvDetail(
        @Path("id") id: Int,
        @Query("api_key") apiKey: String = TMDB_API_KEY,
        @Query("language") language: String = "id-ID",
    ): TmdbDetailResponse

    @GET("tv/{id}/season/{season}")
    suspend fun tvSeasonDetail(
        @Path("id") id: Int,
        @Path("season") season: Int,
        @Query("api_key") apiKey: String = TMDB_API_KEY,
        @Query("language") language: String = "id-ID",
    ): TmdbSeasonDetailResponse
}

// ==========================================================================
// DTO mentah dari response JSON TMDB - nama field disamain sama JSON aslinya
// pake @SerializedName biar Gson bisa langsung parse otomatis.
// ==========================================================================

data class TmdbSearchResponse(
    val page: Int = 1,
    val results: List<TmdbResult> = emptyList(),
)

data class TmdbResult(
    val id: Int,
    val title: String? = null,
    val name: String? = null,
    @com.google.gson.annotations.SerializedName("media_type") val mediaType: String? = null,
    @com.google.gson.annotations.SerializedName("release_date") val releaseDate: String? = null,
    @com.google.gson.annotations.SerializedName("first_air_date") val firstAirDate: String? = null,
    @com.google.gson.annotations.SerializedName("vote_average") val voteAverage: Double? = null,
    @com.google.gson.annotations.SerializedName("poster_path") val posterPath: String? = null,
    @com.google.gson.annotations.SerializedName("backdrop_path") val backdropPath: String? = null,
    val overview: String? = null,
    @com.google.gson.annotations.SerializedName("origin_country") val originCountry: List<String>? = null,
    @com.google.gson.annotations.SerializedName("original_language") val originalLanguage: String? = null,
)

data class TmdbDetailResponse(
    val id: Int,
    val title: String? = null,
    val name: String? = null,
    @com.google.gson.annotations.SerializedName("release_date") val releaseDate: String? = null,
    @com.google.gson.annotations.SerializedName("first_air_date") val firstAirDate: String? = null,
    val runtime: Int? = null,
    @com.google.gson.annotations.SerializedName("episode_run_time") val episodeRunTime: List<Int>? = null,
    val genres: List<TmdbGenre>? = null,
    @com.google.gson.annotations.SerializedName("production_countries") val productionCountries: List<TmdbCountry>? = null,
    @com.google.gson.annotations.SerializedName("origin_country") val originCountry: List<String>? = null,
    @com.google.gson.annotations.SerializedName("vote_average") val voteAverage: Double? = null,
    val overview: String? = null,
    @com.google.gson.annotations.SerializedName("poster_path") val posterPath: String? = null,
    @com.google.gson.annotations.SerializedName("backdrop_path") val backdropPath: String? = null,
    @com.google.gson.annotations.SerializedName("number_of_seasons") val numberOfSeasons: Int? = null,
    val seasons: List<TmdbSeason>? = null,
)

data class TmdbGenre(val id: Int, val name: String)
data class TmdbCountry(@com.google.gson.annotations.SerializedName("iso_3166_1") val iso: String)

data class TmdbSeason(
    @com.google.gson.annotations.SerializedName("season_number") val seasonNumber: Int,
    val name: String? = null,
    @com.google.gson.annotations.SerializedName("episode_count") val episodeCount: Int? = null,
)

data class TmdbSeasonDetailResponse(
    @com.google.gson.annotations.SerializedName("season_number") val seasonNumber: Int,
    val episodes: List<TmdbEpisode> = emptyList(),
)

data class TmdbEpisode(
    @com.google.gson.annotations.SerializedName("episode_number") val episodeNumber: Int,
    val name: String? = null,
    val overview: String? = null,
    @com.google.gson.annotations.SerializedName("still_path") val stillPath: String? = null,
)
