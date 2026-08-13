package com.shadow.shineyverse.ui.gallery

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shadow.shineyverse.data.remote.dto.FolderDto

@Composable
fun FolderPickerDialog(
    title: String,
    folders: List<FolderDto>,
    onDismiss: () -> Unit,
    onPick: (String) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 320.dp).fillMaxWidth()) {
                item { TextButton(onClick = { onPick("") }) { Text("Unfiled") } }
                items(folders) { folder ->
                    TextButton(onClick = { onPick(folder.name) }) { Text("${folder.name} (${folder.count})") }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
