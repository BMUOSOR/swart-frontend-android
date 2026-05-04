package com.antigravity.swart.data.remote.dto

data class ExhibitionFeedDto(
    val idExposicion: Long,
    val titulo: String,
    val descrip: String?,
    val artistaNombre: String,
    val artistaAvatar: String,
    val isNew: Boolean,
    val obrasCount: Int,
    val obrasImages: List<String>,
    val exposicionImgUrl: String?
)
