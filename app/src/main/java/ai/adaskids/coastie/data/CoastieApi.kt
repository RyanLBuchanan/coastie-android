package ai.adaskids.coastie.data

import android.content.ContentResolver
import android.net.Uri
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okio.BufferedSink
import okio.source
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

    /**
     * JSON POST: { "message": "..." }
     * Backend returns JSON { "reply": "..." }
     */
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

                    callback(ChatResult(reply = extractReply(text) ?: text))
                }
            }
        })
    }

    /**
     * Multipart/form-data POST:
     * - message: text
     * - file: a single file (PDF/doc/image/text), ~8MB limit (enforced server-side)
     *
     * Backend returns JSON { "reply": "..." }
     */
    fun chatMultipartAsync(
        contentResolver: ContentResolver,
        message: String,
        fileUri: Uri,
        fileName: String,
        mimeType: String,
        callback: (ChatResult) -> Unit
    ) {
        val fileRequestBody = object : RequestBody() {
            override fun contentType(): MediaType? = mimeType.toMediaType()

            override fun writeTo(sink: BufferedSink) {
                contentResolver.openInputStream(fileUri).use { input ->
                    if (input == null) {
                        throw IOException("Unable to open file stream")
                    }
                    sink.writeAll(input.source())
                }
            }
        }

        val multipartBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("message", message)
            // IMPORTANT: server expects the file field name to be "file"
            .addFormDataPart("file", fileName, fileRequestBody)
            .build()

        val request = Request.Builder()
            .url(baseUrl)
            .post(multipartBody)
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

                    callback(ChatResult(reply = extractReply(text) ?: text))
                }
            }
        })
    }

    /**
     * Extracts the "reply" field from a JSON response.
     * Falls back to null if not found.
     */
    private fun extractReply(responseBody: String): String? {
        // Matches: "reply": "...." where inner quotes and escapes may exist
        return Regex(""""reply"\s*:\s*"((?:\\.|[^"\\])*)"""")
            .find(responseBody)
            ?.groupValues
            ?.getOrNull(1)
            ?.replace("\\n", "\n")
            ?.replace("\\\"", "\"")
            ?.replace("\\\\", "\\")
    }
}
