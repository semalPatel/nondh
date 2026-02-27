package nondh.shared.db

expect class DatabaseFactory {
    fun create(): NondhDatabase
}
