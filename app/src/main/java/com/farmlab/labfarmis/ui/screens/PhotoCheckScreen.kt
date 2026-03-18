package com.farmlab.labfarmis.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.farmlab.labfarmis.ui.components.HealthBadge
import com.farmlab.labfarmis.ui.components.formatDateTime
import com.farmlab.labfarmis.ui.theme.*
import com.farmlab.labfarmis.viewmodel.FarmViewModel
import com.farmlab.labfarmis.data.model.HealthStatus

data class DiagnosisResult(
    val overallStatus: HealthStatus,
    val score: Int,
    val findings: List<Finding>
)

data class Finding(
    val category: String,
    val status: String,
    val description: String,
    val severity: Severity
)

enum class Severity { OK, MILD, MODERATE, SEVERE }

@Composable
fun PhotoCheckScreen(
    viewModel: FarmViewModel,
    navController: NavController,
    innerPadding: PaddingValues
) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var diagnosisResult by remember { mutableStateOf<DiagnosisResult?>(null) }
    var analysisTimestamp by remember { mutableStateOf(0L) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        diagnosisResult = null
    }

    // Simulated analysis
    LaunchedEffect(isAnalyzing) {
        if (isAnalyzing) {
            kotlinx.coroutines.delay(2500)
            diagnosisResult = simulateDiagnosis()
            analysisTimestamp = System.currentTimeMillis()
            isAnalyzing = false
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = innerPadding.calculateTopPadding(),
            bottom = innerPadding.calculateBottomPadding() + 16.dp
        )
    ) {
        item {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(Brush.linearGradient(listOf(FarmBrown, FarmBrownLight)))
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                    Column {
                        Text("Photo Check", style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold, color = Color.White)
                        Text("AI-powered bird health diagnostics",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f))
                    }
                }
            }
        }

        // Image picker area
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(260.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(listOf(FarmGreen, FarmGreenLight)),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { if (!isAnalyzing) launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Selected bird photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Overlay
                    Box(
                        modifier = Modifier.fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.25f))
                    )
                    Box(
                        modifier = Modifier.align(Alignment.BottomCenter)
                            .padding(16.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.Black.copy(alpha = 0.6f)
                        ) {
                            Text(
                                "Tap to change photo",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White
                            )
                        }
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(80.dp).clip(CircleShape)
                                .background(FarmGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CameraAlt, null, tint = FarmGreen,
                                modifier = Modifier.size(44.dp))
                        }
                        Text("Tap to select a photo", style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold)
                        Text("Take or pick a photo of your bird\nfor health analysis",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center)
                    }
                }
            }
        }

        // Analyze button
        if (selectedImageUri != null) {
            item {
                Button(
                    onClick = { isAnalyzing = true },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    enabled = !isAnalyzing,
                    colors = ButtonDefaults.buttonColors(containerColor = FarmGreen),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Analyzing...", style = MaterialTheme.typography.titleSmall)
                    } else {
                        Icon(Icons.Default.Search, null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Analyze Bird Health", style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
        }

        // Results
        diagnosisResult?.let { result ->
            item {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Diagnosis Results", style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold)
                        Text(formatDateTime(analysisTimestamp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    // Overall result card
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when (result.overallStatus) {
                                HealthStatus.HEALTHY -> Color(0xFFD1FAE5)
                                HealthStatus.WARNING -> FarmOrangeContainer
                                HealthStatus.RISK -> FarmRedContainer
                            }
                        ),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = when (result.overallStatus) {
                                    HealthStatus.HEALTHY -> "✅"
                                    HealthStatus.WARNING -> "⚠️"
                                    HealthStatus.RISK -> "🚨"
                                },
                                fontSize = 36.sp
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = when (result.overallStatus) {
                                        HealthStatus.HEALTHY -> "Bird appears healthy"
                                        HealthStatus.WARNING -> "Some concerns detected"
                                        HealthStatus.RISK -> "Immediate attention needed"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("Confidence Score: ${result.score}%",
                                    style = MaterialTheme.typography.bodySmall)
                            }
                            HealthBadge(result.overallStatus)
                        }
                    }

                    // Individual findings
                    result.findings.forEach { finding ->
                        FindingCard(finding = finding)
                    }

                    // Disclaimer
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Info, null, tint = FarmBrown,
                                modifier = Modifier.size(18.dp))
                            Text(
                                "This analysis is for guidance only. Consult a veterinarian for accurate diagnosis.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // How it works
        if (diagnosisResult == null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = FarmGreenContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("How Photo Check Works", style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold, color = FarmGreenDark)
                        val steps = listOf(
                            "📸 Take or select a clear photo of your bird",
                            "🔍 Our system analyzes posture, feathers, and eyes",
                            "📊 Get a detailed health assessment report",
                            "💡 Receive actionable recommendations"
                        )
                        steps.forEach { step ->
                            Text(step, style = MaterialTheme.typography.bodySmall, color = FarmGreenDark)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FindingCard(finding: Finding) {
    val (bgColor, iconColor) = when (finding.severity) {
        Severity.OK -> Pair(Color(0xFFD1FAE5), Color(0xFF059669))
        Severity.MILD -> Pair(FarmYellowContainer, FarmYellow.copy(red = 0.7f))
        Severity.MODERATE -> Pair(FarmOrangeContainer, FarmOrange)
        Severity.SEVERE -> Pair(FarmRedContainer, FarmRed)
    }
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = when (finding.severity) {
                    Severity.OK -> Icons.Default.CheckCircle
                    Severity.MILD -> Icons.Default.Info
                    Severity.MODERATE -> Icons.Default.Warning
                    Severity.SEVERE -> Icons.Default.Error
                },
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(finding.category, style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold)
                    Text(finding.status, style = MaterialTheme.typography.labelSmall,
                        color = iconColor, fontWeight = FontWeight.SemiBold)
                }
                Text(finding.description, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private fun simulateDiagnosis(): DiagnosisResult {
    val rng = java.util.Random()
    val rand = rng.nextInt(100)

    return when {
        rand < 65 -> DiagnosisResult(
            overallStatus = HealthStatus.HEALTHY,
            score = 85 + rng.nextInt(14),
            findings = listOf(
                Finding("Feather Condition", "Normal", "Plumage appears full and well-maintained with good sheen", Severity.OK),
                Finding("Posture & Movement", "Normal", "Upright posture indicates good muscle tone and alertness", Severity.OK),
                Finding("Eyes & Beak", "Clear", "Eyes are bright and clear, beak shows no abnormalities", Severity.OK),
                Finding("Respiratory", "Normal", "No signs of labored breathing or nasal discharge detected", Severity.OK),
                Finding("Leg & Foot", "Healthy", "Legs appear normal with no swelling or discoloration", Severity.OK)
            )
        )
        rand < 85 -> DiagnosisResult(
            overallStatus = HealthStatus.WARNING,
            score = 58 + rng.nextInt(20),
            findings = listOf(
                Finding("Feather Condition", "Mild Loss", "Some feather thinning observed — possible molting or mild stress", Severity.MILD),
                Finding("Posture & Movement", "Slightly hunched", "Slight lethargy noted — monitor food and water intake", Severity.MILD),
                Finding("Eyes & Beak", "Clear", "Eyes appear normal, no discharge detected", Severity.OK),
                Finding("Respiratory", "Normal", "No respiratory distress observed", Severity.OK),
                Finding("Leg & Foot", "Minor Scaling", "Mild scaling on legs — consider mite treatment", Severity.MILD)
            )
        )
        else -> DiagnosisResult(
            overallStatus = HealthStatus.RISK,
            score = 30 + rng.nextInt(25),
            findings = listOf(
                Finding("Feather Condition", "Significant Loss", "Extensive feather loss may indicate disease or severe stress", Severity.SEVERE),
                Finding("Posture & Movement", "Hunched/Lethargic", "Bird showing signs of significant weakness — isolate immediately", Severity.SEVERE),
                Finding("Eyes & Beak", "Discharge Detected", "Possible eye/nasal discharge — could indicate respiratory infection", Severity.MODERATE),
                Finding("Respiratory", "Abnormal", "Possible labored breathing detected — consult vet urgently", Severity.SEVERE),
                Finding("Leg & Foot", "Swelling Detected", "Significant leg swelling — possible Marek's disease or injury", Severity.MODERATE)
            )
        )
    }
}
