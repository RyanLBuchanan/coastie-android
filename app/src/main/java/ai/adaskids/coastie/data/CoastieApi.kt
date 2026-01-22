package ai.adaskids.coastie.data

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

data class ChatResult(
    val reply: String? = null,
    val error: String? = null
)

class CoastieApi(private val baseUrl: String) {

    // Increase timeouts so long responses don't fail early.
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)   // allow Coastie to “think”
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    fun chatJsonAsync(message: String, callback: (ChatResult) -> Unit) {
        val safeMsg = message
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")

        val bodyJson = """{"message":"$safeMsg"}"""
        val body = bodyJson.toRequestBody(jsonMediaType)

        val request = Request.Builder()
            .url(baseUrl)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(ChatResult(error = e.message ?: "Network error"))
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val text = it.body?.string().orEmpty()
                    if (!it.isSuccessful) {
                        callback(ChatResult(error = "HTTP ${it.code}: $text"))
                        return
                    }

                    val reply = Regex(""""reply"\s*:\s*"((?:\\.|[^"\\])*)"""")
                        .find(text)
                        ?.groupValues
                        ?.getOrNull(1)
                        ?.replace("\\n", "\n")
                        ?.replace("\\\"", "\"")
                        ?.replace("\\\\", "\\")

                    callback(ChatResult(reply = reply ?: text))
                }
            }
        })
    }
}
