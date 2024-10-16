package io.github.dracula101.jetscan.data.platform.datasource.network.util
import io.github.dracula101.jetscan.data.platform.datasource.network.core.ResultCall
import retrofit2.Call

/**
 * Synchronously executes the [Call] and returns the [Result].
 */
inline fun <reified T : Any> Call<T>.executeForResult(): Result<T> =
    this
        .toResultCall()
        .executeForResult()

/**
 * Wraps the existing [Call] in a [ResultCall].
 */
inline fun <reified T : Any> Call<T>.toResultCall(): ResultCall<T> =
    ResultCall(
        backingCall = this,
        successType = T::class.java,
    )
