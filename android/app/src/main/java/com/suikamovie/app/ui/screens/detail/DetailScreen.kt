package com.suikamovie.app.ui.screens.detail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.suikamovie.app.MainActivity
import com.suikamovie.app.data.model.MediaType
import com.suikamovie.app.data.remote.STREAM_SERVERS
import com.suikamovie.app.ui.theme.BgBody
import com.suikamovie.app.ui.theme.BgCard
import com.suikamovie.app.ui.theme.BgSurface
import com.suikamovie.app.ui.theme.PrimaryBlue
import com.suikamovie.app.ui.theme.StarGold
import com.suikamovie.app.ui.theme.TextLight
import com.suikamovie.app.ui.theme.TextMain
import com.suikamovie.app.ui.theme.TextSub

@Composable
fun DetailScreen(
    id: Int,
    type: String,
    onBack: () -> Unit,
) {
    val viewModel: DetailViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val autoRotateEnabled by viewModel.autoRotateEnabled.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as? MainActivity

    LaunchedEffect(id, type) {
        viewModel.load(id, MediaType.from(type))
    }

    // Balik ke portrait & keluar fullscreen begitu layar ini ditinggalin,
    // biar nggak nyangkut landscape pas pindah ke layar lain.
    DisposableEffect(Unit) {
        onDispose { activity?.unlockOrientation() }
    }

    // Tombol back Android: kalau lagi fullscreen, keluar fullscreen dulu
    // (bukan langsung balik ke layar sebelumnya).
    BackHandler(enabled = uiState.isFullscreen) {
        viewModel.setFullscreen(false)
        activity?.unlockOrientation()
    }

    if (uiState.isFullscreen) {
        val url = viewModel.currentStreamUrl()
        Box(modifier = Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color.Black)) {
            if (url != null) {
                PlayerWebView(url = url)
            }
            IconButton(
                onClick = {
                    viewModel.setFullscreen(false)
                    activity?.unlockOrientation()
                },
                modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
            ) {
                Icon(Icons.Filled.FullscreenExit, contentDescription = "Keluar Layar Penuh", tint = TextMain)
            }
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize().background(BgBody)) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(color = PrimaryBlue, modifier = Modifier.align(Alignment.Center))
            }
            uiState.errorMessage != null -> {
                Text(uiState.errorMessage ?: "", color = TextLight, modifier = Modifier.align(Alignment.Center))
            }
            uiState.detail != null -> {
                val detail = uiState.detail!!
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(230.dp)) {
                            if (uiState.isPlaying) {
                                val url = viewModel.currentStreamUrl()
                                if (url != null) PlayerWebView(url = url)

                                IconButton(
                                    onClick = {
                                        viewModel.setFullscreen(true)
                                        if (autoRotateEnabled) activity?.lockLandscape()
                                    },
                                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                                ) {
                                    Icon(Icons.Filled.Fullscreen, contentDescription = "Layar Penuh", tint = TextMain)
                                }
                            } else {
                                AsyncImage(
                                    model = detail.backdrop,
                                    contentDescription = detail.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.25f))
                                        .clickable { viewModel.startPlaying() },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(CircleShape)
                                            .background(PrimaryBlue),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(Icons.Filled.PlayArrow, contentDescription = "Putar", tint = TextMain, modifier = Modifier.size(32.dp))
                                    }
                                }
                            }

                            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.TopStart).padding(8.dp)) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali", tint = TextMain)
                            }
                        }
                    }

                    item {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(detail.title, color = TextMain, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, modifier = Modifier.weight(1f))
                                IconButton(onClick = { viewModel.toggleFavorite() }) {
                                    Icon(
                                        if (uiState.isFavorite) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                        contentDescription = "Daftar Saya",
                                        tint = PrimaryBlue,
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
                                Icon(Icons.Filled.Star, contentDescription = null, tint = StarGold, modifier = Modifier.size(15.dp))
                                Text(" ${detail.rating}", color = TextMain, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("  •  ${detail.year}  •  ${detail.runtime}  •  ${detail.country}", color = TextSub, fontSize = 12.sp)
                            }

                            Row(
                                modifier = Modifier.padding(top = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                detail.genres.take(4).forEach { genre ->
                                    AssistChip(
                                        onClick = {},
                                        label = { Text(genre, fontSize = 11.sp) },
                                        colors = AssistChipDefaults.assistChipColors(containerColor = BgSurface, labelColor = TextSub),
                                    )
                                }
                            }

                            Text(
                                detail.overview,
                                color = TextSub,
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                                modifier = Modifier.padding(top = 14.dp),
                            )

                            if (!uiState.isPlaying) {
                                Button(
                                    onClick = { viewModel.startPlaying() },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp).height(48.dp),
                                    shape = RoundedCornerShape(14.dp),
                                ) {
                                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                                    Text(" Putar Sekarang", fontWeight = FontWeight.SemiBold)
                                }
                            }

                            if (detail.type == MediaType.TV && detail.seasonsDetail.isNotEmpty()) {
                                Text("Season", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(top = 18.dp, bottom = 8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    detail.seasonsDetail.forEach { season ->
                                        FilterChip(
                                            selected = uiState.activeSeason == season.seasonNumber,
                                            onClick = { viewModel.selectSeasonEpisode(season.seasonNumber, 1) },
                                            label = { Text(season.name, fontSize = 11.sp) },
                                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryBlue),
                                        )
                                    }
                                }
                                Text("Episode", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(top = 14.dp, bottom = 8.dp))
                                val episodeCount = detail.seasonsDetail.firstOrNull { it.seasonNumber == uiState.activeSeason }?.episodeCount ?: 10
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items((1..episodeCount).toList()) { ep ->
                                        FilterChip(
                                            selected = uiState.activeEpisode == ep,
                                            onClick = { viewModel.selectSeasonEpisode(uiState.activeSeason, ep) },
                                            label = { Text("Eps $ep", fontSize = 11.sp) },
                                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryBlue),
                                        )
                                    }
                                }
                            }

                            Text("Pilih Server", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(top = 18.dp, bottom = 8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(STREAM_SERVERS.indices.toList()) { index ->
                                    val server = STREAM_SERVERS[index]
                                    FilterChip(
                                        selected = uiState.activeServerIndex == index,
                                        onClick = { viewModel.selectServer(index) },
                                        label = { Text(server.label, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryBlue),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
