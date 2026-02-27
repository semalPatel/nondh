package nondh.shared.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SyncRunner(
    private val scope: CoroutineScope,
    private val manager: SyncManager
) {
    fun trigger() {
        scope.launch {
            manager.sync()
        }
    }

    fun schedule(intervalMillis: Long): Job {
        return scope.launch {
            while (isActive) {
                manager.sync()
                delay(intervalMillis)
            }
        }
    }
}
