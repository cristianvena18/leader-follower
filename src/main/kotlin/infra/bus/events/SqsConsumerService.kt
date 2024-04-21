package infra.bus.events

import aws.sdk.kotlin.services.sqs.SqsClient
import kotlinx.coroutines.runBlocking

class SqsConsumerService {
    companion object {

        private val sqsClient = SqsClient {
            region = "us-east-1"
        }

        fun run() = runBlocking {
            /*with(
                StatusSubscriber(sqsClient)
            ) {
                start()
            }*/
        }
    }

}