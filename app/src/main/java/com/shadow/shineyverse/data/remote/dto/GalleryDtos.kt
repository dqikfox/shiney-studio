package com.shadow.shineyverse.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GalleryItemDto(
    val url: String,
    val kind: String, // "image" | "video"
    val name: String,
    val folder: String,
    val mtime: Double,
    val prompt: String? = null,
    val model: String? = null,
    val seed: Long? = null,
    @SerialName("gen_num") val genNum: Int? = null,
)

@Serializable
data class GalleryResponseDto(
    val items: List<GalleryItemDto>,
    val total: Int,
    val offset: Int,
    val limit: Int,
)

@Serializable
data class FolderDto(val name: String, val count: Int)

/** Shared shape for every mutation endpoint (folders + item move/copy/delete). */
@Serializable
data class MutationResultDto(val ok: Boolean, val error: String? = null)
