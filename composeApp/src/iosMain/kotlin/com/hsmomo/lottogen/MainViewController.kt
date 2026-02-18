package com.hsmomo.lottogen

import androidx.compose.ui.window.ComposeUIViewController
import com.hsmomo.lottogen.di.appModule
import com.hsmomo.lottogen.di.databaseModule
import org.koin.core.context.startKoin

fun MainViewController() = ComposeUIViewController { App() }

fun initKoin() {
    startKoin {
        modules(databaseModule, appModule)
    }
}
