package infra.libs.gson

import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import api.exceptions.BadRequestException

class GsonUtils () {
    companion object {
        private val gson: Gson = GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create()

        fun <T: Any> getJsonBody (body: String, clazzType : Class<T>): T {
            try {
                return gson.fromJson(body, clazzType) as T
            } catch (ise: Throwable) {
                throw BadRequestException(cause = ise)
            }
        }
        fun toJson(objekt: Any) : String{
            return gson.toJson(objekt)
        }
    }
}