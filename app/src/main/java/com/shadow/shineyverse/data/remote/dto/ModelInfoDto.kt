package com.shadow.shineyverse.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ModelInfoDto(
    val kind: String, // "image" | "video" | "video_i2v"
    val label: String,
    val width: Int,
    val height: Int,
    val steps: Int,
    val cfg: Float,
    val sampler: String,
    val scheduler: String,
    @SerialName("eta_seconds") val etaSeconds: Int = 0,
    val length: Int? = null, // video models only
    val fps: Int? = null, // video models only
)

const val MODEL_KIND_IMAGE = "image"
const val MODEL_KIND_VIDEO = "video"
const val MODEL_KIND_VIDEO_I2V = "video_i2v"
