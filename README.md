# Retrofit Kotlin Result Adapter

A Retrofit 2 `CallAdapter.Factory` for Kotlin suspend functions that use the standard
library [`kotlin.Result`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-result/) as return value. The call
adapter catches all exceptions so that there is no need to try catch suspend functions at call sites.

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

You can register a default failure handler through the constructor.

```kotlin
val retrofit = Retrofit.Builder()
  .baseUrl("https://")
  .addCallAdapterFactory(SuspendResultCallAdapterFactory {
    report(it)
  })
  .addConverterFactory(MoshiConverterFactory.create(moshi))
  .build()
```
