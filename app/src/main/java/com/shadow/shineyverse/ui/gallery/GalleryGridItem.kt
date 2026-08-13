package com.shadow.shineyverse.ui.gallery

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.shadow.shineyverse.data.remote.dto.GalleryItemDto
import com.shadow.shineyverse.data.remote.toAbsoluteMediaUrl

@Composable
fun GalleryGridItem(
    item: GalleryItemDto,
    onClick: () -> Unit,
    onMove: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var loadError by remember(item.url) { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
    ) {
        if (item.kind == "video") {
            Icon(
                Icons.Filled.PlayCircle,
                contentDescription = "Video",
                tint = Color.White,
                modifier = Modifier.align(Alignment.Center).fillMaxSize(0.35f),
            )
        } else {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(item.url.toAbsoluteMediaUrl())
                    .listener(
                        onError = { _, result ->
                            val message = result.throwable.message ?: result.throwable.toString()
                            Log.e("GalleryImage", "Failed to load ${item.url.toAbsoluteMediaUrl()}: $message", result.throwable)
                            loadError = message
                        },
                        onSuccess = { _, _ -> loadError = null },
                    )
                    .build(),
                contentDescription = item.prompt ?: item.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            loadError?.let { message ->
                Text(
                    message,
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(4.dp),
                )
            }
        }

        IconButton(onClick = { menuExpanded = true }, modifier = Modifier.align(Alignment.TopEnd)) {
            Icon(Icons.Filled.MoreVert, contentDescription = "Actions", tint = Color.White)
        }
        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
            DropdownMenuItem(text = { Text("Move to album") }, onClick = { menuExpanded = false; onMove() })
            DropdownMenuItem(text = { Text("Copy to album") }, onClick = { menuExpanded = false; onCopy() })
            DropdownMenuItem(text = { Text("Delete") }, onClick = { menuExpanded = false; onDelete() })
        }
    }
}
