package dev.kron.app.screens.bookmarks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.kron.app.R
import dev.kron.app.application.KronApplication
import dev.kron.app.models.network.Event
import dev.kron.app.screens.other.EventCard
import dev.kron.app.screens.other.dayTitle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

private const val REFRESH_COOLDOWN_MS = 30_000L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    app: KronApplication,
    onSearch: () -> Unit,
    onSettings: () -> Unit,
    onEvent: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val events by app.eventStorage.events.collectAsState()
    val bookmarks by app.appSettings.bookmarkedProgrammes.collectAsState()
    var refreshing by remember { mutableStateOf(false) }
    var refreshCoolingDown by rememberSaveable { mutableStateOf(false) }
    var refreshError by remember { mutableStateOf<String?>(null) }

    val bookmarkedIds = bookmarks.keys
    val visibleEvents = events.filter { it.scheduleId in bookmarkedIds }

    LaunchedEffect(refreshCoolingDown) {
        if (refreshCoolingDown) {
            delay(REFRESH_COOLDOWN_MS)
            refreshCoolingDown = false
        }
    }

    fun refreshSchedules() {
        if (bookmarks.isEmpty() || refreshing || refreshCoolingDown) return
        refreshCoolingDown = true

        scope.launch {
            refreshing = true
            refreshError = null
            runCatching {
                bookmarks.entries.groupBy { it.value.schoolId }.forEach { (schoolId, entries) ->
                    val ids = entries.map { it.key }
                    val response = app.apiService.getScheduleEvents(schoolId, ids)
                    app.eventStorage.replaceEvents(response.events, ids)
                }
            }.onFailure {
                refreshError = it.message ?: "Failed to refresh schedules"
            }
            refreshing = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.bookmarks_title)) },
                actions = {
                    IconButton(
                        onClick = ::refreshSchedules,
                        enabled = bookmarks.isNotEmpty() && !refreshing && !refreshCoolingDown
                    ) {
                        Icon(Icons.Outlined.Refresh, stringResource(R.string.a11y_refresh))
                    }
                    IconButton(onClick = onSearch) {
                        Icon(Icons.Outlined.Add, stringResource(R.string.a11y_add_schedule))
                    }
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Outlined.Settings, stringResource(R.string.settings_title))
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            if (refreshing) LinearProgressIndicator(Modifier.fillMaxWidth())
            refreshError?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            when {
                bookmarks.isEmpty() -> EmptyState(
                    stringResource(R.string.bookmarks_empty_title),
                    stringResource(R.string.bookmarks_empty_subtitle),
                    onSearch,
                    stringResource(R.string.bookmarks_find_schedule)
                )

                visibleEvents.isEmpty() -> EmptyState(
                    stringResource(R.string.bookmarks_no_events),
                    stringResource(R.string.bookmarks_no_events_cached),
                    ::refreshSchedules,
                    stringResource(R.string.common_refresh)
                )

                else -> DailyEvents(visibleEvents, onEvent)
            }
        }
    }
}

@Composable
private fun DailyEvents(events: List<Event>, onEvent: (String) -> Unit) {
    val groupedEvents = remember(events) {
        events.sortedBy { it.from }.groupBy { localDate(it.from) }
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        groupedEvents.forEach { (date, dayEvents) ->
            item(key = "date-$date") {
                Text(
                    dayTitle(dayEvents.first().from),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                )
            }
            items(dayEvents, key = { it.id }) { event ->
                EventCard(event) { onEvent(event.id) }
            }
        }
    }
}

@Composable
fun EmptyState(title: String, subtitle: String, action: (() -> Unit)? = null, actionTitle: String = "") {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text(subtitle, color = MaterialTheme.colorScheme.onBackground.copy(alpha = .7f))
            if (action != null && actionTitle.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                Button(onClick = action) { Text(actionTitle) }
            }
        }
    }
}

private fun localDate(date: Date): LocalDate =
    date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
