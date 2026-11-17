package dev.kbzon.launcher.ui.home

import android.text.format.DateFormat
import android.text.format.DateUtils
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.kbzon.launcher.search.CalendarEvent
import dev.kbzon.launcher.ui.R
import java.util.Date

/**
 * [fork] Tarjeta con los eventos de hoy y mañana (se abre al tocar el próximo evento bajo el reloj).
 */
@Composable
fun HomeEventsPopup(
    visible: Boolean,
    events: List<CalendarEvent>,
    onEvent: (CalendarEvent) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    AnimatedVisibility(visible, enter = fadeIn(), exit = fadeOut()) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f))
                .combinedClickable(interactionSource = null, indication = null, onClick = onDismiss),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = scaleIn(spring(dampingRatio = 0.8f, stiffness = 700f), initialScale = 0.9f, transformOrigin = TransformOrigin(0.5f, 0.2f)) + fadeIn(),
                exit = scaleOut(targetScale = 0.95f) + fadeOut(),
            ) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .widthIn(max = 420.dp)
                        .fillMaxWidth()
                        .combinedClickable(interactionSource = null, indication = null, onClick = {}),
                ) {
                    Column(
                        Modifier.padding(20.dp).heightIn(max = 520.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(stringResource(R.string.fork_events_title), style = MaterialTheme.typography.titleMedium)
                        if (events.isEmpty()) {
                            Text(
                                stringResource(R.string.fork_events_empty),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                        events.groupBy { dayLabel(context, it.startTime ?: it.endTime) }.forEach { (day, dayEvents) ->
                            Text(
                                day,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 12.dp, bottom = 2.dp),
                            )
                            dayEvents.forEach { ev ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .combinedClickable(onClick = { onEvent(ev) })
                                        .padding(vertical = 8.dp),
                                ) {
                                    Box(
                                        Modifier
                                            .size(8.dp)
                                            .background(ev.color?.let { Color(it) } ?: MaterialTheme.colorScheme.primary, CircleShape),
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        if (ev.allDay) stringResource(R.string.fork_events_all_day)
                                        else ev.startTime?.let { DateFormat.getTimeFormat(context).format(Date(it)) } ?: "",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.width(72.dp),
                                    )
                                    Text(
                                        ev.labelOverride ?: ev.label,
                                        style = MaterialTheme.typography.bodyLarge,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun dayLabel(context: android.content.Context, time: Long): String = when {
    DateUtils.isToday(time) -> context.getString(R.string.fork_events_today)
    DateUtils.isToday(time - DateUtils.DAY_IN_MILLIS) -> context.getString(R.string.fork_events_tomorrow)
    else -> DateFormat.format("EEEE d", time).toString().replaceFirstChar { it.uppercase() }
}
