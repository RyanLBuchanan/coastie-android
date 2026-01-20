package ai.adaskids.coastie.data

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

data class ChatResult(
    val reply: String? = null,
    val error: String? = null
)

class CoastieApi(private val baseUrl: String) {

    private val client = OkHttpClient()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    fun chatJson(message: String): ChatResult {
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

        client.newCall(request).execute().use { resp ->
            val text = resp.body?.string().orEmpty()

            if (!resp.isSuccessful) {
                return ChatResult(error = "HTTP ${resp.code}: $text")
            }

            // Expected: {"reply":"..."}  (fallback to raw text if it doesn't match)
            val reply = Regex(""""reply"\s*:\s*"((?:\\.|[^"\\])*)"""")
                .find(text)
                ?.groupValues
                ?.getOrNull(1)
                ?.replace("\\n", "\n")
                ?.replace("\\\"", "\"")
                ?.replace("\\\\", "\\")

            return ChatResult(reply = reply ?: text)
        }
    }
}
