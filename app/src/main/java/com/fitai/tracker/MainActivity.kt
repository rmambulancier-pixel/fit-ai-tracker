package com.fitai.tracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fitai.tracker.ui.theme.FitAiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.createChannel(this)
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

    val healthConnectPermissionLauncher = rememberLauncherForActivityResult(
    HealthConnectHelper.requestPermissionsContract()
) { grantedPermissions ->
    // Mettre à jour l'état de l'UI selon le résultat
    viewModel.onHealthPermissionsResult(grantedPermissions)
}


    val healthConnectPermissionLauncher = rememberLauncherForActivityResult(
        HealthConnectHelper.requestPermissionsContract()
    ) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    LaunchedEffect(viewModel.scanResult) {
        viewModel.scanResult?.let { result ->
            NotificationHelper.showScanResult(context, result)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            viewModel.scanMeal(bitmap)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) cameraLauncher.launch(null)
    }

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
                        Text("Dépense estimée (TDEE) : ${viewModel.estimatedTDEE} kcal/j")
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
                        if (viewModel.healthConnectAvailable) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    healthConnectPermissionLauncher.launch(HealthConnectHelper.permissions)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Autoriser Health Connect")
                            }
                        }
                    }
                }
            }

            if (viewModel.weightHistory.isNotEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Historique des pesées", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            viewModel.weightHistory.takeLast(5).reversed().forEach { record ->
                                Text("${TrackerViewModel.formatDate(record.timestamp)} — ${record.weight} kg")
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        val granted = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                        if (granted) {
                            cameraLauncher.launch(null)
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !viewModel.isScanning
                ) {
                    Text(if (viewModel.isScanning) "Analyse en cours…" else "📷 Scanner repas / boisson (Gemini Pro)")
                }
            }

            viewModel.scanResult?.let { result ->
                item {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Résultat du scan", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(result)
                        }
                    }
                }
            }

            if (viewModel.scanHistory.size > 1) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Historique des scans", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            viewModel.scanHistory.dropLast(1).takeLast(3).reversed().forEach { record ->
                                Text(
                                    "${TrackerViewModel.formatDate(record.timestamp)} — ${record.result.take(80)}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        }
                    }
                }
            }
        }
    }
} 
