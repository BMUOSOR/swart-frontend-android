package com.antigravity.swart.di

import com.antigravity.swart.data.remote.SwartApi
import com.antigravity.swart.data.repository.ExhibitionRepositoryImpl
import com.antigravity.swart.domain.repository.ExhibitionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSwartApi(): SwartApi {
        return Retrofit.Builder()
            // IP apuntando al propio móvil (redirigido al PC por cable USB gracias a adb reverse)
            .baseUrl("http://127.0.0.1:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SwartApi::class.java)
    }

    @Provides
    @Singleton
    fun provideExhibitionRepository(api: SwartApi): ExhibitionRepository {
        return ExhibitionRepositoryImpl(api)
    }
}
