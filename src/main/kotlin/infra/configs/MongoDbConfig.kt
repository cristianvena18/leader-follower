package infra.configs

val MONGO_DB_URI: String = System.getenv("MONGO_DB_URI")?: System.getProperty("MONGO_DB_URI")?:"mongodb://localhost:27017/"
val MONGO_DB_DBNAME : String = System.getenv("MONGO_DB_DBNAME")?: System.getProperty("MONGO_DB_DBNAME")?:"qr_kit_db"
val MONGO_DB_USER: String = System.getenv("MONGO_DB_USER")?: System.getProperty("MONGO_DB_USER")?: ""
val MONGO_DB_PASS: String = System.getenv("MONGO_DB_PASS")?: System.getProperty("MONGO_DB_PASS")?: ""
val MONGO_DB_TLS: Boolean = (System.getenv("MONGO_DB_TLS")?:System.getProperty("MONGO_DB_TLS"))?.toBoolean()?:false
val MONGO_DB_TLS_FILE: String? = System.getenv("MONGO_DB_TLS_FILE")?:System.getProperty("MONGO_DB_TLS_FILE")?:null
val MONGO_DB_TRUST_STORE: String = System.getenv("MONGO_DB_TRUST_STORE")?: System.getProperty("MONGO_DB_TRUST_STORE")?: ""
val MONGO_DB_TRUST_STORE_TYPE : String = System.getenv("MONGO_DB_TRUST_STORE_TYPE")?: System.getProperty("MONGO_DB_TRUST_STORE_TYPE")?: "JKS"
val MONGO_DB_TRUST_STORE_PASS: String = System.getenv("MONGO_DB_TRUST_STORE_PASS")?: System.getProperty("MONGO_DB_TRUST_STORE_PASS")?: ""
val MONGO_DB_RETRY_WRITES: Boolean = (System.getenv("MONGO_DB_RETRY_WRITES")?:System.getProperty("MONGO_DB_RETRY_WRITES")?:"true").toBoolean()

const val COLLECTION_QR = "qr"
const val COLLECTION_SEQUENCES = "sequences"
const val COLLECTION_QR_PACKAGE = "qr_package"
const val COLLECTION_QR_REQUEST = "qr_requests"
const val SEQUENCE_PACKAGE = "package_number"
const val SEQUENCE_REQUESTS =  "requests_number"
