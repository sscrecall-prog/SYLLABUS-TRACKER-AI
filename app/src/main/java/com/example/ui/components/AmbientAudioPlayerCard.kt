package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AmbientSoundType
import com.example.ui.theme.*

@Composable
fun AmbientAudioPlayerCard(
    currentSound: AmbientSoundType,
    isPlaying: Boolean,
    volume: Float,
    autoPlayWithTimer: Boolean,
    onSelectSound: (AmbientSoundType) -> Unit,
    onTogglePlayPause: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onToggleAutoPlay: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val activeColor = remember(currentSound) {
        try {
            Color(android.graphics.Color.parseColor(currentSound.colorHex))
        } catch (e: Exception) {
            BrandTerracotta
        }
    }

    // Audio Visualizer Wave animation
    val infiniteTransition = rememberInfiniteTransition(label = "audioWave")
    val wave1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "w1"
    )
    val wave2 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "w2"
    )
    val wave3 by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "w3"
    )
    val wave4 by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(520, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "w4"
    )

    BentoCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ambient_audio_player_card"),
        shape = RoundedCornerShape(22.dp),
        accentColor = if (isPlaying && currentSound != AmbientSoundType.NONE) activeColor else MaterialTheme.colorScheme.primary
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Main Top Bar: Icon, Active Ambience Name, Equalizer visualizer & Play/Pause
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isPlaying && currentSound != AmbientSoundType.NONE)
                                    activeColor.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                width = 1.dp,
                                color = if (isPlaying && currentSound != AmbientSoundType.NONE)
                                    activeColor.copy(alpha = 0.5f)
                                else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentSound.emoji,
                            fontSize = 22.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "AMBIENT FOCUS AUDIO",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPlaying && currentSound != AmbientSoundType.NONE) activeColor else MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.5.sp
                            )
                            if (isPlaying && currentSound != AmbientSoundType.NONE) {
                                Spacer(modifier = Modifier.width(6.dp))
                                // Live Equalizer Visualizer Bars
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    verticalAlignment = Alignment.Bottom,
                                    modifier = Modifier.height(12.dp)
                                ) {
                                    Box(modifier = Modifier.width(2.5.dp).height((12 * wave1).dp).clip(RoundedCornerShape(1.dp)).background(activeColor))
                                    Box(modifier = Modifier.width(2.5.dp).height((12 * wave2).dp).clip(RoundedCornerShape(1.dp)).background(activeColor))
                                    Box(modifier = Modifier.width(2.5.dp).height((12 * wave3).dp).clip(RoundedCornerShape(1.dp)).background(activeColor))
                                    Box(modifier = Modifier.width(2.5.dp).height((12 * wave4).dp).clip(RoundedCornerShape(1.dp)).background(activeColor))
                                }
                            }
                        }

                        Text(
                            text = if (currentSound == AmbientSoundType.NONE) "Sound Muted (Tap to Select)" else currentSound.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = currentSound.subtitle,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Play / Pause Circle Action Button
                FilledIconButton(
                    onClick = onTogglePlayPause,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(42.dp)
                        .testTag("toggle_ambient_audio_btn"),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (isPlaying && currentSound != AmbientSoundType.NONE) activeColor else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = if (isPlaying && currentSound != AmbientSoundType.NONE) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "Toggle Ambient Sound",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sound Preset Selector Horizontal Carousel
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ambient_sound_presets_row")
            ) {
                items(AmbientSoundType.values()) { sound ->
                    val isSelected = currentSound == sound
                    val itemColor = try {
                        Color(android.graphics.Color.parseColor(sound.colorHex))
                    } catch (e: Exception) {
                        BrandTerracotta
                    }

                    Surface(
                        onClick = { onSelectSound(sound) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) itemColor.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, itemColor) else null,
                        modifier = Modifier.testTag("ambient_preset_${sound.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = sound.emoji, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = sound.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) itemColor else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Volume Control & Auto-Sync Toggle Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = when {
                            volume == 0f -> Icons.Default.VolumeMute
                            volume < 0.5f -> Icons.Default.VolumeDown
                            else -> Icons.Default.VolumeUp
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Slider(
                        value = volume,
                        onValueChange = onVolumeChange,
                        valueRange = 0f..1f,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ambient_volume_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = if (isPlaying && currentSound != AmbientSoundType.NONE) activeColor else MaterialTheme.colorScheme.primary,
                            activeTrackColor = if (isPlaying && currentSound != AmbientSoundType.NONE) activeColor else MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "${(volume * 100).toInt()}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))
                VerticalDivider(modifier = Modifier.height(20.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.width(12.dp))

                // Auto-sync with timer toggle switch
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onToggleAutoPlay(!autoPlayWithTimer) }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                        .testTag("toggle_ambient_auto_sync")
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Timer Sync",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (autoPlayWithTimer) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (autoPlayWithTimer) "Auto Play" else "Manual",
                            fontSize = 8.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Switch(
                        checked = autoPlayWithTimer,
                        onCheckedChange = onToggleAutoPlay,
                        modifier = Modifier.size(width = 36.dp, height = 24.dp)
                    )
                }
            }
        }
    }
}
