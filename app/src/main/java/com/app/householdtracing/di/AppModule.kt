package com.app.householdtracing.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.app.householdtracing.data.datastore.PreferencesManager
import com.app.householdtracing.data.db.AppDatabase
import com.app.householdtracing.data.db.DB_NAME
import com.app.householdtracing.data.repositoryImpl.SunriseRepositoryImpl
import com.app.householdtracing.network.services.SunriseApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val appModule = module {
    val context : Context

    single {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    single {
        OkHttpClient.Builder().apply {
            readTimeout(120, TimeUnit.SECONDS)
            connectTimeout(120, TimeUnit.SECONDS)
            retryOnConnectionFailure(true)
            addInterceptor(get<HttpLoggingInterceptor>())
        }.build()
    }

    single {
        Retrofit.Builder().baseUrl("https://api.sunrisesunset.io/")
            .addConverterFactory(GsonConverterFactory.create()).client(get<OkHttpClient>()).build()
    }

    single { get<Retrofit>().create(SunriseApiService::class.java) }

    single {
        Room.databaseBuilder(get(), AppDatabase::class.java, DB_NAME).build()
    }

    single { get<AppDatabase>().homeTrackingDao() }

}

val repositoryModule = module {
    single { SunriseRepositoryImpl(get(),get()) }
}