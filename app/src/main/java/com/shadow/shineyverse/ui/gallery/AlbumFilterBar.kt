package com.shadow.shineyverse.ui.gallery

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shadow.shineyverse.data.remote.dto.FolderDto

@Composable
fun AlbumFilterBar(
    folders: List<FolderDto>,
    selectedFolder: String,
    onSelect: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(selected = selectedFolder.isEmpty(), onClick = { onSelect("") }, label = { Text("All") })
        folders.forEach { folder ->
            FilterChip(
                selected = selectedFolder == folder.name,
                onClick = { onSelect(folder.name) },
                label = { Text("${folder.name} (${folder.count})") },
            )
        }
    }
}
