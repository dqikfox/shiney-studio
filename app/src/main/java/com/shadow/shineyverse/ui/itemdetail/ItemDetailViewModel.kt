package com.shadow.shineyverse.ui.itemdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shadow.shineyverse.data.remote.ApiResult
import com.shadow.shineyverse.data.remote.dto.FolderDto
import com.shadow.shineyverse.data.remote.dto.GalleryItemDto
import com.shadow.shineyverse.data.remote.errorMessage
import com.shadow.shineyverse.data.repository.GalleryRepository
import com.shadow.shineyverse.data.repository.RemixRequest
import com.shadow.shineyverse.data.repository.RemixStateHolder
import com.shadow.shineyverse.data.repository.SelectedItemHolder
import com.shadow.shineyverse.data.repository.SettingsRepository
import androidx.media3.datasource.okhttp.OkHttpDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ItemDetailUiState(
    val item: GalleryItemDto? = null,
    val folders: List<FolderDto> = emptyList(),
    val error: String? = null,
    val deleted: Boolean = false,
)

@HiltViewModel
class ItemDetailViewModel @Inject constructor(
    selectedItemHolder: SelectedItemHolder,
    private val galleryRepository: GalleryRepository,
    private val remixStateHolder: RemixStateHolder,
    private val settingsRepository: SettingsRepository,
    val mediaDataSourceFactory: OkHttpDataSource.Factory,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ItemDetailUiState(item = selectedItemHolder.selected.value))
    val uiState: StateFlow<ItemDetailUiState> = _uiState

    init {
        viewModelScope.launch {
            val result = galleryRepository.fetchFolders()
            if (result is ApiResult.Success) {
                _uiState.value = _uiState.value.copy(folders = result.data)
            }
        }
    }

    fun moveTo(folder: String) {
        val item = _uiState.value.item ?: return
        viewModelScope.launch {
            when (val result = galleryRepository.moveItem(item.name, item.kind, folder)) {
                is ApiResult.Success -> {
                    if (result.data.ok) {
                        _uiState.value = _uiState.value.copy(item = item.copy(folder = folder))
                    } else {
                        _uiState.value = _uiState.value.copy(error = result.data.error ?: "Couldn't move that item.")
                    }
                }
                else -> _uiState.value = _uiState.value.copy(error = result.errorMessage())
            }
        }
    }

    fun delete() {
        val item = _uiState.value.item ?: return
        viewModelScope.launch {
            when (val result = galleryRepository.deleteItem(item.name, item.kind)) {
                is ApiResult.Success -> {
                    if (result.data.ok) {
                        _uiState.value = _uiState.value.copy(deleted = true)
                    } else {
                        _uiState.value = _uiState.value.copy(error = result.data.error ?: "Couldn't delete that item.")
                    }
                }
                else -> _uiState.value = _uiState.value.copy(error = result.errorMessage())
            }
        }
    }

    /** Returns true if a remix was actually queued (the item has a known model). */
    fun remix(): Boolean {
        val item = _uiState.value.item ?: return false
        val model = item.model ?: return false
        remixStateHolder.set(RemixRequest(modelKey = model, prompt = item.prompt.orEmpty(), seed = item.seed ?: 0L))
        return true
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /** For sharing outside the app - unlike Coil/ExoPlayer, an external share target
     * never passes through this app's interceptors, so it needs the real server URL,
     * not the internal placeholder host `.toAbsoluteMediaUrl()` produces. */
    suspend fun resolveExternalUrl(relativeUrl: String): String =
        settingsRepository.serverBaseUrl.first().trimEnd('/') + relativeUrl
}
