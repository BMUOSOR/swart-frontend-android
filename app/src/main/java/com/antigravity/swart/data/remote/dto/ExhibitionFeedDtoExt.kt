package com.antigravity.swart.data.remote.dto

import com.antigravity.swart.domain.model.Exhibition

fun ExhibitionFeedDto.toDomain(): Exhibition {
    return Exhibition(
        id = idExposicion,
        title = titulo,
        description = descrip,
        artistName = artistaNombre,
        artistAvatarUrl = artistaAvatar,
        isNew = isNew,
        artworksCount = obrasCount,
        artworkImagesUrls = obrasImages
    )
}
