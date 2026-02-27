package nondh.shared.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SyncRunner(
    private val scope: CoroutineScope,
    private val managerProvider: () -> SyncManager
) {
    fun trigger() {
        scope.launch {
            managerProvider().sync()
        }
    }

    fun schedule(intervalMillis: Long): Job {
        return scope.launch {
            while (isActive) {
                managerProvider().sync()
                delay(intervalMillis)
            }
        }
    }
}
