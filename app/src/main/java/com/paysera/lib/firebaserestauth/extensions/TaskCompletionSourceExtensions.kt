package com.paysera.lib.firebaserestauth.extensions

import com.google.android.gms.tasks.TaskCompletionSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope

fun <T : Any> TaskCompletionSource<T>.execute(
    work: suspend CoroutineScope.() -> T?
) : TaskCompletionSource<T> {

    GlobalScope.launch(work = {
        work()
    }, onSuccess = {
        trySetResult(it)
    }, onError = {
        trySetException(Exception(it))
    })

    return this
}