package application.base

import domains.Task
import infra.utils.runForestRun
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import net.logstash.logback.argument.StructuredArguments
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext

abstract class BaseWorkService<T>(
    private val numberOfWorkers: Int = 1
) : CoroutineScope {

    private val logger = LoggerFactory.getLogger(BaseWorkService::class.java)
    private val supervisorJob = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + supervisorJob;

    fun start() = launch {
        val channel = Channel<T>()
        repeat(numberOfWorkers) {
            launchProcessor(channel)
        }

        launchMessageReceiver(channel)
    }

    fun stop() = supervisorJob.cancel()

    abstract suspend fun receiveMessages(): T

    abstract suspend fun processMessage(task: T)

    abstract suspend fun handleError(e: Throwable, task: T)

    private fun CoroutineScope.launchMessageReceiver(channel: Channel<T>) = launch {
        runForestRun {
            try {
                val tasks = receiveMessages()

                channel.send(tasks)

            } catch (e: Exception) {
                logger.error(
                    StructuredArguments.e(
                        mapOf(
                            "message" to e.message,
                            "stacktrace" to e.stackTrace.toString()
                        )
                    ).toString()
                )
            }
        }
    }

    protected fun CoroutineScope.launchProcessor(
        channel: ReceiveChannel<T>
    ) = launch {
        runForestRun {
            for (task in channel) {
                try {
                    processMessage(task)
                } catch (e: Throwable) {
                    handleError(e, task)
                }
            }
        }
    }
}