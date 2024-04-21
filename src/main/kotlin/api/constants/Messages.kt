package api.constants

class Messages {
    companion object {
        // MESSAGES
        val OK = "OK"
        val SERVER_UP_AND_RUNNING = "Service up and running"

        // ERRORS
        val ERROR = "error"
        val INTERNAL_ERROR = "There was an unhandled exception"

        // Requests errors
        val INVALID_PARAMETERS = "Invalid request or parameters"

        // Entity errors
        val ENTITY_NOT_FOUND = "Entity not found"
        val MESSAGE_QR_NOT_FOUND = "QR not found"
        val MESSAGE_PACK_NOT_FOUND = "Package not found"
        val MESSAGE_QR_REQUEST_NOT_FOUND = "QR request not found"

        // Database errors
        val ERROR_DUPLICATED_CONSTRAINT = "Database constraint violation: duplicated data"

        // QR Requests
        val CANT_DELETE_FINISHED_REQUEST = "QR request can't be deleted because its status is FINISHED"
        val CANT_DELETE_CANCELLED_REQUEST = "That QR request is already CANCELLED"
        val CANT_REQUEST_IF_REQ_PENDING = "There is already a pending QR request associated with that client and point id"
        val CANT_REQUEST_IF_QR_ACTIVE_OR_BLOCKED = "There is already an active or blocked QR associated with that client and point id"
    }
}

