package me.yujinyan.retrofit

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer

fun MockWebServer.enqueueResponse(
  string: String,
  block: (MockResponse.() -> Unit)? = null
): MockResponse = MockResponse().setBody(string)
  .apply {
    if (block != null) {
      block()
    }
  }
  .also { enqueue(it) }