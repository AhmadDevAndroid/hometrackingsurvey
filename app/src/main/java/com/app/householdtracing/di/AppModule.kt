package com.app.householdtracing.di

import android.app.Application
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import androidx.datastore.preferences.protobuf.InvalidProtocolBufferException
import androidx.room.Room
import com.app.householdtracing.BuildConfig.BASE_URL
import com.app.householdtracing.LoginResponse
import com.app.householdtracing.data.db.AppDatabase
import com.app.householdtracing.data.db.DB_NAME
import com.app.householdtracing.data.repositoryImpl.AuthRepositoryImpl
import com.app.householdtracing.data.repositoryImpl.GeofencingRepository
import com.app.householdtracing.data.repositoryImpl.SunriseRepositoryImpl
import com.app.householdtracing.data.repositoryImpl.UserActivityTrackingRepository
import com.app.householdtracing.location.GeofenceManagerClient
import com.app.householdtracing.network.services.HouseHoldApiService
import com.app.householdtracing.sensors.SensorDetectionManager
import com.app.householdtracing.tracking.UserActivityTransitionManager
import com.app.householdtracing.ui.viewmodels.LoginScreenViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.TimeUnit

val appModule = module {

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

    viewModel { LoginScreenViewModel(get()) }

    single {
        DataStoreFactory.create(
            serializer = UserSerializer,
            produceFile = { File(get<Application>().filesDir, "user.pb") }
        )
    }

    single { UserActivityTransitionManager(get()) }
    single { GeofenceManagerClient(get()) }
    single { SensorDetectionManager(get()) }

}

object UserSerializer : Serializer<LoginResponse> {
    override val defaultValue: LoginResponse = LoginResponse.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): LoginResponse {
        try {
            return LoginResponse.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: LoginResponse, output: OutputStream) = t.writeTo(output)
}


val repositoryModule = module {
    single { SunriseRepositoryImpl(get(), get()) }
    single { AuthRepositoryImpl(get(), get()) }
    single { GeofencingRepository(get()) }
    single { UserActivityTrackingRepository() }
}