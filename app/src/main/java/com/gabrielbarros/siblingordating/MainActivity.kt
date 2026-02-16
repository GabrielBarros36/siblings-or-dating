package com.gabrielbarros.siblingordating

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.gabrielbarros.siblingordating.data.PhotoEntry
import com.gabrielbarros.siblingordating.data.PhotoRepository
import com.gabrielbarros.siblingordating.ui.theme.SiblingOrDatingTheme
import java.io.File

class MainActivity : ComponentActivity() {

    private var sensorManager: SensorManager? = null
    private var shakeDetector: ShakeDetector? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SiblingOrDatingTheme {
                var currentIndex by remember { mutableIntStateOf(0) }
                var photos by remember { mutableStateOf(PhotoRepository.getShuffled()) }
                var showResult by remember { mutableStateOf(false) }
                var wasCorrect by remember { mutableStateOf(false) }
                var showUploadDialog by remember { mutableStateOf(false) }
                var score by remember { mutableIntStateOf(0) }
                var total by remember { mutableIntStateOf(0) }

                // Shake detector
                DisposableEffect(Unit) {
                    sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
                    shakeDetector = ShakeDetector {
                        if (!showResult && photos.isNotEmpty()) {
                            currentIndex = (currentIndex + 1) % photos.size
                        }
                    }
                    val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                    sensorManager?.registerListener(
                        shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_UI
                    )
                    onDispose {
                        sensorManager?.unregisterListener(shakeDetector)
                    }
                }

                val currentPhoto = if (photos.isNotEmpty()) photos[currentIndex % photos.size] else null

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GameScreen(
                        photo = currentPhoto,
                        showResult = showResult,
                        wasCorrect = wasCorrect,
                        score = score,
                        total = total,
                        onGuess = { guess ->
                            if (currentPhoto != null && !showResult) {
                                triggerHaptic()
                                wasCorrect = guess == currentPhoto.relationship
                                if (wasCorrect) score++
                                total++
                                showResult = true
                            }
                        },
                        onNext = {
                            showResult = false
                            currentIndex = (currentIndex + 1) % photos.size
                        },
                        onUploadClick = { showUploadDialog = true }
                    )
                }

                if (showUploadDialog) {
                    UploadDialog(
                        onDismiss = { showUploadDialog = false },
                        onUpload = { uri, relationship ->
                            val saved = saveImageToInternal(uri)
                            if (saved != null) {
                                val label = if (relationship == "dating") "Uploaded Couple" else "Uploaded Siblings"
                                PhotoRepository.addPhoto(label, relationship, saved)
                                photos = PhotoRepository.getShuffled()
                            }
                            showUploadDialog = false
                        }
                    )
                }
            }
        }
    }

    private fun triggerHaptic() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(50)
            }
        }
    }

    private fun saveImageToInternal(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val file = File(filesDir, "upload_${System.currentTimeMillis()}.jpg")
            file.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }
}

@Composable
fun GameScreen(
    photo: PhotoEntry?,
    showResult: Boolean,
    wasCorrect: Boolean,
    score: Int,
    total: Int,
    onGuess: (String) -> Unit,
    onNext: () -> Unit,
    onUploadClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Siblings or Dating?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onUploadClick) {
                Icon(Icons.Default.Add, contentDescription = "Upload photo")
            }
        }

        // Score
        if (total > 0) {
            Text(
                text = "Score: $score / $total",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Photo card
        if (photo != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (photo.imageUri != null) {
                        AsyncImage(
                            model = File(photo.imageUri),
                            contentDescription = "Photo to guess",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Placeholder for built-in photos
                        val bgColor = if (photo.relationship == "dating")
                            Color(0xFFFFCDD2) else Color(0xFFC8E6C9)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(bgColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = if (photo.relationship == "dating")
                                        Icons.Default.Favorite else Icons.Default.People,
                                    contentDescription = null,
                                    modifier = Modifier.size(80.dp),
                                    tint = if (photo.relationship == "dating")
                                        Color(0xFFE57373) else Color(0xFF81C784)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = photo.label,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF424242)
                                )
                                Text(
                                    text = "📷 Photo placeholder",
                                    fontSize = 14.sp,
                                    color = Color(0xFF757575),
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Result display
            AnimatedVisibility(
                visible = showResult,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (wasCorrect) Color(0xFF4CAF50) else Color(0xFFF44336)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (wasCorrect) "✅ Correct!" else "❌ Wrong!",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "They are ${photo.relationship}!",
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Buttons
            if (!showResult) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { onGuess("siblings") },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.People, contentDescription = null,
                            modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Siblings", fontSize = 16.sp)
                    }
                    Button(
                        onClick = { onGuess("dating") },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE91E63)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Favorite, contentDescription = null,
                            modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Dating", fontSize = 16.sp)
                    }
                }
            } else {
                Button(
                    onClick = onNext,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Next →", fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "📳 Shake to skip!",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No photos yet! Tap + to add some.", fontSize = 18.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadDialog(
    onDismiss: () -> Unit,
    onUpload: (Uri, String) -> Unit
) {
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedRelationship by remember { mutableStateOf("dating") }
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Upload a Photo") },
        text = {
            Column {
                Button(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (selectedUri != null) "✅ Photo selected" else "Choose Photo")
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Tag this photo:", fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedRelationship == "siblings",
                        onClick = { selectedRelationship = "siblings" },
                        label = { Text("👫 Siblings") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedRelationship == "dating",
                        onClick = { selectedRelationship = "dating" },
                        label = { Text("💑 Dating") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    selectedUri?.let { onUpload(it, selectedRelationship) }
                },
                enabled = selectedUri != null
            ) {
                Text("Upload")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
