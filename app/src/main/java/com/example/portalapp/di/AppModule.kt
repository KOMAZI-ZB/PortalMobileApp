package com.example.portalapp.di

import com.example.portalapp.BuildConfig
import com.example.portalapp.network.AuthApi
import com.example.portalapp.network.NotificationsApi
import com.example.portalapp.network.FaqApi
import com.example.portalapp.network.ModulesApi
import com.example.portalapp.network.DocumentsApi // ⬅️ new
import com.example.portalapp.network.interceptors.AuthInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton fun provideMoshi(): Moshi =
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    @Provides @Singleton
    fun provideOkHttp(authInterceptor: AuthInterceptor): OkHttpClient {
        val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logger)
            .build()
    }

    @Provides @Singleton
    fun provideRetrofit(moshi: Moshi, client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(client)
            .build()

    @Provides @Singleton fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)
    @Provides @Singleton fun provideNotificationsApi(retrofit: Retrofit): NotificationsApi = retrofit.create(NotificationsApi::class.java)
    @Provides @Singleton fun provideFaqApi(retrofit: Retrofit): FaqApi = retrofit.create(FaqApi::class.java)
    @Provides @Singleton fun provideModulesApi(retrofit: Retrofit): ModulesApi = retrofit.create(ModulesApi::class.java)

    // ⬇️ NEW
    @Provides @Singleton
    fun provideDocumentsApi(retrofit: Retrofit): DocumentsApi =
        retrofit.create(DocumentsApi::class.java)
}
