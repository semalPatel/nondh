package nondh.shared.db

import app.cash.sqldelight.driver.native.NativeSqliteDriver

actual class DatabaseFactory {
    actual fun create(): NondhDatabase {
        val driver = NativeSqliteDriver(NondhDatabase.Schema, "nondh.db")
        return NondhDatabase(driver)
    }
}
