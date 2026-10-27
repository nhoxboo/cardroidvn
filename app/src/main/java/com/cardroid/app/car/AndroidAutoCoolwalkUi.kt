package com.cardroid.app.car

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.view.KeyEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Android Auto Coolwalk UI emulation for CarDroid.
 * Renders a standard automotive split layout:
 * - Left/Bottom Dock (App Launcher, Assistant, Clock, Status)
 * - Main Panel (Navigation / Map application)
 * - Secondary Panel (Now Playing Media Card + Quick Widgets)
 */
@Composable
internal fun AndroidAutoCoolwalkScreen(
    apps: List<CarApp>,
    prefs: CarLauncherPrefs,
    visible: Boolean = true,
    onLaunch: (CarApp) -> Unit,
) {
    var showAppDrawer by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var currentTime by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        while (true) {
            currentTime = format.format(Date())
            delay(1000)
        }
    }

    // Identify navigation app and media app from installed apps
    val navApp = remember(apps, prefs) {
        apps.firstOrNull { it.packageName.contains("map") || it.packageName.contains("nav") || it.packageName.contains("vietmap") || it.packageName.contains("waze") }
            ?: apps.firstOrNull()
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1015))
    ) {
        Row(Modifier.fillMaxSize()) {
            // Coolwalk Side Dock (Automotive standard for Left-Hand Drive)
            CoolwalkDock(
                time = currentTime,
                onToggleAppDrawer = { showAppDrawer = !showAppDrawer },
                onVoiceAssistant = {
                    val intent = Intent(Intent.ACTION_VOICE_COMMAND).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    runCatching { context.startActivity(intent) }
                }
            )

            // Main Content Area: Split Navigation & Media Card
            Row(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Primary Navigation / Main App Area (65% width)
                Box(
                    Modifier
                        .weight(0.65f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF181920))
                        .border(1.dp, Color(0xFF2A2B35), RoundedCornerShape(20.dp))
                ) {
                    if (navApp != null) {
                        DashboardHome(apps, prefs, visible)
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Bản đồ điều hướng",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 18.sp
                            )
                        }
                    }
                }

                // Secondary Area: Now Playing Media Card + Quick Actions (35% width)
                Column(
                    Modifier
                        .weight(0.35f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Media Card
                    CoolwalkMediaCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        context = context
                    )

                    // Quick Action Card (Call / Notification / App Drawer Trigger)
                    CoolwalkQuickActionCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        onOpenApps = { showAppDrawer = true }
                    )
                }
            }
        }

        // App Drawer Modal Overlay
        AnimatedVisibility(
            visible = showAppDrawer,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .clickable(remember { MutableInteractionSource() }, null) { showAppDrawer = false }
                    .padding(32.dp)
            ) {
                Column(Modifier.fillMaxSize()) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tất cả ứng dụng",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2A2B35))
                                .clickable { showAppDrawer = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✕", color = Color.White, fontSize = 18.sp)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    AppGrid(
                        apps = apps,
                        prefs = prefs,
                        onLaunch = {
                            showAppDrawer = false
                            onLaunch(it)
                        },
                        onLongPress = { _, _ -> }
                    )
                }
            }
        }
    }
}

@Composable
private fun CoolwalkDock(
    time: String,
    onToggleAppDrawer: () -> Unit,
    onVoiceAssistant: () -> Unit,
) {
    Column(
        Modifier
            .width(68.dp)
            .fillMaxHeight()
            .background(Color(0xFF14151B))
            .border(1.dp, Color(0xFF22232C), RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp))
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // App Launcher Icon (9 dots)
        Box(
            Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFF232530))
                .clickable(onClick = onToggleAppDrawer),
            contentAlignment = Alignment.Center
        ) {
            Text("⊞", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        // Center Voice Assistant Button
        Box(
            Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E2842))
                .clickable(onClick = onVoiceAssistant),
            contentAlignment = Alignment.Center
        ) {
            Text("🎙", fontSize = 20.sp)
        }

        // Bottom Clock & Status
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = time,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "CarPlay",
                color = Color(0xFF3880FF),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun CoolwalkMediaCard(
    modifier: Modifier = Modifier,
    context: Context,
) {
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager }

    Box(
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF1A1C24))
            .border(1.dp, Color(0xFF2A2B35), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2B2E3C)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🎵", fontSize = 24.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Đang phát đa phương tiện",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Âm thanh truyền trực tiếp qua xe",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
            }

            // Media Controls
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous Track
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF252734))
                        .clickable {
                            dispatchMediaKey(audioManager, KeyEvent.KEYCODE_MEDIA_PREVIOUS)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("⏮", color = Color.White, fontSize = 18.sp)
                }

                // Play / Pause Track
                Box(
                    Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3880FF))
                        .clickable {
                            dispatchMediaKey(audioManager, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("⏯", color = Color.White, fontSize = 22.sp)
                }

                // Next Track
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF252734))
                        .clickable {
                            dispatchMediaKey(audioManager, KeyEvent.KEYCODE_MEDIA_NEXT)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("⏭", color = Color.White, fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
private fun CoolwalkQuickActionCard(
    modifier: Modifier = Modifier,
    onOpenApps: () -> Unit,
) {
    Box(
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF1A1C24))
            .border(1.dp, Color(0xFF2A2B35), RoundedCornerShape(20.dp))
            .clickable(onClick = onOpenApps)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Giao thức Android Auto → CarPlay",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Chạm để duyệt toàn bộ ứng dụng",
                    color = Color(0xFF3880FF),
                    fontSize = 12.sp
                )
            }
            Text("→", color = Color.White, fontSize = 20.sp)
        }
    }
}

private fun dispatchMediaKey(audioManager: AudioManager?, keyCode: Int) {
    if (audioManager == null) return
    val down = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
    val up = KeyEvent(KeyEvent.ACTION_UP, keyCode)
    audioManager.dispatchMediaKeyEvent(down)
    audioManager.dispatchMediaKeyEvent(up)
}
