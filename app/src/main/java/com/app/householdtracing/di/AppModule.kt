package com.app.householdtracing.di

import android.content.Context
import androidx.room.Room
import com.app.householdtracing.BuildConfig.BASE_URL
import com.app.householdtracing.data.db.AppDatabase
import com.app.householdtracing.data.db.DB_NAME
import com.app.householdtracing.data.repositoryImpl.AuthRepositoryImpl
import com.app.householdtracing.data.repositoryImpl.SunriseRepositoryImpl
import com.app.householdtracing.network.services.HouseHoldApiService
import com.app.householdtracing.ui.viewmodels.LoginScreenViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val appModule = module {
    val context: Context

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
        // Retrofit.Builder().baseUrl("https://api.sunrisesunset.io/")
        Retrofit.Builder().baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()).client(get<OkHttpClient>()).build()
    }

    single { get<Retrofit>().create(HouseHoldApiService::class.java) }

    single {
        Room.databaseBuilder(get(), AppDatabase::class.java, DB_NAME).build()
    }

    single { get<AppDatabase>().homeTrackingDao() }

    viewModel { LoginScreenViewModel(get(),get()) }

}

val repositoryModule = module {
    single { SunriseRepositoryImpl(get(), get()) }
    single { AuthRepositoryImpl(get()) }
}