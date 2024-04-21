package infra.configs

val APP_PORT : Int = (System.getenv("APP_PORT")?: "8080").toInt()
val APP_IP: String = (System.getenv("APP_IP")?: "localhost")
val SERVICE_NAME = System.getenv("SERVICE_NAME")?: "localhost"

val APP_CONTEXT_PREFIX = System.getenv("APP_CONTEXT_PREFIX")?:""

val USE_MOCKS = (System.getenv("USE_MOCKS")?.toBoolean()) ?: true
val MOCK_ENVS = listOf("local", "develop")
