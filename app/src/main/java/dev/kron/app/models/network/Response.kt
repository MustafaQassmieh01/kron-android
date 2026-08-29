package dev.kron.app.models.network

import com.google.gson.annotations.SerializedName
import java.util.Date

data class School(
    val id: String,
    val name: String,
    val domain: String = "",
    val urls: List<String> = emptyList(),
    @SerializedName("logoUrl", alternate = ["logo_url"]) val logoUrl: String = ""
)

data class SchoolsResponse(val schools: Map<String, School> = emptyMap())

data class Programme(val id: String, val title: String, val subtitle: String = "")
data class ProgrammeSearchResponse(val count: Int = 0, val programmes: List<Programme> = emptyList())

data class Teacher(
    val id: String,
    @SerializedName("first_name") val firstName: String = "",
    @SerializedName("last_name") val lastName: String = ""
) {
    val displayName: String get() = listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ").ifBlank { "Unknown" }
}

data class Location(
    val id: String,
    val name: String = "",
    val building: String = "",
    val floor: String = "",
    @SerializedName("max_seats") val maxSeats: String = ""
)


data class Event(
    val id: String,
    @SerializedName("schedule_id") val encodedScheduleId: String,
    val title: String,
    @SerializedName("course_id") val courseId: String = "",
    @SerializedName("course_name") val courseName: String = "",
    val teachers: List<Teacher> = emptyList(),
    val from: Date,
    val to: Date,
    val locations: List<Location> = emptyList(),
    @SerializedName("last_modified") val lastModified: Date,
    @SerializedName("is_special") val isSpecial: Boolean = false,
    @SerializedName("color") val colorHex: String = "#6750A4"
) {
    // Mirrors iOS normalization: server may return schedule IDs with %2B.
    val scheduleId: String
        get() = runCatching { java.net.URLDecoder.decode(encodedScheduleId.replace("+", "%2B"), Charsets.UTF_8.name()) }.getOrDefault(encodedScheduleId)

    fun withUpdatedColor(hex: String) = copy(colorHex = hex)
}

data class EventsResponse(val count: Int = 0, val events: List<Event> = emptyList())
