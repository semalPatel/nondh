package nondh.shared.db

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

actual class DatabaseFactory(private val context: Context) {
    actual fun create(): NondhDatabase {
        val driver = AndroidSqliteDriver(NondhDatabase.Schema, context, "nondh.db")
        return NondhDatabase(driver)
    }
}
