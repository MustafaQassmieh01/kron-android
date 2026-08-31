package dev.kron.app.screens.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import dev.kron.app.models.network.Programme
import dev.kron.app.models.network.School

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(app: KronApplication, onBack: () -> Unit, onProgramme: (schoolId: String, programmeId: String) -> Unit) {
    var schools by remember { mutableStateOf<List<School>>(emptyList()) }
    var selectedSchoolId by rememberSaveable { mutableStateOf<String?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    var submittedQuery by rememberSaveable { mutableStateOf<String?>(null) }
    var searchRequestVersion by rememberSaveable { mutableIntStateOf(0) }
    var results by remember { mutableStateOf<List<Programme>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val selectedSchool = schools.firstOrNull { it.id == selectedSchoolId }

    LaunchedEffect(Unit) {
        loading = true
        runCatching { app.apiService.getSchools() }
            .onSuccess { loadedSchools ->
                schools = loadedSchools
                if (selectedSchoolId != null && loadedSchools.none { it.id == selectedSchoolId }) {
                    selectedSchoolId = null
                    submittedQuery = null
                }
            }
            .onFailure { error = it.message }
        loading = false
    }

    LaunchedEffect(selectedSchool?.id, submittedQuery, searchRequestVersion) {
        val school = selectedSchool ?: return@LaunchedEffect
        val searchText = submittedQuery?.takeIf { it.isNotBlank() } ?: return@LaunchedEffect

        loading = true
        error = null
        runCatching { app.apiService.searchProgrammes(searchText, school.id) }
            .onSuccess { results = it.programmes }
            .onFailure {
                results = emptyList()
                error = it.message
            }
        loading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.search_title)) },
                navigationIcon = { TextButton(onClick = onBack) { Text(stringResource(R.string.common_done)) } }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            when {
                selectedSchoolId == null -> {
                    Text(
                        stringResource(R.string.search_choose_university),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(20.dp)
                    )
                    if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp)) }
                    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(schools, key = { it.id }) { school ->
                            OutlinedButton(
                                onClick = {
                                    selectedSchoolId = school.id
                                    query = ""
                                    submittedQuery = null
                                    results = emptyList()
                                    error = null
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(Modifier.fillMaxWidth()) {
                                    Text(school.name, fontWeight = FontWeight.SemiBold)
                                    if (school.domain.isNotBlank()) {
                                        Text(school.domain, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }

                selectedSchool == null -> {
                    if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp)) }
                }

                else -> {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            selectedSchool.name,
                            modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.SemiBold
                        )
                        TextButton(
                            onClick = {
                                selectedSchoolId = null
                                query = ""
                                submittedQuery = null
                                results = emptyList()
                                error = null
                            }
                        ) {
                            Text(stringResource(R.string.common_change))
                        }
                    }

                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = query,
                            onValueChange = {
                                query = it
                                if (submittedQuery != null) {
                                    submittedQuery = null
                                    results = emptyList()
                                }
                            },
                            label = { Text(stringResource(R.string.search_programme_or_course)) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val searchText = query.trim()
                                if (searchText.isBlank()) return@Button
                                submittedQuery = searchText
                                searchRequestVersion++
                            }
                        ) {
                            Text(stringResource(R.string.search_title))
                        }
                    }

                    if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp)) }
                    if (submittedQuery != null && !loading && results.isEmpty() && error == null) {
                        Text(stringResource(R.string.search_no_programmes), modifier = Modifier.padding(20.dp))
                    }
                    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(results, key = { it.id }) { programme ->
                            ProgrammeCard(programme) { onProgramme(selectedSchool.id, programme.id) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgrammeCard(programme: Programme, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(programme.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (programme.subtitle.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(programme.subtitle, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .7f))
            }
            Spacer(Modifier.height(6.dp))
            Text(programme.id, style = MaterialTheme.typography.labelSmall)
        }
    }
}
