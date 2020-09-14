package com.paysera.lib.firebaserestauth.extensions

import kotlinx.coroutines.*

fun <T : Any> CoroutineScope.launch(
    work: suspend CoroutineScope.() -> T?,
    onSuccess: ((T?) -> Unit),
    onError: ((error: Throwable) -> Unit) = { }
): Job {
    val errorHandler = CoroutineExceptionHandler { _, throwable ->
        CoroutineScope(Dispatchers.Main).launch {
            onError(throwable)
        }
    }
    return launch(Dispatchers.Main + errorHandler) {
        val data = async(Dispatchers.IO) rt@{
            return@rt work()
        }.await()
        onSuccess(data)
    }
}