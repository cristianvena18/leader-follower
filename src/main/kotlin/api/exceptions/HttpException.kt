package api.exceptions;

open class HttpException (
    open val status : Int = 500,
    override val message : String = "Generic exception ocurred",
    override val cause : Throwable
) : Exception(cause);

class BadRequestException(
    override val status: Int = 400,
    override val message: String = "Invalid request or parameters",
    override val cause: Throwable
) : HttpException(status, message,cause);

class NotFoundException (
        override val status : Int = 404,
        override val message : String = "Resource not found for input params",
        override val cause : Throwable
) : HttpException(status, message,cause);

class UnauthorizedException (
        override val status : Int = 403,
        override val message : String = "Unauthorized resquest access",
        override val cause : Throwable
) : HttpException(status, message,cause);

class notCause () : Throwable()

class withCause (
        override val message : String
) : Throwable()