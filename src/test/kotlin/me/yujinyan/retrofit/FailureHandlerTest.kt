package me.yujinyan.retrofit

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create

class FailureHandlerTest {
  private val server = MockWebServer()

  private val moshi = Moshi.Builder()
    .addLast(KotlinJsonAdapterFactory())
    .build()

  private val throwables = mutableListOf<Throwable>()

  private val retrofit = Retrofit.Builder()
    .baseUrl(server.url("/"))
    .addCallAdapterFactory(SuspendResultCallAdapterFactory {
      throwables += it
    })
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .build()

  private val api = retrofit.create<UserApi>()

  @Test
  fun `failure handler works`() = runBlocking {
    MockResponse().setBody("").also { server.enqueue(it) }
    api.getUser(1).should {
      it.isFailure shouldBe true
      it.exceptionOrNull() shouldBe throwables.first()
    }
  }
}