package de.mm20.launcher2.ui.home

import android.text.format.DateFormat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.mm20.launcher2.preferences.home.MinimalClockStyle
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.Locale

/** Hora actual que se actualiza al cambiar el minuto (o cada segundo para [everySecond]). */
@Composable
private fun rememberNow(everySecond: Boolean = false): Long {
    val now by produceState(System.currentTimeMillis()) {
        while (true) {
            value = System.currentTimeMillis()
            val step = if (everySecond) 1_000 else 60_000
            delay(step - value % step)
        }
    }
    return now
}

/**
 * [fork] Reloj de la home minimalista en el estilo elegido.
 * Tocar la hora → [onTimeClick] (alarmas); tocar la fecha → [onDateClick] (calendario).
 */
@Composable
fun HomeClock(
    style: MinimalClockStyle,
    shadow: Shadow?,
    accent: Color,
    onTimeClick: () -> Unit,
    onDateClick: (Long) -> Unit,
) {
    val context = LocalContext.current
    val now = rememberNow()
    val is24h = DateFormat.is24HourFormat(context)
    val hours = DateFormat.format(if (is24h) "HH" else "h", now).toString()
    val minutes = DateFormat.format("mm", now).toString()
    val datePattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), "EEEEdMMMM")
    val date = DateFormat.format(datePattern, now).toString().replaceFirstChar { it.uppercase() }

    val timeModifier = Modifier.noRippleClick(onTimeClick)
    val dateText: @Composable (TextStyle) -> Unit = { st ->
        Text(date, style = st.copy(shadow = shadow), modifier = Modifier.noRippleClick { onDateClick(now) })
    }
    val dateStyle = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Medium)

    when (style) {
        MinimalClockStyle.Thin -> Column {
            Text(
                "$hours:$minutes",
                style = TextStyle(fontSize = 76.sp, fontWeight = FontWeight.Thin, letterSpacing = (-2).sp, shadow = shadow),
                modifier = timeModifier,
            )
            dateText(dateStyle)
        }

        MinimalClockStyle.Bold -> Column {
            Text(
                "$hours:$minutes",
                style = TextStyle(fontSize = 68.sp, fontWeight = FontWeight.Black, letterSpacing = (-3).sp, shadow = shadow),
                modifier = timeModifier,
            )
            dateText(dateStyle.copy(fontWeight = FontWeight.Light, fontSize = 18.sp))
        }

        MinimalClockStyle.Stacked -> Column(modifier = timeModifier) {
            val big = TextStyle(fontSize = 104.sp, fontWeight = FontWeight.Light, lineHeight = 92.sp, letterSpacing = (-4).sp, shadow = shadow)
            Text(hours.padStart(2, '0'), style = big)
            Text(minutes, style = big, color = accent)
            Spacer(Modifier.height(8.dp))
            dateText(dateStyle)
        }

        MinimalClockStyle.Words -> Column {
            Text(
                timeInWords(now, Locale.getDefault()),
                style = TextStyle(fontSize = 40.sp, fontWeight = FontWeight.Light, lineHeight = 44.sp, shadow = shadow),
                modifier = timeModifier.padding(end = 24.dp),
            )
            Spacer(Modifier.height(10.dp))
            dateText(dateStyle)
        }

        MinimalClockStyle.Analog -> Column {
            AnalogClock(modifier = timeModifier.size(150.dp), accent = accent)
            Spacer(Modifier.height(14.dp))
            dateText(dateStyle)
        }

        MinimalClockStyle.DayProgress -> Column {
            Text(
                "$hours:$minutes",
                style = TextStyle(fontSize = 64.sp, fontWeight = FontWeight.Thin, letterSpacing = (-2).sp, shadow = shadow),
                modifier = timeModifier,
            )
            dateText(dateStyle)
            Spacer(Modifier.height(14.dp))
            DayProgressBar(now = now, accent = accent, shadow = shadow)
        }
    }
}

