package infra.utils

import kong.unirest.HttpResponse
import kong.unirest.JsonNode
import kong.unirest.Unirest
import kong.unirest.json.JSONObject
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(UnirestUtils::class.java)

val aux_number_of_retries = System.getenv("NUMBER_OF_RETRIES")?:System.getProperty("NUMBER_OF_RETRIES")?:"5"
val NUMBER_OF_RETRIES:Int = aux_number_of_retries.toInt()

class UnirestUtils {

    final val METHOD_GET = "GET"
    final val METHOD_POST = "POST"
    //TODO: AGREGAR TAMBIEN EL PATCH DELETE PUT EN CASO DE USARSE

    fun post (
        uri: String,
        body: Map<String, Any?>,
        headers: Map<String, String> = mapOf(),
    ): HttpResponse<JsonNode> {
        return request(METHOD_POST,  uri, headers, body)
    }

    fun get (
        uri: String,
        headers: Map<String, String>,
    ): HttpResponse<JsonNode> {
        return request(METHOD_GET,  uri, headers, mapOf())
    }

    fun request (
        method: String,
        uri: String,
        headers: Map<String, String>,
        body: Map<String, Any?>?
    ): HttpResponse<JsonNode> {
        var correctResponse = false
        var response: HttpResponse<JsonNode>
        var numberOfRetries = 0

        // TODO: add backoff
        do {
            val request = Unirest.request(method, uri)
                .headers(headers)

            val httpRequest = if(body != null) request.body(body) else request

            response = httpRequest.asJson()

            val jsonResponse = JSONObject(response.body.toString()) //JsonParser.parseString(response.body.toString())

            when (response.status) {
                in 200..299 -> {
                    // Correct Response
                    logger.debug("UnirestUtil - Correct $method execution. Response: ${jsonResponse.toMap()}- Retry: $numberOfRetries")
                    correctResponse = true
                }
                in 400..499 -> {
                    // Error Response
                    logger.debug("UnirestUtil - Failed to execute $method. Response: ${jsonResponse.toMap()} - Retry: $numberOfRetries")
                    correctResponse = true
                }
                in 500..599 -> {
                    // Service down, retry
                    numberOfRetries++
                    logger.error("UnirestUtil - Failed to execute $method. Response: ${jsonResponse.toMap()} - Retry: $numberOfRetries")
                }
                else -> {
                    // Another error, retry
                    numberOfRetries++
                    logger.error("UnirestUtil - Failed to execute unknown response: $method. Response: ${jsonResponse.toMap()} - Retry: $numberOfRetries")
                }
            }
        } while (!correctResponse && (numberOfRetries <= NUMBER_OF_RETRIES))

        return response
    }
}