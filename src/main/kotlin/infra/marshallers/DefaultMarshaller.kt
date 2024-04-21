package infra.marshallers

import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import api.constants.Messages
import api.exceptions.NotFoundException
import spark.ResponseTransformer
import java.text.SimpleDateFormat
import java.util.*
import org.bson.Document

class DefaultMarshaller : ResponseTransformer {
    private val gson : Gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()

    private val sdf = createDateFormat()

    private fun createDateFormat () : SimpleDateFormat{
        val s = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        s.timeZone = TimeZone.getTimeZone("UTC")
        return s
    }

    private fun validateModel(model: Any?) {
        checkNotNull(model) { Messages.ENTITY_NOT_FOUND }
    }

    override fun render(model: Any?) : String {
        try {
            validateModel(model)
        } catch (nullObject: Throwable) {
            throw NotFoundException(message = Messages.ENTITY_NOT_FOUND, cause = nullObject)
        }
        if (model is Document) {
            if (model["createdAt"] != null) {
                model["createdAt"] = sdf.format(model["createdAt"])
            }
            if (model["updatedAt"] != null) {
                model["updatedAt"] = sdf.format(model["updatedAt"])
            }
        }
        return gson.toJson(model)
    }


}