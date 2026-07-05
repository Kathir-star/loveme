package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.StyleHistoryEntity
import com.example.data.models.*
import com.example.ui.components.OverlayDrawers
import com.example.ui.viewmodel.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Safe Helper to load image Uris into Bitmaps on any Android version.
 */
fun loadUriBitmap(context: Context, uri: Uri): Bitmap? {
    return try {
        val contentResolver = context.contentResolver
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = android.graphics.ImageDecoder.createSource(contentResolver, uri)
            android.graphics.ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.isMutableRequired = true
            }
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        }
    } catch (e: Exception) {
        Log.e("StyleStudioScreens", "Error loading Uri bitmap", e)
        null
    }
}

/**
 * Root Composable orchestrating view states and navigation.
 */
@Composable
fun StyleStudioApp(viewModel: StyleViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val history by viewModel.historyList.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (uiState.currentScreen) {
                AppScreen.Home -> HomeScreen(
                    historyCount = history.size,
                    onStartAnalysis = { viewModel.navigateTo(AppScreen.Upload) },
                    onNavigateToHistory = { viewModel.navigateTo(AppScreen.History) }
                )
                AppScreen.Upload -> UploadScreen(
                    selfie = uiState.selfieBitmap,
                    fullBody = uiState.fullBodyBitmap,
                    onBack = { viewModel.navigateTo(AppScreen.Home) },
                    onNext = { viewModel.navigateTo(AppScreen.Preferences) },
                    onSelfieSelected = { viewModel.setSelfie(it) },
                    onFullBodySelected = { viewModel.setFullBodyImage(it) }
                )
                AppScreen.Preferences -> PreferencesScreen(
                    gender = uiState.gender,
                    style = uiState.stylePref,
                    occasion = uiState.occasionPref,
                    budget = uiState.budgetPref,
                    isDemoMode = uiState.isDemoMode,
                    isLoading = uiState.isLoadingAnalysis,
                    error = uiState.analysisError,
                    onBack = { viewModel.navigateTo(AppScreen.Upload) },
                    onPreferenceChange = { g, s, o, b -> viewModel.updatePreferences(g, s, o, b) },
                    onRunAnalysis = { viewModel.runStylingAnalysis() }
                )
                AppScreen.Results -> ResultsScreen(
                    report = uiState.activeReport,
                    isDemoMode = uiState.isDemoMode,
                    onBack = { viewModel.navigateTo(AppScreen.Preferences) },
                    onReset = { viewModel.resetStudio() },
                    onOpenStylingRoom = { viewModel.navigateTo(AppScreen.StylingRoom) }
                )
                AppScreen.StylingRoom -> StylingRoomScreen(
                    selfie = uiState.selfieBitmap,
                    report = uiState.activeReport,
                    overlayState = uiState.overlayState,
                    onBack = { viewModel.navigateTo(AppScreen.Results) },
                    onSelectHairstyle = { viewModel.selectOverlayHairstyle(it) },
                    onSelectGlasses = { viewModel.selectOverlayGlasses(it) },
                    onUpdateHairColor = { viewModel.updateHairColor(it) },
                    onUpdateGlassesFrameColor = { viewModel.updateGlassesFrameColor(it) },
                    onUpdateGlassesLensColor = { viewModel.updateGlassesLensColor(it) },
                    onUpdateHairTransforms = { x, y, s, r -> viewModel.updateHairTransforms(x, y, s, r) },
                    onUpdateGlassesTransforms = { x, y, s, r -> viewModel.updateGlassesTransforms(x, y, s, r) },
                    onResetTransforms = { viewModel.resetOverlayTransforms() }
                )
                AppScreen.History -> HistoryScreen(
                    historyList = history,
                    onBack = { viewModel.navigateTo(AppScreen.Home) },
                    onSelectRecord = { viewModel.selectHistoryRecord(it) },
                    onDeleteRecord = { viewModel.deleteHistoryRecord(it) },
                    onClearAll = { viewModel.clearAllHistories() }
                )
            }
        }
    }
}

// ==========================================
// 1. HOME SCREEN
// ==========================================

