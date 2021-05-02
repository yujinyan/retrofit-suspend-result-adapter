package me.yujinyan.retrofit

import com.squareup.moshi.*
import java.lang.RuntimeException
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * Moshi [JsonAdapter.Factory] that deserializes JSON string with following structure.
 * ```
 * {
 *   "errcode": 0,
 *   "data": {"id": 1, "name": "Peter"}
 * }
 * ```
 *
 * It tries to unwrap the `data` field and place it in a [Result].
 *
 */
class MoshiResultTypeAdapterFactory : JsonAdapter.Factory {
  override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? {
    val rawType = type.rawType
    if (rawType != Result::class.java) return null
    val dataType: Type = (type as? ParameterizedType)?.actualTypeArguments?.firstOrNull() ?: return null
    val dataTypeAdapter = moshi.nextAdapter<Any>(this, dataType, annotations)
    return ResultTypeAdapter(dataTypeAdapter)
  }

  class ResultTypeAdapter<T>(
    private val dataTypeAdapter: JsonAdapter<T>
  ) : JsonAdapter<T>() {
    override fun fromJson(reader: JsonReader): T? {
      reader.beginObject()
      var errcode: Int? = null
      var msg: String? = null
      var data: Any? = null

      while (reader.hasNext()) {
        when (reader.nextName()) {
          "errcode" -> errcode = reader.nextString().toIntOrNull()
          "msg" -> msg = reader.nextString()
          "data" -> data = dataTypeAdapter.fromJson(reader)
          else -> reader.skipValue()
        }
      }
      reader.endObject()

      if (errcode == null) throw JsonDataException("Expected field errcode not present.")

      return if (errcode != 0) throw BusinessException(errcode, msg)
      else data as T
    }

    override fun toJson(writer: JsonWriter, value: T?): Unit = TODO("Not yet implemented")
  }
}

class BusinessException(val code: Int, message: String?) : RuntimeException(message)