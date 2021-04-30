package me.yujinyan.retrofit

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlinx.coroutines.runBlocking
import me.yujinyan.retrofit.UserApi.User
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.Test
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Query
import java.time.Duration


interface UserApi {
  @GET("/user")
  suspend fun getUser(@Query("id") id: Int): Result<User>

  data class User(val id: Int, val name: String)
}

class KotlinResultCallAdapterFactoryTest {

  private val server = MockWebServer()

  private val moshi = Moshi.Builder()
    .addLast(KotlinJsonAdapterFactory())
    .build()

  @OptIn(ExperimentalStdlibApi::class)
  val userAdapter = moshi.adapter<User>()

  private val retrofit = Retrofit.Builder()
    .baseUrl(server.url("/"))
    .addCallAdapterFactory(KotlinResultCallAdapterFactory())
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .client(
      OkHttpClient.Builder()
        .apply { callTimeout(Duration.ofMillis(100)) }
        .build()
    )
    .build()

  private val api = retrofit.create<UserApi>()

  @Test
  fun `can request successfully`() = runBlocking {
    User(1, "Peter")
      .let { MockResponse().setBody(userAdapter.toJson(it)) }
      .also { server.enqueue(it) }

    api.getUser(1).should {
      it.isSuccess shouldBe true
      it.getOrThrow().name shouldBe "Peter"
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
}
