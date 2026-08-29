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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.kron.app.R
import dev.kron.app.application.KronApplication
import dev.kron.app.application.settings.BookmarksViewType
import dev.kron.app.models.network.Event
import dev.kron.app.screens.other.EventCard
import dev.kron.app.screens.other.dayTitle
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Date
import java.util.Locale

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
    val viewType by app.appSettings.bookmarkViewType.collectAsState()
    var refreshing by remember { mutableStateOf(false) }
    var refreshError by remember { mutableStateOf<String?>(null) }

    val bookmarkedIds = bookmarks.keys
    val visibleEvents = events.filter { it.scheduleId in bookmarkedIds }

    fun refreshSchedules() {
        if (bookmarks.isEmpty() || refreshing) return
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
                    IconButton(onClick = ::refreshSchedules, enabled = bookmarks.isNotEmpty() && !refreshing) {
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

                else -> {
                    ViewTypeTabs(viewType) { app.appSettings.setBookmarkViewType(it) }
                    when (viewType) {
                        BookmarksViewType.DAILY -> DailyEvents(visibleEvents, onEvent)
                        BookmarksViewType.WEEKLY -> WeeklyEvents(visibleEvents, onEvent)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ViewTypeTabs(selected: BookmarksViewType, onSelected: (BookmarksViewType) -> Unit) {
    SingleChoiceSegmentedButtonRow(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        BookmarksViewType.entries.forEachIndexed { index, type ->
            SegmentedButton(
                selected = selected == type,
                onClick = { onSelected(type) },
                shape = SegmentedButtonDefaults.itemShape(index, BookmarksViewType.entries.size)
            ) {
                Text(
                    when (type) {
                        BookmarksViewType.DAILY -> stringResource(R.string.bookmarks_daily)
                        BookmarksViewType.WEEKLY -> stringResource(R.string.bookmarks_weekly)
                    }
                )
            }
        }
    }
}

@Composable
private fun DailyEvents(events: List<Event>, onEvent: (String) -> Unit) {
    var selectedDay by remember { mutableStateOf(LocalDate.now()) }
    val dayEvents = events.filter { localDate(it.from) == selectedDay }.sortedBy { it.from }

    Column(Modifier.fillMaxSize()) {
        DayNavigator(
            title = selectedDay.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault())),
            onPrevious = { selectedDay = selectedDay.minusDays(1) },
            onNext = { selectedDay = selectedDay.plusDays(1) }
        )

        if (dayEvents.isEmpty()) {
            EmptyState(stringResource(R.string.bookmarks_no_events), stringResource(R.string.bookmarks_nothing_day))
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(dayEvents, key = { it.id }) { event ->
                    EventCard(event) { onEvent(event.id) }
                }
            }
        }
    }
}

@Composable
private fun WeeklyEvents(events: List<Event>, onEvent: (String) -> Unit) {
    var selectedDay by remember { mutableStateOf(LocalDate.now()) }
    val monday = selectedDay.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    Column(Modifier.fillMaxSize()) {
        DayNavigator(
            title = stringResource(
                R.string.bookmarks_week_number,
                monday.get(WeekFields.ISO.weekOfWeekBasedYear())
            ),
            onPrevious = { selectedDay = selectedDay.minusWeeks(1) },
            onNext = { selectedDay = selectedDay.plusWeeks(1) }
        )

        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            (0..6).forEach { index ->
                val day = monday.plusDays(index.toLong())
                FilterChip(
                    selected = day == selectedDay,
                    onClick = { selectedDay = day },
                    label = {
                        Text(
                            "${day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(2)}\n${day.dayOfMonth}"
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        val dayEvents = events.filter { localDate(it.from) == selectedDay }.sortedBy { it.from }
        if (dayEvents.isEmpty()) {
            EmptyState(stringResource(R.string.bookmarks_no_events), stringResource(R.string.bookmarks_nothing_day))
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(dayTitle(dayEvents.first().from), fontWeight = FontWeight.SemiBold)
                }
                items(dayEvents, key = { it.id }) { event ->
                    EventCard(event) { onEvent(event.id) }
                }
            }
        }
    }
}

@Composable
private fun DayNavigator(title: String, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextButton(onClick = onPrevious) { Text("‹") }
        Text(title, fontWeight = FontWeight.SemiBold)
        TextButton(onClick = onNext) { Text("›") }
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
