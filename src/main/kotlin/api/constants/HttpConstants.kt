package api.constants

class HttpConstants {
    companion object {
        // HTTP Constants
        // HTTP Status
        val RES_STATUS_OK = 200
        val RES_STATUS_CREATED = 201
        val RES_STATUS_NOT_FOUND = 404
        val RES_STATUS_BAD_REQUEST = 400
        val RES_STATUS_CONFLICT = 409
        val RES_STATUS_INTERNAL_ERROR = 500

        // HTTP Headers
        val HEADER_NAME_CONTENT_TYPE = "Content-type"
        val HEADER_CONTENT_TYPE_APP_JSON = "application/json"

        val HEADER_NAME_CONTENT_LENGTH = "Content-Length"
        val HEADER_NAME_CACHE_CONTROL= "Cache-Control"
        val HEADER_NOT_FOUND_CACHE = "public, max-age=86400"
        val HEADER_NAME_CONTENT_SECURITY_POLICY = "Content-Security-Policy"
        val HEADER_NAME_CONTENT_SECURITY_POLICY_DEFAULT_VALUE = "default-src 'none'"

        val HEADER_NAME_TIME_ELAPSED = "x-time-elapsed"
        val HEADER_NAME_TIME_ELAPSED_IN_MS = "x-time-elapsed-in-millis"
    }
}
