package me.yujinyan.retrofit

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.Test
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create

class CustomConverterTest {

  private val server = MockWebServer()

  private val moshi = Moshi.Builder()
    .add(MoshiResultTypeAdapterFactory())
    .addLast(KotlinJsonAdapterFactory())
    .build()

  private val retrofit = Retrofit.Builder()
    .baseUrl(server.url("/"))
    .addCallAdapterFactory(SuspendResultCallAdapterFactory {
      println("caught error: $it")
    })
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .build()

  private val api = retrofit.create<UserApi>()

  @Test
  fun `successful request`() = runBlocking {
    server.enqueueResponse(
      """ 
        {
          "errcode": 0,
          "data": {"id": 1, "name": "Peter"} 
        }
      """
    )

    api.getUser(1).should {
      it.isSuccess shouldBe true
      it.getOrThrow().name shouldBe "Peter"
      it.getOrThrow().id shouldBe 1
    }
  }

  @Test
  fun `server returned business error`() = runBlocking {
    server.enqueueResponse(
      """ 
        {
          "errcode": 500,
          "msg": "Whoops."
        }
      """
    )

    api.getUser(1).should {
      it.isSuccess shouldBe false
      it.exceptionOrNull()
        .shouldBeTypeOf<BusinessException>()
        .should { e ->
          e.message shouldBe "Whoops."
          e.code shouldBe 500
        }
    }
  }

  @Test
  fun `http 500`() = runBlocking {
    server.enqueueResponse("Server Error") {
      setResponseCode(500)
    }

    api.getUser(1).should {
      it.isSuccess shouldBe false
      it.exceptionOrNull().shouldBeTypeOf<HttpException>()
        .code() shouldBe 500
    }
  }

  @Test
  fun `invalid json`() = runBlocking {
    server.enqueueResponse("some gibberish")

    api.getUser(1).should {
      it.isSuccess shouldBe false
      it.exceptionOrNull().shouldBeTypeOf<JsonEncodingException>()
    }
  }

  @Test
  fun `json data mismatch`() = runBlocking {
    server.enqueueResponse(
      """
      {"foo": 1, "bar": 2}
    """
    )

    api.getUser(1).should {
      it.isSuccess shouldBe false
      it.exceptionOrNull().shouldBeTypeOf<JsonDataException>()
    }

    server.enqueueResponse(
      """
        {
          "errcode":"",
          "data":null
        }
    """
    )
    api.getUser(1).should {
      it.isSuccess shouldBe false
      it.exceptionOrNull().shouldBeTypeOf<JsonDataException>()
    }
  }


}