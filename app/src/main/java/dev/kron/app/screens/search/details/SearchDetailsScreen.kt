package dev.kron.app.screens.search.details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.kron.app.R
import dev.kron.app.application.KronApplication
import dev.kron.app.models.network.Event
import dev.kron.app.screens.other.EventCard
import dev.kron.app.screens.other.dayTitle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchDetailsScreen(
    app: KronApplication,
    schoolId: String,
    programmeId: String,
    onBack: () -> Unit,
    onEvent: (String) -> Unit
) {
    val bookmarks by app.appSettings.bookmarkedProgrammes.collectAsState()
    var events by remember(programmeId) { mutableStateOf<List<Event>>(emptyList()) }
    var loading by remember(programmeId) { mutableStateOf(true) }
    var error by remember(programmeId) { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val bookmarkLimitMessage = stringResource(R.string.schedule_free_bookmark_limit)

    LaunchedEffect(programmeId, schoolId) {
        loading = true
        error = null
        runCatching { app.apiService.getScheduleEvents(schoolId, listOf(programmeId)) }
            .onSuccess { events = it.events }
            .onFailure { error = it.message ?: "Failed to load schedule" }
        loading = false
    }

    val bookmarked = bookmarks.containsKey(programmeId)
    val groupedEvents = remember(events) {
        events.sortedBy { it.from }.groupBy { dayTitle(it.from) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.schedule_title)) },
                navigationIcon = { TextButton(onClick = onBack) { Text(stringResource(R.string.common_back)) } },
                actions = {
                    IconButton(
                        enabled = events.isNotEmpty(),
                        onClick = {
                            when {
                                bookmarked -> {
                                    app.appSettings.removeBookmarkedProgramme(programmeId)
                                    app.eventStorage.removeEventsForProgramme(programmeId)
                                }

                                !app.appSettings.hasFullVersion && bookmarks.isNotEmpty() -> {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(bookmarkLimitMessage)
                                    }
                                }

                                else -> {
                                    app.appSettings.addBookmarkedProgramme(programmeId, schoolId)
                                    app.eventStorage.saveEvents(events)
                                }
                            }
                        }
                    ) {
                        Icon(
                            if (bookmarked) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,
                            if (bookmarked) stringResource(R.string.a11y_remove_bookmark) else stringResource(R.string.a11y_bookmark)
                        )
                    }
                }
            )
        }
    ) { padding ->
        when {
            loading -> Box(Modifier.padding(padding).fillMaxSize()) {
                CircularProgressIndicator(Modifier.padding(32.dp))
            }

            error != null -> Text(
                error.orEmpty(),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(padding).padding(20.dp)
            )

            events.isEmpty() -> Text(
                stringResource(R.string.schedule_no_events),
                modifier = Modifier.padding(padding).padding(20.dp)
            )

            else -> LazyColumn(
                Modifier.padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        if (bookmarked) stringResource(R.string.schedule_saved) else stringResource(R.string.schedule_previewing),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                groupedEvents.forEach { (dateTitle, dayEvents) ->
                    item(key = "date-$dateTitle") {
                        Text(
                            dateTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }
                    items(dayEvents, key = { it.id }) { event ->
                        EventCard(event) {
                            app.eventStorage.saveEvents(listOf(event))
                            onEvent(event.id)
                        }
                    }
                }
            }
        }
    }
}
