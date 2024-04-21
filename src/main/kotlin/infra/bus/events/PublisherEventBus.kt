package infra.bus.events

import aws.sdk.kotlin.services.sns.SnsClient
import aws.sdk.kotlin.services.sns.model.MessageAttributeValue
import aws.sdk.kotlin.services.sns.model.PublishRequest
import domains.events.DomainEvent
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

private val TOPIC_ARN_PUBLISHER = System.getenv("TOPIC_ARN_PUBLISHER") ?: System.getProperty("TOPIC_ARN_PUBLISHER")
private val logger = LoggerFactory.getLogger(PublisherEventBus::class.java)

class PublisherEventBus {
    companion object {
        private var snsClient: SnsClient? = null

        private fun getClient(): SnsClient {
            if (snsClient == null) {
                snsClient = SnsClient {
                    region = "us-east-1" // Replace with your desired AWS region
                }
            }
            return snsClient!!
        }

        fun initialize() {
            try {
                getClient().let {
                    logger.info("sns client initialized")
                }
            } catch (e: Exception) {
                logger.error("error while initialize sns client: ${e.message}")
            }
        }

        fun publishEvent(event: DomainEvent, metadata: Map<String, String> = mapOf()) = runBlocking {
            try {
                val attributes = mutableMapOf(
                    "eventName" to MessageAttributeValue {
                        this.stringValue = event.eventName
                        this.dataType = "String"
                    },
                    "sourceName" to MessageAttributeValue {
                        this.stringValue = "qr-kit-api-service"
                        this.dataType = "String"
                    },
                    "bodyVersion" to MessageAttributeValue {
                        this.stringValue = "1.0.0"
                        this.dataType = "String"
                    }
                )

                val request = PublishRequest {
                    messageAttributes = attributes
                    message = event.toString()
                    topicArn = TOPIC_ARN_PUBLISHER
                }
                logger.info("start to publish message: {}", request)
                try {
                    val snsClient = getClient()
                    launch {
                        snsClient.publish(request).runCatching {
                            logger.info("message published: " + this.messageId)
                        }
                    }

                } catch (ex: Exception) {
                    logger.error("exception while publishing a message ${ex.toString()}, ${ex.stackTraceToString()}")
                }
            } catch (ex: Exception) {
                logger.error("error while publishing a event: " + ex.message)
            }
        }
    }
}