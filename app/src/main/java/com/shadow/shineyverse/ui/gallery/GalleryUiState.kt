package com.shadow.shineyverse.ui.gallery

import com.shadow.shineyverse.data.remote.dto.FolderDto
import com.shadow.shineyverse.data.remote.dto.GalleryItemDto

data class GalleryUiState(
    val items: List<GalleryItemDto> = emptyList(),
    val folders: List<FolderDto> = emptyList(),
    val selectedFolder: String = "", // "" = unfiled/all view, matching the server's own semantics
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val error: String? = null,
) {
    val filteredItems: List<GalleryItemDto>
        get() {
            val query = searchQuery.trim().lowercase()
            if (query.isEmpty()) return items
            return items.filter { item ->
                item.name.lowercase().contains(query) ||
                    item.prompt?.lowercase()?.contains(query) == true ||
                    item.model?.lowercase()?.contains(query) == true
            }
        }
}
