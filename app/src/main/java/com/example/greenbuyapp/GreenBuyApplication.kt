package com.example.greenbuyapp

import android.app.Application
import android.content.res.Configuration
import com.example.greenbuyapp.di.appModules
import com.example.greenbuyapp.domain.SharedPreferencesRepository
import com.example.greenbuyapp.util.applyLanguage
import com.example.greenbuyapp.util.applyTheme
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class GreenBuyApplication : Application() {
    private val sharedPreferencesRepository: SharedPreferencesRepository by inject()

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(
                // Workaround for https://github.com/InsertKoinIO/koin/issues/1188
                if (BuildConfig.DEBUG) Level.ERROR else Level.NONE
            )
            androidContext(this@GreenBuyApplication)
            workManagerFactory()
            modules(appModules)
        }
        applyLanguage(sharedPreferencesRepository.locale)
        applyTheme(sharedPreferencesRepository.theme)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        applyLanguage(sharedPreferencesRepository.locale)
    }

//    override fun onLowMemory() {
//        super.onLowMemory()
//        GlideApp.get(this).clearMemory()
//    }
//
//    override fun onTrimMemory(level: Int) {
//        super.onTrimMemory(level)
//        GlideApp.get(this).trimMemory(level)
//    }
}