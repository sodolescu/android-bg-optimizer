package com.example.androidbgoptimizer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidbgoptimizer.cache.*
import com.example.androidbgoptimizer.ui.theme.AndroidBGOptimizerTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidBGOptimizerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0A0E27)
                ) {
                    DashboardScreen()
                }
            }
        }
    }
}

@Composable
fun DashboardScreen() {
    val cache = remember { GoldenRatioCache(2048) }
    var stats by remember { mutableStateOf(cache.getStats()) }
    var hotProcs by remember { mutableStateOf(cache.getHotProcesses()) }
    var coldProcs by remember { mutableStateOf(cache.getColdProcesses()) }
    var events by remember { mutableStateOf(cache.getEventLog()) }
    var isSimulating by remember { mutableStateOf(false) }
    var simulationSpeed by remember { mutableStateOf(1) }
    val scope = rememberCoroutineScope()

    val mockApps = listOf(
        CacheProcess("gmail", "Gmail", "📧", 128, "COLD"),
        CacheProcess("chrome", "Chrome", "🌐", 256, "COLD"),
        CacheProcess("spotify", "Spotify", "🎵", 96, "COLD"),
        CacheProcess("maps", "Google Maps", "🗺️", 192, "COLD"),
        CacheProcess("youtube", "YouTube", "📺", 320, "COLD"),
        CacheProcess("whatsapp", "WhatsApp", "💬", 112, "COLD"),
        CacheProcess("instagram", "Instagram", "📸", 224, "COLD"),
        CacheProcess("twitter", "Twitter", "𝕏", 144, "COLD"),
        CacheProcess("reddit", "Reddit", "🔴", 160, "COLD"),
        CacheProcess("discord", "Discord", "💜", 176, "COLD"),
    )

    LaunchedEffect(isSimulating) {
        if (isSimulating) {
            while (isSimulating) {
                delay(2000L / simulationSpeed)
                val randomApp = mockApps.random()
                cache.admitProcess(randomApp)
                cache.touchProcess(randomApp.id)
                stats = cache.getStats()
                hotProcs = cache.getHotProcesses()
                coldProcs = cache.getColdProcesses()
                events = cache.getEventLog()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E27))
    ) {
        // Header
        HeaderBar(stats, isSimulating) {
            isSimulating = !isSimulating
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            // Left Sidebar
            LeftSidebar(
                hotProcs, coldProcs,
                onAdmitApp = { app ->
                    cache.admitProcess(app)
                    stats = cache.getStats()
                    hotProcs = cache.getHotProcesses()
                    coldProcs = cache.getColdProcesses()
                },
                onTouchProcess = { id ->
                    cache.touchProcess(id)
                    stats = cache.getStats()
                    hotProcs = cache.getHotProcesses()
                    coldProcs = cache.getColdProcesses()
                    events = cache.getEventLog()
                },
                onEvictProcess = { id ->
                    cache.evictProcess(id)
                    stats = cache.getStats()
                    hotProcs = cache.getHotProcesses()
                    coldProcs = cache.getColdProcesses()
                    events = cache.getEventLog()
                },
                mockApps = mockApps
            )

            // Center Content
            CenterContent(
                cache, stats, hotProcs, coldProcs, events,
                onTouchProcess = { id ->
                    cache.touchProcess(id)
                    stats = cache.getStats()
                    hotProcs = cache.getHotProcesses()
                    coldProcs = cache.getColdProcesses()
                    events = cache.getEventLog()
                }
            )

            // Right Sidebar
            RightSidebar(stats, cache)
        }
    }
}

@Composable
fun HeaderBar(stats: CacheStats, isSimulating: Boolean, onToggle: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        color = Color(0xFF0F1629),
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "φ",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC9A961),
                    modifier = Modifier.padding(end = 8.dp)
                )
                Column {
                    Text(
                        "Android Background Optimizer",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        "61.8% HOT · 38.2% COLD · φ = 1.618034",
                        fontSize = 10.sp,
                        color = Color(0xFF8B92A9),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatBadge("Hit Rate", "${(stats.hitRate * 100).toInt()}%")
                StatBadge("Memory", "${stats.totalUsedMB.toInt()}/${2048} MB")
                StatBadge("Processes", "${stats.totalCount}")

                Button(
                    onClick = onToggle,
                    modifier = Modifier.height(32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSimulating) Color(0xFF1E3A5F) else Color(0xFFC9A961)
                    )
                ) {
                    Icon(
                        if (isSimulating) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isSimulating) Color.White else Color(0xFF0A0E27)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (isSimulating) "Pause" else "Simulate",
                        fontSize = 11.sp,
                        color = if (isSimulating) Color.White else Color(0xFF0A0E27)
                    )
                }
            }
        }
    }
}

