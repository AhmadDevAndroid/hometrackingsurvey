package com.app.householdtracing

import android.app.Application
import com.app.householdtracing.di.appModule
import com.app.householdtracing.di.repositoryModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class App : Application() {

    companion object {

        const val APP_TAG = "HouseHold_App"

        private lateinit var instance: App
        fun getInstance(): App = instance

    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(listOf( appModule, repositoryModule))
        }

    }

}