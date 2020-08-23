package com.example.easynews.network

import kotlinx.coroutines.*

object AsyncTask {

    fun <R> CoroutineScope.executeAsyncTask(
            onPreExecute: () -> Unit,
            doInBackground: () -> R,
            onPostExecute: (R) -> Unit
    ) = launch {
        onPreExecute() // runs in Main Thread
        val result = withContext(Dispatchers.IO) {
            doInBackground() // runs in background thread without blocking the Main Thread
        }
        onPostExecute(result) // runs in Main Thread
    }
}