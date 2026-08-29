package dev.kron.app.services.kron.store.event

import android.content.Context
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import dev.kron.app.models.network.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.util.Calendar
import java.util.Date

/** Simple local JSON cache for the Android MVP. */
class EventStorageService(private val context: Context) {
    private val cacheFile = File(context.filesDir, "kron_events.json")
    private val cleanupPrefs = context.getSharedPreferences("kron_event_storage", Context.MODE_PRIVATE)

    private val gson = GsonBuilder()
        .registerTypeAdapter(Date::class.java, object : JsonSerializer<Date>, JsonDeserializer<Date> {
            override fun serialize(src: Date, typeOfSrc: java.lang.reflect.Type, context: JsonSerializationContext): JsonElement =
                JsonPrimitive(src.toInstant().toString())

            override fun deserialize(json: JsonElement, typeOfT: java.lang.reflect.Type, context: JsonDeserializationContext): Date =
                parseDate(json.asString)
        })
        .create()

    private val _events = MutableStateFlow(load())
    val events: StateFlow<List<Event>> = _events.asStateFlow()

    fun getEvent(id: String): Event? = _events.value.firstOrNull { it.id == id }

    @Synchronized
    fun saveEvents(newEvents: List<Event>) {
        val map = _events.value.associateBy { it.id }.toMutableMap()
        newEvents.forEach { map[it.id] = it }
        persist(map.values.sortedBy { it.from })
    }

    @Synchronized
    fun replaceEvents(newEvents: List<Event>, scheduleIds: List<String>) {
        val ids = scheduleIds.toSet()
        val retained = _events.value.filterNot { it.scheduleId in ids }
        persist((retained + newEvents).distinctBy { it.id }.sortedBy { it.from })
    }

    @Synchronized
    fun removeEventsForProgramme(programmeId: String) {
        persist(_events.value.filterNot { it.scheduleId == programmeId })
    }

    @Synchronized
    fun performAutomaticCleanup() {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        if (cleanupPrefs.getLong("lastEventStorageCleanup", -1L) == today) return

        val cutoff = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -30) }.time
        val cleaned = _events.value.filter { !it.from.before(cutoff) }
        if (cleaned.size != _events.value.size) persist(cleaned)
        cleanupPrefs.edit().putLong("lastEventStorageCleanup", today).apply()
    }

    private fun load(): List<Event> {
        if (!cacheFile.exists()) return emptyList()
        val type = object : TypeToken<List<Event>>() {}.type
        return runCatching { gson.fromJson<List<Event>>(cacheFile.readText(), type) }.getOrDefault(emptyList())
    }

    private fun persist(value: List<Event>) {
        val sorted = value.sortedBy { it.from }
        cacheFile.writeText(gson.toJson(sorted))
        _events.value = sorted
    }
}

private fun parseDate(raw: String): Date =
    runCatching { Date.from(java.time.Instant.parse(raw)) }
        .recoverCatching { Date.from(java.time.OffsetDateTime.parse(raw).toInstant()) }
        .getOrElse { throw IllegalArgumentException("Unsupported date $raw", it) }
