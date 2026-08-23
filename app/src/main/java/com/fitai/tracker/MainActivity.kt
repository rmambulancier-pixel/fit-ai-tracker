package com.fitai.tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TrackerDashboard()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerDashboard() {
    var rawWeight by remember { mutableStateOf("82.4") }
    var trendWeight by remember { mutableStateOf("82.1") }
    var estimatedTDEE by remember { mutableStateOf("2450") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Fit AI Metabolic Engine") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Métabolisme en direct", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Dépense réelle (TDEE) : $estimatedTDEE kcal/j")
                        Text("Poids lissé (Trend) : $trendWeight kg")
                        Text("Dernière pesée brute : $rawWeight kg")
                    }
                }
            }
            item {
                Button(
                    onClick = { /* Action Scanner Repas avec Gemini */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("📷 Scanner repas / boisson (Gemini Pro)")
                }
            }
        }
    }
}
