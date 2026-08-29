package dev.kron.app.services.kron.api

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import dev.kron.app.BuildConfig
import dev.kron.app.models.network.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.lang.reflect.Type
import java.util.Date
import java.util.concurrent.TimeUnit

sealed class NetworkError(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class Server(val code: Int) : NetworkError("Server error (HTTP $code)")
    data object Forbidden : NetworkError("Access forbidden")
    data object NotFound : NetworkError("Resource not found")
    data object NoInternet : NetworkError("No internet connection")
    class Decode(cause: Throwable) : NetworkError("Failed to decode response", cause)
}

class KronApiService(private val context: Context) {
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(Date::class.java, FlexibleDateAdapter())
        .create()

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun getSchools(): List<School> {
        val response: SchoolsResponse = get("/.well-known/schools.json", emptyMap())
        return response.schools.values.sortedBy { it.name.lowercase() }
    }

    suspend fun searchProgrammes(query: String, school: String): ProgrammeSearchResponse =
        get("/api/v1/programme/search", mapOf("search_query" to query, "school" to school))

    suspend fun getScheduleEvents(school: String, scheduleIds: List<String>): EventsResponse =
        get(
            "/api/v1/schedule/events",
            mapOf("school" to school, "schedule_ids" to scheduleIds.joinToString(","))
        )

    private suspend inline fun <reified T> get(path: String, query: Map<String, String>): T = withContext(Dispatchers.IO) {
        assertInternetAvailable()
        val base = BuildConfig.API_URL.toHttpUrl()
        val builder = base.newBuilder().encodedPath(path)
        // Important parity detail: OkHttp encodes literal '+' as %2B in schedule IDs.
        query.forEach { (key, value) -> builder.addQueryParameter(key, value) }

        val request = Request.Builder()
            .url(builder.build())
            .get()
            .header("Accept", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            when (response.code) {
                403 -> throw NetworkError.Forbidden
                404 -> throw NetworkError.NotFound
            }
            if (!response.isSuccessful) throw NetworkError.Server(response.code)
            try {
                gson.fromJson<T>(body, object : TypeToken<T>() {}.type)
            } catch (t: Throwable) {
                throw NetworkError.Decode(t)
            }
        }
    }

    private fun assertInternetAvailable() {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: throw NetworkError.NoInternet
        val caps = cm.getNetworkCapabilities(network) ?: throw NetworkError.NoInternet
        if (!caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) throw NetworkError.NoInternet
    }
}

private class FlexibleDateAdapter : JsonDeserializer<Date>, JsonSerializer<Date> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Date {
        val raw = json.asString
        return runCatching { Date.from(java.time.Instant.parse(raw)) }
            .recoverCatching { Date.from(java.time.OffsetDateTime.parse(raw).toInstant()) }
            .getOrElse { throw JsonParseException("Unsupported date: $raw", it) }
    }

    override fun serialize(src: Date, typeOfSrc: Type, context: JsonSerializationContext): JsonElement =
        JsonPrimitive(src.toInstant().toString())
}
