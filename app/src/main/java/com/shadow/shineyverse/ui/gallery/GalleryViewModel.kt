package com.shadow.shineyverse.ui.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shadow.shineyverse.data.remote.ApiResult
import com.shadow.shineyverse.data.remote.dto.GalleryItemDto
import com.shadow.shineyverse.data.remote.dto.MutationResultDto
import com.shadow.shineyverse.data.remote.errorMessage
import com.shadow.shineyverse.data.repository.GalleryRepository
import com.shadow.shineyverse.data.repository.SelectedItemHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val PAGE_SIZE = 60

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val galleryRepository: GalleryRepository,
    private val selectedItemHolder: SelectedItemHolder,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GalleryUiState())
    val uiState: StateFlow<GalleryUiState> = _uiState

    init {
        refreshAll()
    }

    fun refreshAll() {
        loadFolders()
        loadGallery(reset = true)
    }

    private fun loadFolders() {
        viewModelScope.launch {
            val result = galleryRepository.fetchFolders()
            if (result is ApiResult.Success) {
                _uiState.value = _uiState.value.copy(folders = result.data)
            }
        }
    }

    fun selectFolder(folder: String) {
        if (_uiState.value.selectedFolder == folder) return
        _uiState.value = _uiState.value.copy(selectedFolder = folder)
        loadGallery(reset = true)
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun loadMore() {
        loadGallery(reset = false)
    }

    private fun loadGallery(reset: Boolean) {
        val current = _uiState.value
        if (!reset && (!current.hasMore || current.isLoadingMore)) return
        val offset = if (reset) 0 else current.items.size

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = reset,
                isLoadingMore = !reset,
                error = null,
            )
            when (val result = galleryRepository.fetchGallery(current.selectedFolder, PAGE_SIZE, offset)) {
                is ApiResult.Success -> {
                    val body = result.data
                    val newItems = if (reset) body.items else _uiState.value.items + body.items
                    _uiState.value = _uiState.value.copy(
                        items = newItems,
                        isLoading = false,
                        isLoadingMore = false,
                        hasMore = newItems.size < body.total,
                    )
                }
                else -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    error = "Couldn't load the gallery - check your connection.",
                )
            }
        }
    }

    fun selectItem(item: GalleryItemDto) {
        selectedItemHolder.set(item)
    }

    fun createFolder(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            handleMutation(galleryRepository.createFolder(name)) { refreshAll() }
        }
    }

    fun renameFolder(oldName: String, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            handleMutation(galleryRepository.renameFolder(oldName, newName)) { refreshAll() }
        }
    }

    fun deleteFolder(name: String) {
        viewModelScope.launch {
            handleMutation(galleryRepository.deleteFolder(name)) {
                if (_uiState.value.selectedFolder == name) {
                    _uiState.value = _uiState.value.copy(selectedFolder = "")
                }
                refreshAll()
            }
        }
    }

    fun moveItem(item: GalleryItemDto, targetFolder: String) {
        viewModelScope.launch {
            handleMutation(galleryRepository.moveItem(item.name, item.kind, targetFolder)) { refreshAll() }
        }
    }

    fun copyItem(item: GalleryItemDto, targetFolder: String) {
        viewModelScope.launch {
            handleMutation(galleryRepository.copyItem(item.name, item.kind, targetFolder)) { refreshAll() }
        }
    }

    fun deleteItem(item: GalleryItemDto) {
        viewModelScope.launch {
            handleMutation(galleryRepository.deleteItem(item.name, item.kind)) { refreshAll() }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun handleMutation(result: ApiResult<MutationResultDto>, onOk: () -> Unit) {
        when (result) {
            is ApiResult.Success -> {
                if (result.data.ok) {
                    onOk()
                } else {
                    _uiState.value = _uiState.value.copy(error = result.data.error ?: "That didn't work.")
                }
            }
            else -> _uiState.value = _uiState.value.copy(error = result.errorMessage())
        }
    }
}
