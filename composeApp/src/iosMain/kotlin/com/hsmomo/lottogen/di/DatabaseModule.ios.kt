package com.hsmomo.lottogen.di

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.hsmomo.lottogen.db.LottoDatabase
import org.koin.dsl.module

actual val databaseModule = module {
    single {
        val driver = NativeSqliteDriver(
            schema = LottoDatabase.Schema,
            name = "lotto.db"
        )
        LottoDatabase(driver)
    }
}
