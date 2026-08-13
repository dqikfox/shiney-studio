package com.shadow.shineyverse.ui.albums

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shadow.shineyverse.data.remote.dto.FolderDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumManagementSheet(
    folders: List<FolderDto>,
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit,
    onRename: (String, String) -> Unit,
    onDelete: (String) -> Unit,
) {
    var newName by remember { mutableStateOf("") }
    var renameTarget by remember { mutableStateOf<String?>(null) }
    var renameValue by remember { mutableStateOf("") }
    var deleteTarget by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Albums", style = MaterialTheme.typography.titleLarge)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("New album name") },
                    modifier = Modifier.weight(1f),
                )
                Button(onClick = { onCreate(newName); newName = "" }, enabled = newName.isNotBlank()) {
                    Text("Create")
                }
            }

            LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                items(folders) { folder ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    ) {
                        Text("${folder.name} (${folder.count})", modifier = Modifier.weight(1f))
                        IconButton(onClick = { renameTarget = folder.name; renameValue = folder.name }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Rename")
                        }
                        IconButton(onClick = { deleteTarget = folder.name }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete")
                        }
                    }
                }
            }
        }
    }

    renameTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text("Rename album") },
            text = {
                OutlinedTextField(value = renameValue, onValueChange = { renameValue = it }, label = { Text("New name") })
            },
            confirmButton = {
                TextButton(onClick = { onRename(target, renameValue); renameTarget = null }) { Text("Rename") }
            },
            dismissButton = { TextButton(onClick = { renameTarget = null }) { Text("Cancel") } },
        )
    }

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete album?") },
            text = { Text("\"$target\" and everything in it will be deleted. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = { onDelete(target); deleteTarget = null }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Cancel") } },
        )
    }
}
