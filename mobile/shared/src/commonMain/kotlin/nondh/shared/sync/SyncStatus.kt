package nondh.shared.sync

data class SyncStatus(
    val inProgress: Boolean = false,
    val lastSuccessAt: Long? = null,
    val lastError: String? = null
)
