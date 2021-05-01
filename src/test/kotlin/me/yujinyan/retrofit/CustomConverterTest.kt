package me.yujinyan.retrofit

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
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
    MockResponse().setBody(
      """ 
        {
          "errcode": 0,
          "data": {"id": 1, "name": "Peter"} 
        }
      """
    ).also { server.enqueue(it) }

    api.getUser(1).should {
      it.isSuccess shouldBe true
      it.getOrThrow().name shouldBe "Peter"
    }
  }

  @Test
  fun `server returned business error`() = runBlocking {
    MockResponse().setBody(
      """ 
        {
          "errcode": 500,
          "msg": "Whoops."
        }
      """
    ).also { server.enqueue(it) }

    api.getUser(1).should {
      it.isSuccess shouldBe false
      it.exceptionOrNull()
        .shouldNotBeNull()
        .message shouldBe "Whoops."
    }
  }

  @Test
  fun `http 500`() = runBlocking {
    MockResponse().setResponseCode(500).setBody("Server Error")
      .also { server.enqueue(it) }

    api.getUser(1).should {
      it.isSuccess shouldBe false
      it.exceptionOrNull().shouldBeTypeOf<HttpException>()
        .code() shouldBe 500
    }
  }

  @Test
  fun `invalid json`() = runBlocking {
    MockResponse().setResponseCode(200).setBody("some gibberish ...")
      .also { server.enqueue(it) }

    api.getUser(1).should {
      it.isSuccess shouldBe false
      it.exceptionOrNull().shouldBeTypeOf<JsonEncodingException>()
    }
  }

  @Test
  fun `json data mismatch`() = runBlocking {
    MockResponse().setResponseCode(200).setBody(
      """
      {"foo": 1, "bar": 2}
    """
    ).also { server.enqueue(it) }

    api.getUser(1).should {
      it.isSuccess shouldBe false
      it.exceptionOrNull().shouldBeTypeOf<JsonDataException>()
    }
  }
}