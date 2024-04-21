package infra.configs

val host: String = System.getenv("REDIS_HOSTNAME") ?: System.getProperty("REDIS_HOSTNAME") ?: "redis.local" //"localhost" //"redis.local"
val port: Int = System.getenv("REDIS_PORT")?.toInt() ?: System.getProperty("REDIS_PORT")?.toInt() ?: 6379
val dbNumber: Int = System.getenv("REDIS_DB_NUMBER")?.toInt() ?: System.getProperty("REDIS_DB_NUMBER")?.toInt() ?: 15
val expirationTime: Long = System.getenv("REDIS_EXPIRATION_TIME")?.toLong() ?:
                                System.getProperty("REDIS_EXPIRATION_TIME")?.toLong() ?:
                                60 * 60 * 24 * 365 * 10

val tls: Boolean = System.getenv("REDIS_USE_TLS")?.toBoolean() ?: System.getProperty("REDIS_USE_TLS")?.toBoolean() ?: false
val password: String? = System.getenv("REDIS_PRIMARY_ACCESS_KEY") ?: System.getProperty("REDIS_PRIMARY_ACCESS_KEY")
val timeout: Int = System.getenv("REDIS_THREADS_IDLE_TIMEOUT")?.toInt() ?: 30000
val maxThreads: Int = System.getenv("REDIS_MAX_THREADS")?.toInt() ?: 0