@Composable
private fun AnalogClock(modifier: Modifier, accent: Color) {
    val now = rememberNow(everySecond = true)
    val cal = remember(now) { Calendar.getInstance().apply { timeInMillis = now } }
    val minutes = cal.get(Calendar.MINUTE) + cal.get(Calendar.SECOND) / 60f
    val hours = (cal.get(Calendar.HOUR) + minutes / 60f)
    // aguja de minutos con un pequeño resorte al avanzar
    val minuteAngle by animateFloatAsState(minutes * 6f, spring(dampingRatio = 0.5f, stiffness = 300f), label = "min")
    val color = LocalContentColor.current
    Canvas(modifier) {
        val r = size.minDimension / 2f
        val c = Offset(size.width / 2f, size.height / 2f)
        for (i in 0 until 12) {
            rotate(i * 30f, c) {
                val long = i % 3 == 0
                drawLine(
                    color.copy(alpha = if (long) 0.9f else 0.4f),
                    Offset(c.x, c.y - r),
                    Offset(c.x, c.y - r + (if (long) 14.dp else 7.dp).toPx()),
                    strokeWidth = (if (long) 2.dp else 1.5.dp).toPx(),
                    cap = StrokeCap.Round,
                )
            }
        }
        rotate(hours * 30f, c) {
            drawLine(color, c, Offset(c.x, c.y - r * 0.5f), strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
        }
        rotate(minuteAngle, c) {
            drawLine(color, c, Offset(c.x, c.y - r * 0.78f), strokeWidth = 2.5.dp.toPx(), cap = StrokeCap.Round)
        }
        drawCircle(accent, radius = 4.dp.toPx(), center = c)
    }
}

@Composable
private fun DayProgressBar(now: Long, accent: Color, shadow: Shadow?) {
    val cal = Calendar.getInstance().apply { timeInMillis = now }
    val minutesOfDay = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
    val progress = minutesOfDay / (24f * 60f)
    val color = LocalContentColor.current
    Column(Modifier.fillMaxWidth(0.8f)) {
        Box(Modifier.fillMaxWidth().height(3.dp)) {
            Canvas(Modifier.matchParentSize()) {
                drawLine(color.copy(alpha = 0.25f), Offset(0f, size.height / 2), Offset(size.width, size.height / 2), size.height, StrokeCap.Round)
                drawLine(accent, Offset(0f, size.height / 2), Offset(size.width * progress, size.height / 2), size.height, StrokeCap.Round)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.labelMedium.copy(shadow = shadow),
            color = color.copy(alpha = 0.75f),
        )
    }
}

private fun Modifier.noRippleClick(onClick: () -> Unit): Modifier = this.then(
    Modifier.combinedClickable(
        interactionSource = null,
        indication = null,
        onClick = onClick,
    )
)

/** Hora en palabras, redondeada a 5 minutos. Español e inglés (resto de idiomas → inglés). */
internal fun timeInWords(time: Long, locale: Locale): String {
    val cal = Calendar.getInstance().apply { timeInMillis = time }
    var h = cal.get(Calendar.HOUR_OF_DAY)
    val m = ((cal.get(Calendar.MINUTE) + 2) / 5 * 5).let { if (it == 60) { h += 1; 0 } else it }
    return if (locale.language == "es") wordsEs(h % 24, m) else wordsEn(h % 24, m)
}

private val esHours = listOf("doce", "una", "dos", "tres", "cuatro", "cinco", "seis", "siete", "ocho", "nueve", "diez", "once")
private val esMinutes = mapOf(5 to "cinco", 10 to "diez", 15 to "cuarto", 20 to "veinte", 25 to "veinticinco", 30 to "media")

private fun wordsEs(h24: Int, m: Int): String {
    val toNext = m > 30
    val hour = (if (toNext) h24 + 1 else h24) % 12
    val name = esHours[hour]
    val prefix = if (hour == 1) "Es la" else "Son las"
    return when {
        m == 0 -> "$prefix $name en punto"
        !toNext -> "$prefix $name y ${esMinutes.getValue(m)}"
        else -> "$prefix $name menos ${esMinutes.getValue(60 - m)}"
    }
}

private val enHours = listOf("twelve", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "eleven")
private val enMinutes = mapOf(5 to "five", 10 to "ten", 15 to "quarter", 20 to "twenty", 25 to "twenty-five", 30 to "half")

private fun wordsEn(h24: Int, m: Int): String {
    val toNext = m > 30
    val name = enHours[(if (toNext) h24 + 1 else h24) % 12]
    return when {
        m == 0 -> "$name o'clock".replaceFirstChar { it.uppercase() }
        !toNext -> "${enMinutes.getValue(m)} past $name".replaceFirstChar { it.uppercase() }
        else -> "${enMinutes.getValue(60 - m)} to $name".replaceFirstChar { it.uppercase() }
    }
}