@Composable
fun StatBadge(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Text(
            label,
            fontSize = 9.sp,
            color = Color(0xFF8B92A9),
            fontFamily = FontFamily.Monospace
        )
        Text(
            value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun LeftSidebar(
    hotProcs: List<CacheProcess>,
    coldProcs: List<CacheProcess>,
    onAdmitApp: (CacheProcess) -> Unit,
    onTouchProcess: (String) -> Unit,
    onEvictProcess: (String) -> Unit,
    mockApps: List<CacheProcess>
) {
    Surface(
        modifier = Modifier
            .width(280.dp)
            .fillMaxHeight(),
        color = Color(0xFF0F1629),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "Process Registry",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            )

            if (hotProcs.isEmpty() && coldProcs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "φ",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC9A961),
                            modifier = Modifier.alpha(0.3f)
                        )
                        Text(
                            "CACHE EMPTY",
                            fontSize = 10.sp,
                            color = Color(0xFF8B92A9),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            } else {
                if (hotProcs.isNotEmpty()) {
                    Text(
                        "🔥 HOT (${hotProcs.size})",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE8A76A),
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp)
                    )
                    hotProcs.forEach { proc ->
                        ProcessItem(proc, onTouchProcess, onEvictProcess)
                    }
                }

                if (coldProcs.isNotEmpty()) {
                    Text(
                        "❄️ COLD (${coldProcs.size})",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5BA3D0),
                        modifier = Modifier.padding(start = 16.dp, top = 12.dp)
                    )
                    coldProcs.forEach { proc ->
                        ProcessItem(proc, onTouchProcess, onEvictProcess)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Divider(color = Color(0xFF1E2A47))

            Button(
                onClick = { onAdmitApp(mockApps.random()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E3A5F)
                )
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Random App", fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun ProcessItem(
    proc: CacheProcess,
    onTouch: (String) -> Unit,
    onEvict: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(proc.icon, fontSize = 16.sp)
        Column(modifier = Modifier.weight(1f)) {
            Text(proc.name, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
            Text(
                "${proc.memoryMB}MB · ×${proc.accessCount}",
                fontSize = 9.sp,
                color = Color(0xFF8B92A9),
                fontFamily = FontFamily.Monospace
            )
        }
        IconButton(onClick = { onTouch(proc.id) }, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(12.dp))
        }
        IconButton(onClick = { onEvict(proc.id) }, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(12.dp))
        }
    }
}

@Composable
fun CenterContent(
    cache: GoldenRatioCache,
    stats: CacheStats,
    hotProcs: List<CacheProcess>,
    coldProcs: List<CacheProcess>,
    events: List<CacheEvent>,
    onTouchProcess: (String) -> Unit
) {
    val (hotCap, coldCap) = cache.computeCapacities()

    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .background(Color(0xFF0A0E27))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard("Hit Rate", "${(stats.hitRate * 100).toInt()}%", Color(0xFFC9A961), Modifier.weight(1f))
            StatCard("Memory Pressure", "${(stats.memoryPressure * 100).toInt()}%", Color(0xFFE8A76A), Modifier.weight(1f))
            StatCard("Promotions", "${stats.promotions}", Color(0xFF5BA3D0), Modifier.weight(1f))
            StatCard("Evictions", "${stats.evictedCount}", Color(0xFFE85D75), Modifier.weight(1f))
        }

        // Segment Utilization
        SegmentCard(
            "HOT Segment",
            stats.hotUsedMB.toInt(),
            hotCap,
            Color(0xFFE8A76A),
            hotProcs.size
        )
        SegmentCard(
            "COLD Segment",
            stats.coldUsedMB.toInt(),
            coldCap,
            Color(0xFF5BA3D0),
            coldProcs.size
        )

        // Phi Visualization
        PhiCard(stats, hotCap, coldCap)

        // Event Log
        EventLogCard(events)
    }
}

