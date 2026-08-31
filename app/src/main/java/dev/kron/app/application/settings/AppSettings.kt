package dev.kron.app.application.settings

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppAppearance { SYSTEM, DARK, LIGHT }

data class BookmarkedProgrammeData(val schoolId: String)

class AppSettings(context: Context) {
    private val prefs = context.getSharedPreferences("kron_shared", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _bookmarkedProgrammes = MutableStateFlow(loadBookmarks())
    val bookmarkedProgrammes: StateFlow<Map<String, BookmarkedProgrammeData>> = _bookmarkedProgrammes.asStateFlow()

    private val _appearance = MutableStateFlow(enumPref("appearance", AppAppearance.SYSTEM))
    val appearance: StateFlow<AppAppearance> = _appearance.asStateFlow()

    /**
     * Full-version entitlement hook.
     *
     * The Android port does not have billing/entitlement verification yet, so the
     * current build behaves as the free version. Replace this implementation with
     * the verified store entitlement when billing is added.
     */
    val hasFullVersion: Boolean
        get() = false

    fun setAppearance(value: AppAppearance) {
        prefs.edit().putString("appearance", value.name).apply()
        _appearance.value = value
    }

    fun addBookmarkedProgramme(programmeId: String, schoolId: String) {
        val next = _bookmarkedProgrammes.value.toMutableMap().apply {
            put(programmeId, BookmarkedProgrammeData(schoolId))
        }
        saveBookmarks(next)
    }

    fun removeBookmarkedProgramme(programmeId: String) {
        val next = _bookmarkedProgrammes.value.toMutableMap().apply { remove(programmeId) }
        saveBookmarks(next)
    }

    fun isBookmarked(programmeId: String) = _bookmarkedProgrammes.value.containsKey(programmeId)

    private fun loadBookmarks(): Map<String, BookmarkedProgrammeData> {
        val json = prefs.getString("bookmarkedProgrammes", null) ?: return emptyMap()
        val type = object : TypeToken<Map<String, BookmarkedProgrammeData>>() {}.type
        return runCatching {
            gson.fromJson<Map<String, BookmarkedProgrammeData>>(json, type)
                .filterValues { it.schoolId.isNotBlank() }
        }.getOrDefault(emptyMap())
    }

    private fun saveBookmarks(value: Map<String, BookmarkedProgrammeData>) {
        prefs.edit().putString("bookmarkedProgrammes", gson.toJson(value)).apply()
        _bookmarkedProgrammes.value = value
    }

    private inline fun <reified T : Enum<T>> enumPref(key: String, default: T): T =
        runCatching { enumValueOf<T>(prefs.getString(key, default.name) ?: default.name) }.getOrDefault(default)
}
