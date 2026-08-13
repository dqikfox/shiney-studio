package com.shadow.shineyverse.ui.itemdetail

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.shadow.shineyverse.data.remote.toAbsoluteMediaUrl
import com.shadow.shineyverse.ui.common.ErrorBanner
import com.shadow.shineyverse.ui.gallery.FolderPickerDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    onBack: () -> Unit,
    onRemixed: () -> Unit,
    viewModel: ItemDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    var showMoveDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(state.deleted) {
        if (state.deleted) onBack()
    }

    val item = state.item
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(item?.name.orEmpty(), maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        if (item == null) {
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
                Text("This item is no longer available.", style = MaterialTheme.typography.bodyMedium)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            state.error?.let { ErrorBanner(it) }

            if (item.kind == "video") {
                VideoPlayer(
                    url = item.url.toAbsoluteMediaUrl(),
                    dataSourceFactory = viewModel.mediaDataSourceFactory,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                )
            } else {
                AsyncImage(
                    model = coil.request.ImageRequest.Builder(LocalContext.current)
                        .data(item.url.toAbsoluteMediaUrl())
                        .listener(
                            onError = { _, result ->
                                android.util.Log.e(
                                    "ItemDetailImage",
                                    "Failed to load ${item.url.toAbsoluteMediaUrl()}: ${result.throwable}",
                                    result.throwable,
                                )
                            },
                        )
                        .build(),
                    contentDescription = item.prompt,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                )
            }

            item.prompt?.let { Text(it, style = MaterialTheme.typography.bodyLarge) }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                item.model?.let { Text("Model: $it", style = MaterialTheme.typography.bodyMedium) }
                item.seed?.let { Text("Seed: $it", style = MaterialTheme.typography.bodyMedium) }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    if (viewModel.remix()) onRemixed()
                }) { Text("Remix") }
                OutlinedButton(onClick = { showMoveDialog = true }) { Text("Move to album") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = {
                    coroutineScope.launch {
                        val url = viewModel.resolveExternalUrl(item.url)
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, url)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share"))
                    }
                }) { Text("Share link") }
                TextButton(onClick = { showDeleteConfirm = true }) { Text("Delete") }
            }
        }
    }

    if (showMoveDialog) {
        FolderPickerDialog(
            title = "Move to...",
            folders = state.folders,
            onDismiss = { showMoveDialog = false },
            onPick = { folder -> viewModel.moveTo(folder); showMoveDialog = false },
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete this item?") },
            text = { Text("It'll be moved to the trash - recoverable, not permanently erased.") },
            confirmButton = {
                TextButton(onClick = { viewModel.delete(); showDeleteConfirm = false }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun VideoPlayer(url: String, dataSourceFactory: OkHttpDataSource.Factory, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
            .build()
            .apply {
                setMediaItem(MediaItem.fromUri(url))
                prepare()
            }
    }
    LaunchedEffect(url) {
        exoPlayer.setMediaItem(MediaItem.fromUri(url))
        exoPlayer.prepare()
    }
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }
    AndroidView(
        factory = { PlayerView(it).apply { player = exoPlayer } },
        modifier = modifier,
    )
}
