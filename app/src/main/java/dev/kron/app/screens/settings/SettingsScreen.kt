package dev.kron.app.screens.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.kron.app.R
import dev.kron.app.application.KronApplication
import dev.kron.app.application.settings.AppAppearance

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(app: KronApplication, onBack: () -> Unit) {
    val context = LocalContext.current
    val appearance by app.appSettings.appearance.collectAsState()
    val bookmarks by app.appSettings.bookmarkedProgrammes.collectAsState()

    fun openLanguageSettings() {
        val intent = if (Build.VERSION.SDK_INT >= 33) {
            Intent(Settings.ACTION_APP_LOCALE_SETTINGS, Uri.fromParts("package", context.packageName, null))
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
        }
        context.startActivity(intent)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = { TextButton(onClick = onBack) { Text(stringResource(R.string.common_done)) } }
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                SettingsCard(stringResource(R.string.settings_appearance)) {
                    AppAppearance.entries.forEach { item ->
                        RadioRow(appearanceLabel(item), appearance == item) {
                            app.appSettings.setAppearance(item)
                        }
                    }
                    HorizontalDivider()
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.settings_language), fontWeight = FontWeight.Medium)
                            Text(
                                stringResource(R.string.settings_language_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .65f)
                            )
                        }
                        TextButton(onClick = ::openLanguageSettings) {
                            Text(stringResource(R.string.common_change))
                        }
                    }
                }
            }

            item {
                SettingsCard(stringResource(R.string.settings_bookmarks)) {
                    if (bookmarks.isEmpty()) {
                        Text(stringResource(R.string.settings_no_saved_schedules))
                    } else {
                        bookmarks.forEach { (id, data) ->
                            Row(
                                Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(id, fontWeight = FontWeight.Medium)
                                    Text(data.schoolId, style = MaterialTheme.typography.labelSmall)
                                }
                                TextButton(
                                    onClick = {
                                        app.eventStorage.removeEventsForProgramme(id)
                                        app.appSettings.removeBookmarkedProgramme(id)
                                    }
                                ) {
                                    Text(stringResource(R.string.common_remove))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun appearanceLabel(value: AppAppearance): String = when (value) {
    AppAppearance.SYSTEM -> stringResource(R.string.appearance_system)
    AppAppearance.DARK -> stringResource(R.string.appearance_dark)
    AppAppearance.LIGHT -> stringResource(R.string.appearance_light)
}

@Composable
private fun SettingsCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Card {
            Column(Modifier.padding(16.dp).fillMaxWidth(), content = content)
        }
    }
}

@Composable
private fun RadioRow(title: String, selected: Boolean, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected, onClick = onClick)
        Text(title)
    }
}
