package com.example.android.car_locator

import android.app.Application
import com.example.android.car_locator.di.myModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level


class App: Application() {

//    companion object {
//        var api: RetrofitApi? = null
//    }

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@App)
            modules(myModules)
        }

    }
}