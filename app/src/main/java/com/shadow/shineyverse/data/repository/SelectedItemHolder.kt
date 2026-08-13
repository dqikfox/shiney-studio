package com.shadow.shineyverse.data.repository

import com.shadow.shineyverse.data.remote.dto.GalleryItemDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Hands the tapped gallery item off to the item-detail screen without a
 * redundant network refetch or complex route-argument encoding - the same
 * pattern as [RemixStateHolder], just one-directional (Gallery -> detail).
 */
@Singleton
class SelectedItemHolder @Inject constructor() {
    private val _selected = MutableStateFlow<GalleryItemDto?>(null)
    val selected: StateFlow<GalleryItemDto?> = _selected

    fun set(item: GalleryItemDto) {
        _selected.value = item
    }
}