@Composable
fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .height(80.dp),
        color = Color(0xFF0F1629),
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 9.sp, color = Color(0xFF8B92A9), fontFamily = FontFamily.Monospace)
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun SegmentCard(label: String, used: Int, capacity: Int, color: Color, count: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF0F1629),
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = color)
                Text("$used / $capacity MB", fontSize = 9.sp, color = Color(0xFF8B92A9), fontFamily = FontFamily.Monospace)
            }
            LinearProgressIndicator(
                progress = (used.toFloat() / capacity).coerceIn(0f, 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .padding(top = 8.dp),
                color = color,
                trackColor = Color(0xFF1E2A47)
            )
            Text(
                "${(used.toFloat() / capacity * 100).toInt()}% used · $count procs",
                fontSize = 8.sp,
                color = Color(0xFF8B92A9),
                modifier = Modifier.padding(top = 4.dp),
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun PhiCard(stats: CacheStats, hotCap: Int, coldCap: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF0F1629),
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Golden Ratio Partition", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                Text("φ = 1.618034", fontSize = 10.sp, color = Color(0xFFC9A961), fontFamily = FontFamily.Monospace)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("HOT", fontSize = 10.sp, color = Color(0xFFE8A76A), fontWeight = FontWeight.Bold)
                    Text("${(hotCap.toFloat() / (hotCap + coldCap) * 100).toInt()}%", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE8A76A))
                    Text("$hotCap MB", fontSize = 9.sp, color = Color(0xFF8B92A9), fontFamily = FontFamily.Monospace)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("COLD", fontSize = 10.sp, color = Color(0xFF5BA3D0), fontWeight = FontWeight.Bold)
                    Text("${(coldCap.toFloat() / (hotCap + coldCap) * 100).toInt()}%", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5BA3D0))
                    Text("$coldCap MB", fontSize = 9.sp, color = Color(0xFF8B92A9), fontFamily = FontFamily.Monospace)
                }
            }

            Text(
                "C/φ = 2048/1.618 ≈ 1265.7 MB HOT + 782.3 MB COLD",
                fontSize = 8.sp,
                color = Color(0xFF8B92A9),
                modifier = Modifier.padding(top = 12.dp),
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun EventLogCard(events: List<CacheEvent>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF0F1629),
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Event Log", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            if (events.isEmpty()) {
                Text(
                    "No events yet",
                    fontSize = 9.sp,
                    color = Color(0xFF8B92A9),
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else {
                events.takeLast(5).forEach { evt ->
                    Text(
                        "[${evt.type}] ${evt.processName}: ${evt.detail}",
                        fontSize = 8.sp,
                        color = Color(0xFF8B92A9),
                        modifier = Modifier.padding(top = 4.dp),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun RightSidebar(stats: CacheStats, cache: GoldenRatioCache) {
    Surface(
        modifier = Modifier
            .width(240.dp)
            .fillMaxHeight(),
        color = Color(0xFF0F1629),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Live Metrics", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)

            MetricRow("φ Ratio (HOT/COLD)", if (stats.phiRatio == PHI || !stats.phiRatio.isFinite()) "—" else "%.3f".format(stats.phiRatio), Color(0xFFC9A961))
            MetricRow("HOT Processes", "${stats.hotCount}", Color(0xFFE8A76A))
            MetricRow("COLD Processes", "${stats.coldCount}", Color(0xFF5BA3D0))
            MetricRow("Cache Hits", "${stats.cacheHits}", Color(0xFF5BA3D0))
            MetricRow("Cache Misses", "${stats.cacheMisses}", Color(0xFFE85D75))
            MetricRow("Promotions", "${stats.promotions}", Color(0xFF5BA3D0))
            MetricRow("Demotions", "${stats.demotions}", Color(0xFFB8A0D0))
            MetricRow("Evictions", "${stats.evictedCount}", Color(0xFFE85D75))
        }
    }
}

@Composable
fun MetricRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 9.sp, color = Color(0xFF8B92A9), fontFamily = FontFamily.Monospace)
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color, fontFamily = FontFamily.Monospace)
    }
}
