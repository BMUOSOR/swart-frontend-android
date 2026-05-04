package com.antigravity.swart.domain.repository

import com.antigravity.swart.domain.model.Exhibition

interface ExhibitionRepository {
    suspend fun getFeed(): Result<List<Exhibition>>
}
