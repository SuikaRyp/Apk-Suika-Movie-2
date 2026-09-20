package com.suikamovie.app.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.suikamovie.app.data.model.toMediaItem
import com.suikamovie.app.ui.components.MediaCard
import com.suikamovie.app.ui.theme.BgBody
import com.suikamovie.app.ui.theme.TextLight
import com.suikamovie.app.ui.theme.TextMain

@Composable
fun HistoryScreen(onOpenDetail: (id: Int, type: String) -> Unit) {
    val viewModel: HistoryViewModel = viewModel()
    val history by viewModel.watchHistory.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize().background(BgBody).padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Riwayat Nonton", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            if (history.isNotEmpty()) {
                IconButton(onClick = { viewModel.clearHistory() }) {
                    Icon(Icons.Filled.DeleteSweep, contentDescription = "Hapus Semua", tint = TextLight)
                }
            }
        }

        if (history.isEmpty()) {
            Text(
                "Belum ada riwayat nonton. Yuk mulai nonton film!",
                color = TextLight,
                modifier = Modifier.align(Alignment.Center),
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 52.dp, bottom = 24.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(history, key = { "${it.type.apiValue}-${it.id}" }) { entry ->
                    MediaCard(
                        item = entry.toMediaItem(),
                        modifier = Modifier.animateItem(),
                        fillWidth = true,
                        onClick = { onOpenDetail(entry.id, entry.type.apiValue) },
                    )
                }
            }
        }
    }
}
