package infra.webservice

import infra.configs.APP_CONTEXT_PREFIX
import infra.configs.APP_PORT
import api.constants.HttpConstants
import api.constants.Messages
import api.exceptions.HttpException
import infra.configs.MOCK_ENVS
import infra.configs.USE_MOCKS
import net.logstash.logback.argument.StructuredArguments
import org.eclipse.jetty.http.HttpStatus
import org.slf4j.LoggerFactory
import spark.Request
import spark.Response
import spark.servlet.SparkApplication
import spark.Spark.*
import java.util.UUID

private val logger = LoggerFactory.getLogger(RouterApplication::class.java)

abstract class RouterApplication : SparkApplication {
    override fun init() {
        port(APP_PORT)

        routing()
    }
    abstract fun routes()

    open fun mockRoutes() {}

    fun appContextUri() : String {
        return APP_CONTEXT_PREFIX
    }

    open fun healthRoutes() {
        get("/health") { _ : Request, _: Response ->
            """{ "health":"OK" }"""
        }
    }

    private fun generateCorrelationId() : String = UUID.randomUUID().toString()

    final fun routing() {
        healthRoutes()

        routes()

        path(appContextUri()){
            healthRoutes()

            routes()
        }

        if (USE_MOCKS && MOCK_ENVS.contains(System.getenv("ENV")?:System.getProperty("ENV"))) {
            mockRoutes()
        }


        before("/*") { req, _ ->
            req.attribute("request_initial_time", System.currentTimeMillis())
            req.attribute("x-correlation-id", req.headers("x-correlation-id")?: generateCorrelationId())
        }

        afterAfter { req, res ->
            res.type(HttpConstants.HEADER_CONTENT_TYPE_APP_JSON);
            res.header(HttpConstants.HEADER_NAME_CONTENT_SECURITY_POLICY, HttpConstants.HEADER_NAME_CONTENT_SECURITY_POLICY_DEFAULT_VALUE)

            val cache =  if (res.status() >= HttpConstants.RES_STATUS_BAD_REQUEST) {
                "public, max-age=10"
            } else "public, max-age=600"
            res.header(HttpConstants.HEADER_NAME_CACHE_CONTROL, cache) //

            var finalTime : String
            var timeInNumber : Long = 999999L
            try {
                timeInNumber = System.currentTimeMillis() - req.attribute<Int>("request_initial_time")
                finalTime = "${timeInNumber}ms."
            } catch (e: Throwable) {
                finalTime = "Error calculating time."
            }

            res.header(HttpConstants.HEADER_NAME_TIME_ELAPSED, finalTime)
            res.header(HttpConstants.HEADER_NAME_TIME_ELAPSED_IN_MS, timeInNumber.toString())

            logger.info("Request processed",
                StructuredArguments.e(mapOf(
                    "event_type" to "request",
                    "http_method" to req.requestMethod(),
                    "uri" to req.uri(),
                    "params" to req.params()?.toString(),
                    "http_status" to res.status(),
                    "time_in_millis" to timeInNumber,
                    "time" to finalTime
                ))
            )
        }

        notFound { _, res ->
            logger.info("Exception: NOT_FOUND")
            res.type(HttpConstants.HEADER_CONTENT_TYPE_APP_JSON);
            res.status(HttpStatus.NOT_FOUND_404)
            """{"code": "${Messages.ERROR}", "message":"The resource you requested was not found"}""";
        }

        exception (Exception::class.java) { e, _, res ->
            res.type(HttpConstants.HEADER_CONTENT_TYPE_APP_JSON);
            when (e) {
                is HttpException -> {
                    res.status(e.status)
                    res.body("""{"code":"${Messages.ERROR}", "message":"${e.message}"}""");
                    logger.info("Exception: HTTP EXCEPTION",
                        StructuredArguments.e(mapOf(
                            "event_type" to "http_exception_log",
                            "http_status" to res.status(),
                            "cause" to e.cause.toString(),
                            "message" to e.message
                        ))
                    )
                }
                else -> {
                    e.printStackTrace() // Not funny to print stacktrace :( but necessary
                    res.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
                    res.body("""{"code": "${Messages.ERROR}", "message": "${Messages.INTERNAL_ERROR}"}""");
                    logger.error("Exception: GENERIC EXCEPTION",
                        StructuredArguments.e(mapOf(
                            "event_type" to "internal_error_log",
                            "http_status" to res.status(),
                            "cause" to e.cause.toString(),
                            "message" to e.message,
                            "trace" to e.stackTrace
                        ))
                    )
                }
            }
        }
    }

}