package com.suikamovie.app.data.remote

import com.suikamovie.app.data.model.MediaType

/**
 * Server embed streaming pihak ketiga - dipindah 1:1 dari SERVERS di
 * public/app.js. Ini alasan kenapa layar player masih butuh WebView: server-
 * server ini adalah halaman web lengkap (iframe player), bukan link file
 * video langsung yang bisa diputer native player.
 */
data class StreamServer(
    val id: String,
    val label: String,
    val movieTemplate: String,
    val tvTemplate: String,
)

val STREAM_SERVERS = listOf(
    StreamServer(
        id = "vidlink",
        label = "SERVER 1 (VIP)",
        movieTemplate = "https://vidlink.pro/movie/{id}",
        tvTemplate = "https://vidlink.pro/tv/{id}/{s}/{e}",
    ),
    StreamServer(
        id = "vidsrc",
        label = "SERVER 2 (FAST)",
        movieTemplate = "https://vidsrc.cc/v2/embed/movie/{id}",
        tvTemplate = "https://vidsrc.cc/v2/embed/tv/{id}/{s}/{e}",
    ),
    StreamServer(
        id = "embedsu",
        label = "SERVER 3 (HD)",
        movieTemplate = "https://embed.su/embed/movie/{id}",
        tvTemplate = "https://embed.su/embed/tv/{id}/{s}/{e}",
    ),
    StreamServer(
        id = "vidsrcpro",
        label = "SERVER 4",
        movieTemplate = "https://vidsrc.pro/embed/movie/{id}",
        tvTemplate = "https://vidsrc.pro/embed/tv/{id}/{s}/{e}",
    ),
    StreamServer(
        id = "autoembed",
        label = "SERVER 5",
        movieTemplate = "https://player.autoembed.cc/embed/movie/{id}",
        tvTemplate = "https://player.autoembed.cc/embed/tv/{id}/{s}/{e}",
    ),
    StreamServer(
        id = "superembed",
        label = "SERVER 6",
        movieTemplate = "https://multiembed.mov/directstream.php?video_id={id}&tmdb=1",
        tvTemplate = "https://multiembed.mov/directstream.php?video_id={id}&tmdb=1&s={s}&e={e}",
    ),
    StreamServer(
        id = "2embed",
        label = "SERVER 7",
        movieTemplate = "https://www.2embed.cc/embed/{id}",
        tvTemplate = "https://www.2embed.cc/embedtv/{id}&s={s}&e={e}",
    ),
)

fun StreamServer.buildUrl(id: Int, type: MediaType, season: Int = 1, episode: Int = 1): String {
    val template = if (type == MediaType.MOVIE) movieTemplate else tvTemplate
    return template
        .replace("{id}", id.toString())
        .replace("{s}", season.toString())
        .replace("{e}", episode.toString())
}
