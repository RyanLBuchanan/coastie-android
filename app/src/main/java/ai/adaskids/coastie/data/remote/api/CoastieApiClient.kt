package ai.adaskids.coastie.data.remote.api

import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * NOTE: This client is optional. The app currently uses ai.adaskids.coastie.data.CoastieApi.
 * Keeping this file compile-safe in case you want it later.
 */
class CoastieApiClient(
    private val baseUrl: String
) {
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    fun sendJson(
        message: String,
        onResult: (Result<String>) -> Unit
    ) {
        val bodyJson = JSONObject().put("message", message).toString()
        val body = bodyJson.toRequestBody(jsonMediaType)

        val req = Request.Builder()
            .url(baseUrl)
            .post(body)
            .build()

        client.newCall(req).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onResult(Result.failure(e))
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val text = it.body?.string().orEmpty()
                    if (!it.isSuccessful) {
                        onResult(Result.failure(IllegalStateException("HTTP ${it.code}: $text")))
                        return
                    }

                    val reply = runCatching {
                        JSONObject(text).optString("reply", text)
                    }.getOrDefault(text)

                    onResult(Result.success(reply))
                }
            }
        })
    }
}
