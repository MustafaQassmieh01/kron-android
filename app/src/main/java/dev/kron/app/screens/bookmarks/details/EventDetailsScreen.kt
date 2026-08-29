package dev.kron.app.screens.bookmarks.details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.kron.app.R
import dev.kron.app.application.KronApplication
import dev.kron.app.screens.bookmarks.EmptyState
import dev.kron.app.screens.other.time
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(app: KronApplication, eventId: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val allEvents by app.eventStorage.events.collectAsState()
    val event = allEvents.firstOrNull { it.id == eventId }

    if (event == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.event_title)) },
                    navigationIcon = { TextButton(onClick = onBack) { Text(stringResource(R.string.common_back)) } }
                )
            }
        ) { padding ->
            Box(Modifier.padding(padding)) {
                EmptyState(
                    stringResource(R.string.event_unavailable_title),
                    stringResource(R.string.event_unavailable_subtitle)
                )
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.event_details_title)) },
                navigationIcon = { TextButton(onClick = onBack) { Text(stringResource(R.string.common_back)) } }
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card {
                    Column(Modifier.padding(18.dp)) {
                        Text(event.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        if (event.courseName.isNotBlank() || event.courseId.isNotBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                event.courseName.ifBlank { event.courseId },
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .72f)
                            )
                        }
                    }
                }
            }
            item {
                DetailCard(
                    stringResource(R.string.event_date),
                    SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(event.from)
                )
            }
            item { DetailCard(stringResource(R.string.event_time), "${time(event.from)} – ${time(event.to)}") }
            if (event.locations.isNotEmpty()) {
                item {
                    DetailCard(
                        stringResource(R.string.event_location),
                        event.locations.joinToString("\n") { loc ->
                            listOf(
                                loc.name.ifBlank { loc.id },
                                loc.building.takeIf { it.isNotBlank() },
                                loc.floor.takeIf { it.isNotBlank() }?.let { context.getString(R.string.event_floor, it) }
                            ).filterNotNull().filter { it.isNotBlank() }.joinToString(" · ")
                        }
                    )
                }
            }
            if (event.teachers.isNotEmpty()) {
                item {
                    DetailCard(
                        stringResource(R.string.event_teachers),
                        event.teachers.joinToString("\n") { it.displayName }
                    )
                }
            }
            if (event.isSpecial) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Text(
                            stringResource(R.string.event_special),
                            Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailCard(title: String, value: String) {
    Card {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
