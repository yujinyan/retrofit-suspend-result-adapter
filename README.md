# Retrofit Suspend Kotlin Result Adapter

[![Release](https://jitpack.io/v/yujinyan/retrofit-suspend-result-adapter.svg?style=flat-square)](https://jitpack.io/#User/Repo)

A Retrofit 2 `CallAdapter.Factory` for Kotlin suspend functions that use the standard
library [`kotlin.Result`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-result/) as return value. The call
adapter catches network failures / deserialization errors so that there is no need to try catch suspend functions at
call sites.

Many useful methods are available on the `Result` type. Once [Kotlin allows null-safety operators `?.`, `?:` and `!!`
on `Result`](https://github.com/Kotlin/KEEP/pull/244), the ergonomics of using this type will be greatly improved.

> Note that using `kotlin.Result` as function return type requires Kotlin 1.5.

## Usage

```kotlin
val retrofit = Retrofit.Builder()
  .baseUrl("https://example.com")
  .addCallAdapterFactory(SuspendResultCallAdapterFactory())
  .build()
```

Your retrofit service can now use suspend functions with `Result<T>` return type.

```kotlin
interface MyService {
  @GET("/user")
  suspend fun getUser(): Result<User>
}
```

On Android, you can call suspend functions on the main thread without try catch.

```kotlin
lifecycleScope.launch {
  retrofit.create<MyService>()
    .getUser()
    .getOrNull()?.let {
      // update UI
      binding.nameLabel.value = it
    }
}
```

## Failure handler

You can register a default failure handler through the constructor. This is a good place for configuring global error
handling logic.

```kotlin
val retrofit = Retrofit.Builder()
  .baseUrl("https://example.com")
  .addCallAdapterFactory(SuspendResultCallAdapterFactory {
    report(it)
  })
  .addConverterFactory(MoshiConverterFactory.create(moshi))
  .build()
```

## Custom converter for `Result<T>`

If your API returns data wrapped in an "envelop", it may be a good idea to unwrap the data and place it in `Result`
directly.

Suppose the API response looks like this. You can still use `Result<User>` as return type.

```json
{
  "errcode": 0,
  "data": {
    "id": 1,
    "name": "Peter"
  }
}
```

To achieve this, write a custom converter.
Checkout [CustomConverterTest](src/test/kotlin/me/yujinyan/retrofit/CustomConverterTest.kt) for an example.
