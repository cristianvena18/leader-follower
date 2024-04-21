package infra.bus.events

import aws.sdk.kotlin.services.sqs.SqsClient
import aws.sdk.kotlin.services.sqs.model.ChangeMessageVisibilityRequest
import aws.sdk.kotlin.services.sqs.model.DeleteMessageRequest
import aws.sdk.kotlin.services.sqs.model.Message
import aws.sdk.kotlin.services.sqs.model.ReceiveMessageRequest
import infra.utils.runForestRun
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext
import kotlin.math.pow


abstract class SQSChannelConsumer(
    private val sqs: SqsClient,
    private val sqsTaskProcessURL: String,
    private val numberOfWorkers: Int,
    private val intervalSeconds: Int = 3,
    private val backoffRetry: Int = 6
) : CoroutineScope {

    private val logger = LoggerFactory.getLogger(SQSChannelConsumer::class.java)
    private val supervisorJob = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + supervisorJob

    fun start() = launch {
        logger.info("about to launch for $sqsTaskProcessURL with $numberOfWorkers workers")
        val taskProcessMessageChannel = Channel<Message>()

        repeat(numberOfWorkers) {
            launchProcessor(taskProcessMessageChannel, sqsTaskProcessURL)
        }
        launchMsgReceiver(taskProcessMessageChannel, sqsTaskProcessURL)
    }

    fun stop() = supervisorJob.cancel()

    private fun CoroutineScope.launchMsgReceiver(channel: SendChannel<Message>, queueURL: String) = launch {
        runForestRun {
            val receiveMessageRequest = ReceiveMessageRequest {
                queueUrl = queueURL
                maxNumberOfMessages = 10
                waitTimeSeconds = 3
                messageAttributeNames = listOf("All")
            }
            val response = sqs.receiveMessage(receiveMessageRequest)
            val messages = response.messages
            logger.debug("Hilo ${Thread.currentThread().name} obtuvo ${messages?.size} mensajes")
            logger.debug("launchMsgReceiver")
            messages?.forEach {
                channel.send(it)
            }


        }
    }

    private suspend fun processErrorMsg(msg: Message, e: Exception) {
        logger.debug("Hilo ${Thread.currentThread().name} excepción $e con mensaje ${msg.body}")

        var retries = msg.attributes?.get("ApproximateReceiveCount")?.toInt()

        if (retries == null) {
            retries = 1
        }

        val exponentialBackoffRetry = (intervalSeconds + retries).toDouble().pow(backoffRetry).toInt()

        logger.debug("i gonna retry message on $exponentialBackoffRetry seconds")

        val input = ChangeMessageVisibilityRequest {
            receiptHandle = msg.receiptHandle
            queueUrl = sqsTaskProcessURL
            visibilityTimeout = exponentialBackoffRetry
        }

        sqs.changeMessageVisibility(input)
    }

    private fun CoroutineScope.launchProcessor(
        channel: ReceiveChannel<Message>,
        queue: String,
    ) = launch {
        runForestRun {
            for (msg in channel) {
                try {
                    if (processMsg(msg)) {
                        deleteMessage(msg, queue)
                    }
                } catch (e: Exception) {
                    processErrorMsg(msg, e)
                }
            }
        }
    }

    abstract suspend fun processMsg(message: Message): Boolean;

    private suspend fun deleteMessage(message: Message, queue: String) {
        val deleteMessageRequest = DeleteMessageRequest {
            receiptHandle = message.receiptHandle
            queueUrl = queue
        }
        sqs.deleteMessage(deleteMessageRequest)
        logger.info("${Thread.currentThread().name} Mensaje eliminado: ${message.body}")
    }
}