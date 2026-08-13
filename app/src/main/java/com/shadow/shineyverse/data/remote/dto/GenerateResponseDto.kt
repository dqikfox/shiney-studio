package com.shadow.shineyverse.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Same shape covers both outcomes - `ok=false` responses only populate `error`. */
@Serializable
data class GenerateResponseDto(
    val ok: Boolean,
    val kind: String? = null,
    val url: String? = null,
    @SerialName("gen_num") val genNum: Int? = null,
    val error: String? = null,
)

@Serializable
data class UploadImageResponseDto(val filename: String)
