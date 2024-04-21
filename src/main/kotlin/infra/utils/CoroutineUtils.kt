package infra.utils


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.yield
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(CoroutineScope::class.java)

suspend fun CoroutineScope.runForestRun(block: suspend () -> Unit) {
    while (isActive) {
        try {
            block()
            yield()
        } catch (ex: Exception) {
            println("En ${Thread.currentThread().name} falla con {$ex}. Reintentando...")
            ex.printStackTrace()
        }
    }

    logger.info("corutina en ${Thread.currentThread().name} saliendo")
}

