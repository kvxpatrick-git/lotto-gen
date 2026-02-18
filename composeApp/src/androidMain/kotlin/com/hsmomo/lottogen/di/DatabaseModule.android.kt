package com.hsmomo.lottogen.di

import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.hsmomo.lottogen.db.LottoDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val databaseModule = module {
    single {
        val driver = AndroidSqliteDriver(
            schema = LottoDatabase.Schema,
            context = androidContext(),
            name = "lotto.db"
        )
        LottoDatabase(driver)
    }
}
