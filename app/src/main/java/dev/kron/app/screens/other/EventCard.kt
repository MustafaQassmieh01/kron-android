package dev.kron.app.screens.other

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.kron.app.R
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.kron.app.models.network.Event
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EventCard(event: Event, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(event.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Text(event.courseName.ifBlank { event.courseId }, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .7f), maxLines = 2)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.LocationOn, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha=.65f))
                    Spacer(Modifier.width(4.dp))
                    Text(event.locations.firstOrNull()?.id?.uppercase() ?: stringResource(R.string.common_unknown), style = MaterialTheme.typography.labelMedium, maxLines = 1)
                    Spacer(Modifier.width(12.dp))
                    Icon(Icons.Outlined.Person, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha=.65f))
                    Spacer(Modifier.width(4.dp))
                    Text(event.teachers.firstOrNull()?.displayName ?: stringResource(R.string.common_no_teacher), style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                val chipColor = if (event.isSpecial) Color(0xFFD32F2F) else parseColor(event.colorHex)
                Surface(color = chipColor.copy(alpha=.12f), shape = RoundedCornerShape(8.dp)) {
                    Text("${time(event.from)}–${time(event.to)}", Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = chipColor, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

fun parseColor(hex: String): Color = runCatching {
    val raw = hex.removePrefix("#")
    val value = raw.toLong(16)
    when (raw.length) {
        6 -> Color(0xFF000000 or value)
        8 -> Color(value)
        else -> Color(0xFFF1377E)
    }
}.getOrDefault(Color(0xFFF1377E))

fun time(date: Date): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
fun dayTitle(date: Date): String = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(date)
