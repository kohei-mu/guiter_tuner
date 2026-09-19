package com.example.guitartuner.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.guitartuner.tuner.GuitarString
import com.example.guitartuner.tuner.GuitarTuning
import com.example.guitartuner.tuner.TunerState
import com.example.guitartuner.tuner.TuningStatus
import java.util.Locale

@Composable
fun TunerScreen(
    state: TunerState,
    onStringSelected: (GuitarString) -> Unit,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Surface(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Guitar Tuner", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Standard tuning · A4 = 440 Hz", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(26.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                GuitarTuning.standard.forEach { string ->
                    FilterChip(
                        selected = state.selectedString == string,
                        onClick = { onStringSelected(string) },
                        label = { Text("${string.number}:${string.note.first()}") },
                    )
                }
            }
            Spacer(Modifier.height(24.dp))

            if (!state.hasMicrophonePermission) {
                PermissionCard(state.permissionDenied, onRequestPermission, onOpenSettings)
            } else if (state.selectedString == null) {
                MessageCard("チューニングする弦を選択してください")
            } else {
                TuningPanel(state)
            }
        }
    }
}

@Composable
private fun PermissionCard(denied: Boolean, onRequest: () -> Unit, onSettings: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("マイクへのアクセスが必要です", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("ギターの音程を測定するためにマイクを使用します。録音した音声は保存されません。", textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            if (denied) {
                OutlinedButton(onClick = onSettings) { Text("設定を開く") }
                Spacer(Modifier.height(6.dp))
                Button(onClick = onRequest) { Text("もう一度許可する") }
            } else {
                Button(onClick = onRequest) { Text("マイクを許可") }
            }
        }
    }
}

@Composable
private fun MessageCard(message: String) {
    Card(Modifier.fillMaxWidth()) {
        Text(message, Modifier.padding(28.dp).fillMaxWidth(), textAlign = TextAlign.Center)
    }
}

@Composable
private fun TuningPanel(state: TunerState) {
    val string = requireNotNull(state.selectedString)
    val message = when {
        state.isOutOfRange -> "選択した弦を鳴らしてください"
        state.status == TuningStatus.WAITING -> "弦を鳴らしてください"
        state.status == TuningStatus.DETECTING -> "音程を検出しています…"
        state.status == TuningStatus.LOW -> "LOW"
        state.status == TuningStatus.IN_TUNE -> "OK"
        state.status == TuningStatus.HIGH -> "HIGH"
        else -> ""
    }
    val statusColor = when (state.status) {
        TuningStatus.IN_TUNE -> Color(0xFF178447)
        TuningStatus.LOW, TuningStatus.HIGH -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("${string.number}弦", style = MaterialTheme.typography.titleMedium)
            Text(string.note, fontSize = 64.sp, fontWeight = FontWeight.Bold)
            Text(message, color = statusColor, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            TuningMeter(state.meterCents, update = state.cents != null && !state.isOutOfRange)
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                ValueLabel("検出", state.detectedFrequencyHz?.let { formatHz(it) } ?: "-- Hz")
                ValueLabel("基準", formatHz(string.frequencyHz))
                ValueLabel("ずれ", state.cents?.let { String.format(Locale.US, "%+.1f ¢", it) } ?: "-- ¢")
            }
        }
    }
}

@Composable
private fun ValueLabel(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun TuningMeter(cents: Float, update: Boolean) {
    val needleColor = MaterialTheme.colorScheme.primary
    Column(Modifier.fillMaxWidth()) {
        Canvas(Modifier.fillMaxWidth().height(100.dp)) {
            val centerY = size.height * 0.62f
            drawLine(Color(0xFF9CA0AC), Offset(0f, centerY), Offset(size.width, centerY), 5f, StrokeCap.Round)
            for (mark in -5..5) {
                val x = size.width * (mark + 5) / 10f
                val tall = mark == 0
                drawLine(
                    if (tall) Color(0xFF178447) else Color(0xFF777B87),
                    Offset(x, centerY - if (tall) 25f else 12f),
                    Offset(x, centerY + if (tall) 25f else 12f),
                    if (tall) 5f else 3f,
                )
            }
            if (update) {
                val x = size.width * ((cents.coerceIn(-50f, 50f) + 50f) / 100f)
                drawLine(needleColor, Offset(x, centerY - 42f), Offset(x, centerY + 30f), 8f, StrokeCap.Round)
                drawCircle(needleColor, 14f, Offset(x, centerY - 42f))
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("−50", style = MaterialTheme.typography.labelSmall)
            Text("0 cents", style = MaterialTheme.typography.labelSmall)
            Text("+50", style = MaterialTheme.typography.labelSmall)
        }
    }
}

private fun formatHz(value: Double) = String.format(Locale.US, "%.2f Hz", value)
