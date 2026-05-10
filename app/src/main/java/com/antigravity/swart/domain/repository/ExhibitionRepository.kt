package com.antigravity.swart.domain.repository

import com.antigravity.swart.domain.model.Exhibition
import com.antigravity.swart.domain.model.MapPin

interface ExhibitionRepository {
    suspend fun getFeed(): Result<List<Exhibition>>
    suspend fun getMapPins(): Result<List<MapPin>>
}
