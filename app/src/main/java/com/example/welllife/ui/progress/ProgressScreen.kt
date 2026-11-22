package com.example.welllife.ui.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.welllife.data.repository.DailyProgress
import java.time.format.DateTimeFormatter

@Composable
fun ProgressScreen(progress: List<DailyProgress>, modifier: Modifier = Modifier) {
    val formatter = DateTimeFormatter.ofPattern("EEE, MMM d")
    LazyColumn(
        modifier = modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { Text("Your last seven days") }
        items(progress, key = { it.date.toString() }) { day ->
            ProgressDayCard(day, formatter)
        }
    }
}

@Composable
private fun ProgressDayCard(day: DailyProgress, formatter: DateTimeFormatter) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(day.date.format(formatter))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Water: ${day.waterMl} mL")
                Text("Activity: ${day.activityMinutes} min")
            }
            Text(
                text = day.sleepMinutes?.let { "Sleep: $it min" } ?: "Sleep: No data"
            )
        }
    }
}
