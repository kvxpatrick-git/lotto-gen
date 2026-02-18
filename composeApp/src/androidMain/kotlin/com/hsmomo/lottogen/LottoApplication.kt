package com.hsmomo.lottogen

import android.app.Application
import com.hsmomo.lottogen.di.appModule
import com.hsmomo.lottogen.di.databaseModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class LottoApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@LottoApplication)
            modules(databaseModule, appModule)
        }
    }
}
