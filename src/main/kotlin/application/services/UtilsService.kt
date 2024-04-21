package services;

import net.logstash.logback.argument.StructuredArguments
import org.slf4j.LoggerFactory
import infra.webservice.RouterApplication
import java.security.SecureRandom

const val DEFAULT_RANDOM_NAME_LENGTH = 8;
private val logger = LoggerFactory.getLogger(RouterApplication::class.java)

class UtilsService {
	private val strChain : String = "lgwjzc7bxf9rs6dha2i8ven3t45q1kuypom0";

	fun getRandomName() : String {
        return getRandomName(DEFAULT_RANDOM_NAME_LENGTH)
    }
	fun getRandomName(length : Int ) : String{
		val sr = SecureRandom()
		val sb = StringBuffer()
		
		val maxIndex = strChain.length
		
		for (i in 1..length){
			sb.append(strChain[sr.nextInt(maxIndex)])
		}
		
		return sb.toString()
	}

	fun generateRandomUserId() : String {
		return getRandomName().uppercase()
	}

	fun meassureTime (description: String, method: () -> Any) : Any? {
		val init = System.currentTimeMillis()
		try {
			val result = method()

			val timeMillis = System.currentTimeMillis() - init

			logger.info("Meassuring method $description : ${timeMillis}ms.",
				StructuredArguments.e(
					mapOf("event_type" to "measure_time",
						"method_call" to description,
						"method_result" to "OK",
						"time_in_millis" to timeMillis,
						"time" to "${timeMillis}ms.",
						"result" to result?.toString())
				)
			)
			return result

		} catch (thr : Throwable) {
			val timeMillis = System.currentTimeMillis() - init

			logger.error("Excepcption meassuring method $description : ${timeMillis}ms.",
				StructuredArguments.e(
					mapOf("event_type" to "measure_time",
						"method_call" to description,
						"method_result" to "ERROR",
						"time_in_millis" to timeMillis,
						"time" to "${timeMillis}ms.",
						"exception" to thr?.message)
				)
			)
			throw thr
		}
	}

}