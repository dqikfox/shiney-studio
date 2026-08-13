package com.shadow.shineyverse.ui.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shadow.shineyverse.data.remote.dto.GalleryItemDto
import com.shadow.shineyverse.ui.albums.AlbumManagementSheet
import com.shadow.shineyverse.ui.common.ErrorBanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    onNavigateToDetail: () -> Unit,
    viewModel: GalleryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showAlbumSheet by remember { mutableStateOf(false) }
    var moveTarget by remember { mutableStateOf<GalleryItemDto?>(null) }
    var copyTarget by remember { mutableStateOf<GalleryItemDto?>(null) }

    val gridState = rememberLazyGridState()
    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = gridState.layoutInfo
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= layoutInfo.totalItemsCount - 6
        }
    }
    LaunchedEffect(shouldLoadMore, state.hasMore, state.isLoadingMore) {
        if (shouldLoadMore && state.hasMore && !state.isLoadingMore) {
            viewModel.loadMore()
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAlbumSheet = true }) {
                Icon(Icons.Filled.PhotoLibrary, contentDescription = "Manage albums")
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp, vertical = 12.dp)) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                label = { Text("Search by prompt, file name, or model") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            AlbumFilterBar(state.folders, state.selectedFolder, viewModel::selectFolder)

            state.error?.let { ErrorBanner(it) }

            if (state.isLoading) {
                com.shadow.shineyverse.ui.common.FullScreenLoading(modifier = Modifier.fillMaxSize())
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 140.dp),
                    state = gridState,
                    contentPadding = PaddingValues(vertical = 12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(state.filteredItems, key = { "${it.folder}/${it.name}" }) { item ->
                        GalleryGridItem(
                            item = item,
                            onClick = { viewModel.selectItem(item); onNavigateToDetail() },
                            onMove = { moveTarget = item },
                            onCopy = { copyTarget = item },
                            onDelete = { viewModel.deleteItem(item) },
                        )
                    }
                    if (state.isLoadingMore) {
                        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                        }
                    }
                }
            }
        }
    }

    if (showAlbumSheet) {
        AlbumManagementSheet(
            folders = state.folders,
            onDismiss = { showAlbumSheet = false },
            onCreate = viewModel::createFolder,
            onRename = viewModel::renameFolder,
            onDelete = viewModel::deleteFolder,
        )
    }

    moveTarget?.let { item ->
        FolderPickerDialog(
            title = "Move to...",
            folders = state.folders,
            onDismiss = { moveTarget = null },
            onPick = { folder -> viewModel.moveItem(item, folder); moveTarget = null },
        )
    }
    copyTarget?.let { item ->
        FolderPickerDialog(
            title = "Copy to...",
            folders = state.folders,
            onDismiss = { copyTarget = null },
            onPick = { folder -> viewModel.copyItem(item, folder); copyTarget = null },
        )
    }
}
