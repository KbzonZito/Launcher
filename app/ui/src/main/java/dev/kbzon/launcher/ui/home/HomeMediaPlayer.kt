package dev.kbzon.launcher.ui.home

import dev.kbzon.launcher.ui.theme.typography.fontfamily.LocalAppFontFamily
import android.app.ActivityOptions
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.kbzon.launcher.music.MusicService
import dev.kbzon.launcher.music.PlaybackState
import dev.kbzon.launcher.ui.R
import org.koin.compose.koinInject

/**
 * [fork] Mini reproductor de la home: solo aparece si hay algo sonando o en pausa.
 * Requiere acceso a notificaciones (el mismo permiso que el widget de música de Kvaesitso).
 */
@Composable
fun HomeMediaPlayer(shadow: Shadow?, modifier: Modifier = Modifier) {
    val music: MusicService = koinInject()
    val state by music.playbackState.collectAsState(PlaybackState.Stopped)
    val title by music.title.collectAsState(null)
    val artist by music.artist.collectAsState(null)
    val art by music.albumArt.collectAsState(null)
    val dim = LocalContentColor.current.copy(alpha = 0.7f)

    AnimatedVisibility(
        visible = state != PlaybackState.Stopped && !title.isNullOrBlank(),
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .combinedClickable(
                        interactionSource = null,
                        indication = null,
                        onClick = {
                            val options = if (Build.VERSION.SDK_INT >= 34) {
                                ActivityOptions.makeBasic()
                                    .setPendingIntentBackgroundActivityStartMode(ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED)
                                    .toBundle()
                            } else null
                            try {
                                music.openPlayer()?.send(options)
                            } catch (_: Exception) {
                            }
                        },
                    ),
            ) {
                art?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)),
                    )
                    Spacer(Modifier.width(10.dp))
                }
                Column(Modifier.weight(1f, fill = false)) {
                    Text(
                        title.orEmpty(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = LocalAppFontFamily.current, fontSize = 15.sp, fontWeight = FontWeight.Medium, shadow = shadow),
                    )
                    artist?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            it,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = dim,
                            style = TextStyle(fontFamily = LocalAppFontFamily.current, fontSize = 13.sp, shadow = shadow),
                        )
                    }
                }
            }
            IconButton(onClick = { music.previous() }, modifier = Modifier.size(40.dp)) {
                Icon(painterResource(R.drawable.skip_previous_24px), contentDescription = null, modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = { music.togglePause() }, modifier = Modifier.size(40.dp)) {
                Icon(
                    painterResource(if (state == PlaybackState.Playing) R.drawable.pause_24px_filled else R.drawable.play_arrow_24px),
                    contentDescription = null,
                )
            }
            IconButton(onClick = { music.next() }, modifier = Modifier.size(40.dp)) {
                Icon(painterResource(R.drawable.skip_next_24px), contentDescription = null, modifier = Modifier.size(20.dp))
            }
        }
    }
}
