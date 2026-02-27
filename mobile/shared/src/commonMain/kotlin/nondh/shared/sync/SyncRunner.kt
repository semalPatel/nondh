package nondh.shared.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SyncRunner(
    private val scope: CoroutineScope,
    private val syncAction: suspend () -> Unit
) {
    fun trigger() {
        scope.launch {
            syncAction()
        }
    }

    fun schedule(intervalMillis: Long): Job {
        return scope.launch {
            while (isActive) {
                syncAction()
                delay(intervalMillis)
            }
        }
    }
}