@Composable
fun HomeScreen(
    historyCount: Int,
    onStartAnalysis: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                // Background artistic style glow
                drawRect(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF2C1B4D), Color(0xFF0C0C0F)),
                        center = Offset(size.width * 0.8f, size.height * 0.2f),
                        radius = size.width * 1.2f
                    )
                )
            }
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Logo & Branding
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 28.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFD6BBFF), Color(0xFF9EAEFF))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Studio Logo",
                        tint = Color(0xFF0C0C0F),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "AI STYLE STUDIO",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp,
                    fontFamily = FontFamily.SansSerif,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "CURATED FOR THE NEXT GEN",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 2.sp,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            // Central Artistic Feature Display / Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp)
                    .graphicsLayer { rotationZ = -2f },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.04f))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = "Face scan illustration",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Face Landmarks & Personal Recommendations",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Upload a selfie to unlock hairstyles, frames, outfits, and color palettes optimized for your unique structural contours.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }

            // Quick Stats & Call to action triggers
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onStartAnalysis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "START STYLE ANALYSIS",
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowForward,
                            contentDescription = "Forward"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (historyCount > 0) {
                    OutlinedButton(
                        onClick = onNavigateToHistory,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "History Icon",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "VIEW PAST STYLES ($historyCount)",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                } else {
                    Text(
                        text = "No saved histories yet. Your styling scans will appear here.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// ==========================================
// 2. UPLOAD SCREEN
// ==========================================

@Composable
fun UploadScreen(
    selfie: Bitmap?,
    fullBody: Bitmap?,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onSelfieSelected: (Bitmap?) -> Unit,
    onFullBodySelected: (Bitmap?) -> Unit
) {
    val context = LocalContext.current

    val selfiePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val bitmap = loadUriBitmap(context, it)
            onSelfieSelected(bitmap)
        }
    }

    val fullBodyPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val bitmap = loadUriBitmap(context, it)
            onFullBodySelected(bitmap)
        }
    }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Upload Studio", fontWeight = FontWeight.Bold, letterSpacing = 1.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Upload Your Scans",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Provide a clean portrait for face contouring analysis. A full body scan helps refine apparel suggestions.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Selfie Image Selection Box (Mandatory)
                Text(
                    text = "PORTRAIT SELFIE (MANDATORY)*",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(
                            width = 1.dp,
                            color = if (selfie != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { selfiePicker.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (selfie != null) {
                        Image(
                            bitmap = selfie.asImageBitmap(),
                            contentDescription = "Uploaded Selfie",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Dark dim overlay with Change label
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.BottomEnd
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Change Portrait", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Add Selfie",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Select Facial Selfie", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("High resolution, front facing", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Full Body Image Selection Box (Optional)
                Text(
                    text = "FULL BODY SCAN (OPTIONAL)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { fullBodyPicker.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (fullBody != null) {
                        Image(
                            bitmap = fullBody.asImageBitmap(),
                            contentDescription = "Uploaded Full Body",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.BottomEnd
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Change Scan", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Accessibility,
                                contentDescription = "Add Full Body",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Select Full Body Scan", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Improves clothing contour fits", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Bottom Continue Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onBackground)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = onNext,
                    enabled = selfie != null,
                    modifier = Modifier
                        .weight(1.5f)
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("PREFERENCES", fontWeight = FontWeight.Bold, color = if (selfie != null) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = Icons.AutoMirrored.Default.ArrowForward, contentDescription = "Next")
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. PREFERENCES SCREEN
// ==========================================

@Composable
fun PreferencesScreen(
    gender: String,
    style: String,
    occasion: String,
    budget: String,
    isDemoMode: Boolean,
    isLoading: Boolean,
    error: String?,
    onBack: () -> Unit,
    onPreferenceChange: (String, String, String, String) -> Unit,
    onRunAnalysis: () -> Unit
) {
    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Personalization", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Customize Preferences",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "These selections refine your hair, accessories, and wardrobe recommendations.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Gender Preference
                    Text("GENDER ORIENTATION", fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Male", "Female", "Androgynous").forEach { option ->
                            val isSelected = gender == option
                            InputChip(
                                selected = isSelected,
                                onClick = { onPreferenceChange(option, style, occasion, budget) },
                                label = { Text(option) },
                                colors = InputChipDefaults.inputChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Style Theme Preference
                    Text("STYLE CONTEXT", fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Minimal", "Trendy", "Bold").forEach { option ->
                            val isSelected = style == option
                            InputChip(
                                selected = isSelected,
                                onClick = { onPreferenceChange(gender, option, occasion, budget) },
                                label = { Text(option) },
                                colors = InputChipDefaults.inputChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Occasion Preference
                    Text("OCCASION SPECIFIC", fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Casual", "College", "Party", "Traditional").forEach { option ->
                            val isSelected = occasion == option
                            InputChip(
                                selected = isSelected,
                                onClick = { onPreferenceChange(gender, style, option, budget) },
                                label = { Text(option) },
                                colors = InputChipDefaults.inputChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Budget Preference
                    Text("SARTORIAL BUDGET", fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Low", "Medium", "High").forEach { option ->
                            val isSelected = budget == option
                            InputChip(
                                selected = isSelected,
                                onClick = { onPreferenceChange(gender, style, occasion, option) },
                                label = { Text(option) },
                                colors = InputChipDefaults.inputChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                    
                    if (isDemoMode) {
                        Spacer(modifier = Modifier.height(28.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        ) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, contentDescription = "Demo Mode Info", tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    "No Gemini API key detected. Running in Offline Demo Mode with local style matching rules.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }

                    if (error != null) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(error, color = MaterialTheme.colorScheme.error, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Action Bar
                Button(
                    onClick = onRunAnalysis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "Run Analysis")
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("RUN AI STYLING REPORT", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                }
            }

            // Full screen loading shroud
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "ANALYZING STYLE LANDMARKS...",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 2.sp,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Detecting face shapes, color undertones, and outfits.",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. RESULTS SCREEN
// ==========================================

@Composable
fun ResultsScreen(
    report: StyleReport?,
    isDemoMode: Boolean,
    onBack: () -> Unit,
    onReset: () -> Unit,
    onOpenStylingRoom: () -> Unit
) {
    if (report == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No style analysis report loaded.")
        }
        return
    }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Style Assessment", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onReset) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset Studio")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Title block
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Your AI Assessment",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (isDemoMode) {
                            Text("Matched via Local Demo rules", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        } else {
                            Text("Powered by Gemini 3.5 Flash", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                        }
                    }

                    // Score Badge
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${report.styleScore}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.background,
                                lineHeight = 24.sp
                            )
                            Text(
                                text = "SCORE",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.background,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }

            // Interactive Virtual Try-On Banner Hook
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenStylingRoom() },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AccessibilityNew, contentDescription = "Try-On", tint = MaterialTheme.colorScheme.background)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Virtual Styling Room", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Fit suggested glasses and hairstyles directly on your portrait selfie.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.AutoMirrored.Default.ArrowForward, contentDescription = "Go", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Section 1: Detected Features Grid
            item {
                Text(
                    text = "DETECTED FACIAL CONTINGENTS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf(
                        "Face Shape" to report.faceShape.uppercase(),
                        "Skin Tone" to report.skinTone.uppercase(),
                        "Hair Type" to report.hairType.uppercase()
                    ).forEach { (label, value) ->
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(value, fontSize = 13.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Psychology, contentDescription = "Structure", tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("FACIAL STRUCTURE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(report.facialStructure, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 18.sp)
                        }
                    }
                }
            }

            // Section 2: Personalised Hairstyles (Hairstyles Suggestion)
            item {
                Text(
                    text = "RECOMMENDED HAIRSTYLES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                report.hairstyleSuggestions.forEach { styleItem ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(styleItem.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                                Box(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(styleItem.compatibility, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(styleItem.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
                        }
                    }
                }
            }

            // Section 3: Recommended Glasses
            item {
                Text(
                    text = "ACCESSORY / GLASSES FRAMES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                report.glassesSuggestions.forEach { glassItem ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(glassItem.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.secondary)
                                Box(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(glassItem.compatibility, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(glassItem.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
                        }
                    }
                }
            }

            // Section 4: Outfit Suggestions
            item {
                Text(
                    text = "CURATED OUTFT SUGGESTIONS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                report.outfitSuggestions.forEach { outfit ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(outfit.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.tertiary)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(outfit.occasion, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(outfit.priceRange, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(outfit.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
                        }
                    }
                }
            }

            // Section 5: Recommended Color Palette (Skin Tone Palette)
            item {
                Text(
                    text = "PERSONALIZED SKIN UNDERTONE COLOR PALETTE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(report.colorPalette) { colorItem ->
                        Card(
                            modifier = Modifier.width(140.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(70.dp)
                                        .background(Color(android.graphics.Color.parseColor(colorItem.hex)))
                                )
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(colorItem.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text(colorItem.hex, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(colorItem.description, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 13.sp, maxLines = 3)
                                }
                            }
                        }
                    }
                }
            }

            // Section 6: Custom Tips Bullet points
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TipsAndUpdates, contentDescription = "Tips", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("PERSONAL STYLE DIRECTIVES", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        report.styleTips.forEachIndexed { i, tip ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text("${i + 1}.", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(20.dp))
                                Text(tip, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Spacer bottom
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// ==========================================
// 5. VIRTUAL STYLING ROOM (MOCKUP ENGINE)
// ==========================================

@Composable
fun StylingRoomScreen(
    selfie: Bitmap?,
    report: StyleReport?,
    overlayState: OverlayState,
    onBack: () -> Unit,
    onSelectHairstyle: (String?) -> Unit,
    onSelectGlasses: (String?) -> Unit,
    onUpdateHairColor: (String) -> Unit,
    onUpdateGlassesFrameColor: (String) -> Unit,
    onUpdateGlassesLensColor: (String) -> Unit,
    onUpdateHairTransforms: (Float, Float, Float, Float) -> Unit,
    onUpdateGlassesTransforms: (Float, Float, Float, Float) -> Unit,
    onResetTransforms: () -> Unit
) {
    var showOverlays by remember { mutableStateOf(true) }
    var activeAdjustTarget by remember { mutableStateOf("hair") } // "hair" or "glasses"

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Styling Studio", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onResetTransforms) {
                        Icon(imageVector = Icons.Default.LayersClear, contentDescription = "Reset Overlays")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Main Live Mockup Canvas Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.3f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (selfie != null) {
                    // Selfie portrait background
                    Image(
                        bitmap = selfie.asImageBitmap(),
                        contentDescription = "Active Selfie background",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No selfie portrait loaded for mockup preview.", color = Color.White.copy(alpha = 0.5f))
                    }
                }

                // AR Live overlays drawn inside a multi-touch transform listener box
                if (showOverlays && selfie != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(activeAdjustTarget) {
                                detectTransformGestures { _, pan, zoom, rotation ->
                                    if (activeAdjustTarget == "hair") {
                                        onUpdateHairTransforms(pan.x, pan.y, zoom, rotation)
                                    } else {
                                        onUpdateGlassesTransforms(pan.x, pan.y, zoom, rotation)
                                    }
                                }
                            }
                    ) {
                        // 1. Draw Hairstyle Silhouette
                        if (overlayState.activeHairstyleId != null) {
                            Canvas(
                                modifier = Modifier
                                    .size(240.dp)
                                    .align(Alignment.Center)
                                    .graphicsLayer(
                                        translationX = overlayState.hairX,
                                        translationY = overlayState.hairY,
                                        scaleX = overlayState.hairScale,
                                        scaleY = overlayState.hairScale,
                                        rotationZ = overlayState.hairRotation
                                    )
                                    .border(
                                        width = if (activeAdjustTarget == "hair") 1.dp else 0.dp,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                            ) {
                                OverlayDrawers.drawHairstyle(
                                    drawScope = this,
                                    id = overlayState.activeHairstyleId,
                                    hairColor = Color(android.graphics.Color.parseColor(overlayState.hairColorHex))
                                )
                            }
                        }

                        // 2. Draw Glasses Silhouette
                        if (overlayState.activeGlassesId != null) {
                            Canvas(
                                modifier = Modifier
                                    .size(150.dp)
                                    .align(Alignment.Center)
                                    .graphicsLayer(
                                        translationX = overlayState.glassesX,
                                        translationY = overlayState.glassesY,
                                        scaleX = overlayState.glassesScale,
                                        scaleY = overlayState.glassesScale,
                                        rotationZ = overlayState.glassesRotation
                                    )
                                    .border(
                                        width = if (activeAdjustTarget == "glasses") 1.dp else 0.dp,
                                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                            ) {
                                OverlayDrawers.drawGlasses(
                                    drawScope = this,
                                    id = overlayState.activeGlassesId,
                                    frameColor = Color(android.graphics.Color.parseColor(overlayState.glassesFrameColorHex)),
                                    tintColor = Color(android.graphics.Color.parseColor(overlayState.glassesLensColorHex))
                                )
                            }
                        }
                    }
                }

                // "Before / After" instant comparator toggle holding block
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Row(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { showOverlays = !showOverlays },
                            modifier = Modifier.height(38.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (showOverlays) MaterialTheme.colorScheme.primary else Color.Transparent
                            ),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                        ) {
                            Text(
                                if (showOverlays) "Before vs After" else "Compare Mode",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (showOverlays) MaterialTheme.colorScheme.background else Color.White
                            )
                        }
                    }
                }

                // Active Transform Target Switch Overlay (floating on top right)
                if (showOverlays) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                            .padding(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("hair" to "Hairstyle", "glasses" to "Glasses").forEach { (target, label) ->
                            val isActive = activeAdjustTarget == target
                            Button(
                                onClick = { activeAdjustTarget = target },
                                modifier = Modifier.height(34.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent
                                )
                            ) {
                                Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isActive) Color.Black else Color.White)
                            }
                        }
                    }
                }
            }

            // Controls & Palettes area (weight-based)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.9f)
                    .padding(top = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. Selector row for Hair Options or Glasses based on adjust target
                if (activeAdjustTarget == "hair" && report != null) {
                    Text("CHOOSE HAIRSTYLE OVERLAY", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            InputChip(
                                selected = overlayState.activeHairstyleId == null,
                                onClick = { onSelectHairstyle(null) },
                                label = { Text("None") }
                            )
                        }
                        items(report.hairstyleSuggestions) { sug ->
                            InputChip(
                                selected = overlayState.activeHairstyleId == sug.id,
                                onClick = { onSelectHairstyle(sug.id) },
                                label = { Text(sug.name) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("SELECT HAIR COLOR", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf(
                            "#1A1A1A" to "Onyx",
                            "#2C3E50" to "Slate",
                            "#D35400" to "Auburn",
                            "#F1C40F" to "Blonde",
                            "#7F8C8D" to "Silver"
                        ).forEach { (hex, name) ->
                            val isSelected = overlayState.hairColorHex == hex
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(hex)))
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.2f),
                                        shape = CircleShape
                                    )
                                    .clickable { onUpdateHairColor(hex) }
                            )
                        }
                    }
                } else if (activeAdjustTarget == "glasses" && report != null) {
                    Text("SELECT GLASSES SILHOUETTE", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            InputChip(
                                selected = overlayState.activeGlassesId == null,
                                onClick = { onSelectGlasses(null) },
                                label = { Text("None") }
                            )
                        }
                        items(report.glassesSuggestions) { sug ->
                            InputChip(
                                selected = overlayState.activeGlassesId == sug.id,
                                onClick = { onSelectGlasses(sug.id) },
                                label = { Text(sug.name) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("SELECT FRAME COLOR", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf(
                            "#111111" to "Black",
                            "#D4AC0D" to "Gold",
                            "#95A5A6" to "Silver",
                            "#8E44AD" to "Purple"
                        ).forEach { (hex, name) ->
                            val isSelected = overlayState.glassesFrameColorHex == hex
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(hex)))
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.secondary else Color.White.copy(alpha = 0.2f),
                                        shape = CircleShape
                                    )
                                    .clickable { onUpdateGlassesFrameColor(hex) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("CHOOSE LENS TINT", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf(
                            "#3498DB" to "Sky Blue",
                            "#1D8348" to "Forest Green",
                            "#F39C12" to "Warm Amber",
                            "#9B59B6" to "Lilac",
                            "#E74C3C" to "Crimson"
                        ).forEach { (hex, name) ->
                            val isSelected = overlayState.glassesLensColorHex == hex
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(hex)))
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.secondary else Color.White.copy(alpha = 0.2f),
                                        shape = CircleShape
                                    )
                                    .clickable { onUpdateGlassesLensColor(hex) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Gesture, contentDescription = "Gesture Hint", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "Pro-Tip: Use intuitive 2-finger drag, pinch, and rotate gestures directly on the preview to align the overlays perfectly with your eyes & forehead contours.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 6. HISTORY SCREEN
// ==========================================

@Composable
fun HistoryScreen(
    historyList: List<StyleHistoryEntity>,
    onBack: () -> Unit,
    onSelectRecord: (StyleHistoryEntity) -> Unit,
    onDeleteRecord: (Int) -> Unit,
    onClearAll: () -> Unit
) {
    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Saved Reports", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (historyList.isNotEmpty()) {
                        IconButton(onClick = onClearAll) {
                            Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = "Clear All", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        if (historyList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.HistoryToggleOff, contentDescription = "No History", size = 64.dp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No Styling history available yet.", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Your completed face-scans and fashion forecasts will appear here.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(historyList) { item ->
                    val dateFormatted = remember(item.timestamp) {
                        val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
                        sdf.format(Date(item.timestamp))
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectRecord(item) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = dateFormatted,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(item.faceShape.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(item.stylePref, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }

                                // Delete Button & Score indicator
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("${item.styleScore}", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.secondary, fontSize = 14.sp)
                                    }

                                    IconButton(onClick = { onDeleteRecord(item.id) }) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Icon(imageVector: ImageVector, contentDescription: String, size: androidx.compose.ui.unit.Dp, color: Color) {
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        tint = color,
        modifier = Modifier.size(size)
    )
}
