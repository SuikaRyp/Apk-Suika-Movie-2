package com.suikamovie.app.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.suikamovie.app.data.model.MediaItem
import com.suikamovie.app.ui.components.MediaCard
import com.suikamovie.app.ui.theme.BgBody
import com.suikamovie.app.ui.theme.BgCard
import com.suikamovie.app.ui.theme.PrimaryBlue
import com.suikamovie.app.ui.theme.TextLight
import com.suikamovie.app.ui.theme.TextMain
import kotlinx.coroutines.delay
import kotlin.math.abs

@Composable
fun HomeScreen(
    onOpenDetail: (id: Int, type: String) -> Unit,
    onOpenSearch: () -> Unit,
) {
    val viewModel: HomeViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // Search bar cuma "muncul" abis user scroll ngelewatin hero banner
    // (item index 0) - port dari behavior IntersectionObserver di web dulu.
    val searchRevealed by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 }
    }

    Column(modifier = Modifier.fillMaxSize().background(BgBody)) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BgCard)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(brandAnnotatedText(), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)

            AnimatedVisibility(
                visible = searchRevealed,
                enter = fadeIn(tween(220)) + expandHorizontally(tween(280)),
                exit = fadeOut(tween(160)) + shrinkHorizontally(tween(220)),
            ) {
                Row(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .height(36.dp)
                        .background(BgBody, RoundedCornerShape(50))
                        .clickable(onClick = onOpenSearch)
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = TextLight, modifier = Modifier.size(16.dp))
                    Text("Cari film...", color = TextLight, fontSize = 13.sp, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }

        Crossfade(targetState = uiState.isLoading, label = "home-loading-crossfade") { loading ->
            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(bottom = 24.dp),
                ) {
                    item { HeroBanner(items = uiState.heroItems, onClick = { onOpenDetail(it.id, it.type.apiValue) }) }

                    item {
                        CategoryRow(
                            title = "Top Rekomendasi Minggu Ini",
                            items = uiState.trending,
                            onOpenDetail = onOpenDetail,
                        )
                    }
                    item {
                        CategoryRow(
                            title = "Film Populer",
                            items = uiState.popularMovies,
                            onOpenDetail = onOpenDetail,
                        )
                    }
                    item {
                        CategoryRow(
                            title = "Series Populer",
                            items = uiState.popularTv,
                            onOpenDetail = onOpenDetail,
                        )
                    }
                }
            }
        }
    }
}

private fun brandAnnotatedText() = buildAnnotatedString {
    withStyle(SpanStyle(color = TextMain)) { append("SUIKA") }
    withStyle(SpanStyle(color = PrimaryBlue)) { append("MOVIE") }
}

@Composable
private fun HeroBanner(items: List<MediaItem>, onClick: (MediaItem) -> Unit) {
    if (items.isEmpty()) return
    var index by remember { mutableIntStateOf(0) }

    LaunchedEffect(items.size) {
        while (true) {
            delay(5000)
            index = (index + 1) % items.size
        }
    }

    val item = items[index % items.size]

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clickable { onClick(item) },
    ) {
        // Crossfade halus antar banner (ganti dari langsung "loncat" jadi fade
        // pelan-pelan) - ini yang bikin rotasi hero-nya kerasa mulus & "mahal".
        Crossfade(targetState = item, animationSpec = tween(700), label = "hero-crossfade") { current ->
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = current.backdrop ?: current.poster,
                    contentDescription = current.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(colors = listOf(Color.Transparent, BgBody)))
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
                ) {
                    Text("TOP REKOMENDASI MINGGU INI", color = PrimaryBlue, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Text(current.title, color = TextMain, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, maxLines = 2)
                }
            }
        }

        // Indikator titik - biar ada feedback visual banner ke berapa dari total.
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            items.indices.forEach { i ->
                val active = i == index % items.size
                val dotAlpha by animateFloatAsState(if (active) 1f else 0.35f, label = "hero-dot")
                Box(
                    modifier = Modifier
                        .size(if (active) 7.dp else 5.dp)
                        .alpha(dotAlpha)
                        .background(Color.White, RoundedCornerShape(50))
                )
            }
        }
    }
}

@Composable
private fun CategoryRow(title: String, items: List<MediaItem>, onOpenDetail: (Int, String) -> Unit) {
    if (items.isEmpty()) return
    val rowState = rememberLazyListState()

    Column(modifier = Modifier.padding(top = 20.dp)) {
        Text(
            title,
            color = TextMain,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        LazyRow(
            state = rowState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(items, key = { "${it.type.apiValue}-${it.id}" }) { media ->
                val index = items.indexOf(media)
                MediaCard(
                    item = media,
                    modifier = Modifier
                        .animateItem()
                        .scaleOnScroll(rowState, index),
                    onClick = { onOpenDetail(media.id, media.type.apiValue) },
                )
            }
        }
    }
}

/**
 * Efek "scroll carousel" ala Wibuku: card yang lagi di tengah viewport
 * horizontal tampil penuh (scale 1x, alpha 1x), makin ke pinggir makin
 * ngecil & pudar dikit. Baca posisi lewat graphicsLayer (fase draw), jadi
 * ringan - nggak micu recomposition tiap scroll, cuma re-draw.
 */
private fun Modifier.scaleOnScroll(listState: androidx.compose.foundation.lazy.LazyListState, index: Int): Modifier =
    this.graphicsLayer {
        val layoutInfo = listState.layoutInfo
        val itemInfo = layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }
        if (itemInfo != null) {
            val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2f
            val itemCenter = itemInfo.offset + itemInfo.size / 2f
            val viewportSpan = (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset).toFloat().coerceAtLeast(1f)
            val distanceFraction = (abs(viewportCenter - itemCenter) / viewportSpan).coerceIn(0f, 1f)
            val scale = 1f - (distanceFraction * 0.12f)
            scaleX = scale
            scaleY = scale
            alpha = 1f - (distanceFraction * 0.35f)
        }
    }
