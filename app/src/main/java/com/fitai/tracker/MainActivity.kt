package com.fitai.tracker

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fitai.tracker.ui.theme.FitAiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FitAiTheme {
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
fun TrackerDashboard(viewModel: TrackerViewModel = viewModel()) {
    val context = LocalContext.current

    Scaffold(
        topBar = { TopAppBar(title = { Text("Fit AI Metabolic Engine") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Métabolisme en direct", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Dépense réelle (TDEE) : ${viewModel.estimatedTDEE} kcal/j")
                        Text("Poids lissé (Trend) : ${"%.1f".format(viewModel.trendWeight)} kg")
                        Text("Dernière pesée brute : ${viewModel.lastRawWeight} kg")
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Ajouter une pesée", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = viewModel.rawWeightInput,
                            onValueChange = { viewModel.onWeightInputChange(it) },
                            label = { Text("Poids du jour (kg)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.logWeight() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Enregistrer la pesée")
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        Toast.makeText(
                            context,
                            "Scan Gemini pas encore branché — il faudra une clé API Gemini",
                            Toast.LENGTH_LONG
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("📷 Scanner repas / boisson (Gemini Pro)")
                }
            }
        }
    }
} 
