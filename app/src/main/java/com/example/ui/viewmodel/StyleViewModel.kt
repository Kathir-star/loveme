package com.example.ui.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.StyleHistoryEntity
import com.example.data.models.StyleReport
import com.example.data.repository.StyleRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppScreen {
    Home,
    Upload,
    Preferences,
    Results,
    StylingRoom,
    History
}

data class OverlayState(
    val activeHairstyleId: String? = null,
    val activeGlassesId: String? = null,
    val hairColorHex: String = "#2C3E50", // Dark Slate Black
    val glassesFrameColorHex: String = "#1A1A1A", // Onyx
    val glassesLensColorHex: String = "#3498DB", // Sky blue Tint
    
    // Hair coordinate transforms
    val hairX: Float = 0f,
    val hairY: Float = -150f,
    val hairScale: Float = 1.1f,
    val hairRotation: Float = 0f,

    // Glasses coordinate transforms
    val glassesX: Float = 0f,
    val glassesY: Float = 50f,
    val glassesScale: Float = 1.0f,
    val glassesRotation: Float = 0f
)

data class StyleUiState(
    val currentScreen: AppScreen = AppScreen.Home,
    val selfieBitmap: Bitmap? = null,
    val fullBodyBitmap: Bitmap? = null,
    val gender: String = "Male", // "Male", "Female", "Androgynous"
    val stylePref: String = "Minimal", // "Minimal", "Trendy", "Bold"
    val occasionPref: String = "Casual", // "Casual", "College", "Party", "Traditional"
    val budgetPref: String = "Medium", // "Low", "Medium", "High"
    val isLoadingAnalysis: Boolean = false,
    val analysisError: String? = null,
    val activeReport: StyleReport? = null,
    val isDemoMode: Boolean = false,
    val overlayState: OverlayState = OverlayState()
)

class StyleViewModel(private val repository: StyleRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(StyleUiState())
    val uiState: StateFlow<StyleUiState> = _uiState.asStateFlow()

    // Reactive flow of all saved histories in the Room database
    val historyList: StateFlow<List<StyleHistoryEntity>> = repository.allItemsStateFlow(viewModelScope)

    init {
        // Run a liveness check on API key to see if we're in Demo mode
        val apiKey = com.example.BuildConfig.GEMINI_API_KEY
        val isDemo = apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY"
        _uiState.update { it.copy(isDemoMode = isDemo) }
    }

    fun navigateTo(screen: AppScreen) {
        _uiState.update { it.copy(currentScreen = screen, analysisError = null) }
    }

    fun setSelfie(bitmap: Bitmap?) {
        _uiState.update { it.copy(selfieBitmap = bitmap) }
    }

    fun setFullBodyImage(bitmap: Bitmap?) {
        _uiState.update { it.copy(fullBodyBitmap = bitmap) }
    }

    fun updatePreferences(
        gender: String,
        style: String,
        occasion: String,
        budget: String
    ) {
        _uiState.update { 
            it.copy(
                gender = gender,
                stylePref = style,
                occasionPref = occasion,
                budgetPref = budget
            )
        }
    }

    fun selectHistoryRecord(entity: StyleHistoryEntity) {
        val report = StyleReport(
            faceShape = entity.faceShape,
            skinTone = entity.skinTone,
            hairType = entity.hairType,
            facialStructure = entity.facialStructure,
            styleScore = entity.styleScore,
            hairstyleSuggestions = entity.hairstyleSuggestions,
            glassesSuggestions = entity.glassesSuggestions,
            outfitSuggestions = entity.outfitSuggestions,
            colorPalette = entity.colorPalette,
            styleTips = entity.styleTips
        )

        _uiState.update {
            it.copy(
                activeReport = report,
                gender = entity.gender ?: "Male",
                stylePref = entity.stylePref,
                occasionPref = entity.occasionPref,
                budgetPref = entity.budgetPref,
                // Pre-populate overlay with suggested options
                overlayState = OverlayState(
                    activeHairstyleId = report.hairstyleSuggestions.firstOrNull()?.id,
                    activeGlassesId = report.glassesSuggestions.firstOrNull()?.id
                )
            )
        }
        navigateTo(AppScreen.Results)
    }

    fun deleteHistoryRecord(id: Int) {
        viewModelScope.launch {
            repository.deleteHistory(id)
        }
    }

    fun clearAllHistories() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun resetStudio() {
        _uiState.update {
            StyleUiState(
                currentScreen = AppScreen.Home,
                isDemoMode = it.isDemoMode
            )
        }
    }

    /**
     * Executes the styling analysis. Combines uploaded photos with selections,
     * calls Gemini, saves to history, and navigates to Results.
     */
    fun runStylingAnalysis() {
        val selfie = _uiState.value.selfieBitmap
        if (selfie == null) {
            _uiState.update { it.copy(analysisError = "Selfie image is required to run analysis.") }
            return
        }

        _uiState.update { it.copy(isLoadingAnalysis = true, analysisError = null) }

        viewModelScope.launch {
            try {
                val state = _uiState.value
                val report = repository.analyzeStyle(
                    selfieBitmap = selfie,
                    fullBodyBitmap = state.fullBodyBitmap,
                    gender = state.gender,
                    stylePref = state.stylePref,
                    occasionPref = state.occasionPref,
                    budgetPref = state.budgetPref
                )

                // Save results locally to Room Database
                val historyEntity = StyleHistoryEntity(
                    selfieUri = null, // In prototypes we can keep the active bitmaps in-memory for virtual overlays
                    fullBodyUri = null,
                    gender = state.gender,
                    stylePref = state.stylePref,
                    occasionPref = state.occasionPref,
                    budgetPref = state.budgetPref,
                    faceShape = report.faceShape,
                    skinTone = report.skinTone,
                    hairType = report.hairType,
                    facialStructure = report.facialStructure,
                    styleScore = report.styleScore,
                    hairstyleSuggestions = report.hairstyleSuggestions,
                    glassesSuggestions = report.glassesSuggestions,
                    outfitSuggestions = report.outfitSuggestions,
                    colorPalette = report.colorPalette,
                    styleTips = report.styleTips
                )
                repository.insertHistory(historyEntity)

                _uiState.update {
                    it.copy(
                        activeReport = report,
                        isLoadingAnalysis = false,
                        overlayState = OverlayState(
                            activeHairstyleId = report.hairstyleSuggestions.firstOrNull()?.id,
                            activeGlassesId = report.glassesSuggestions.firstOrNull()?.id
                        )
                    )
                }
                navigateTo(AppScreen.Results)

            } catch (e: Exception) {
                Log.e("StyleViewModel", "Styling analysis failed", e)
                _uiState.update { 
                    it.copy(
                        isLoadingAnalysis = false,
                        analysisError = "Styling analysis failed: ${e.localizedMessage}. Please try again."
                    )
                }
            }
        }
    }

    // --- Overlay State Controls ---
    
    fun selectOverlayHairstyle(id: String?) {
        _uiState.update {
            it.copy(
                overlayState = it.overlayState.copy(activeHairstyleId = id)
            )
        }
    }

    fun selectOverlayGlasses(id: String?) {
        _uiState.update {
            it.copy(
                overlayState = it.overlayState.copy(activeGlassesId = id)
            )
        }
    }

    fun updateHairColor(hex: String) {
        _uiState.update {
            it.copy(
                overlayState = it.overlayState.copy(hairColorHex = hex)
            )
        }
    }

    fun updateGlassesFrameColor(hex: String) {
        _uiState.update {
            it.copy(
                overlayState = it.overlayState.copy(glassesFrameColorHex = hex)
            )
        }
    }

    fun updateGlassesLensColor(hex: String) {
        _uiState.update {
            it.copy(
                overlayState = it.overlayState.copy(glassesLensColorHex = hex)
            )
        }
    }

    fun updateHairTransforms(panX: Float, panY: Float, zoom: Float, rotate: Float) {
        _uiState.update {
            val os = it.overlayState
            it.copy(
                overlayState = os.copy(
                    hairX = os.hairX + panX,
                    hairY = os.hairY + panY,
                    hairScale = (os.hairScale * zoom).coerceIn(0.4f, 3.0f),
                    hairRotation = os.hairRotation + rotate
                )
            )
        }
    }

    fun updateGlassesTransforms(panX: Float, panY: Float, zoom: Float, rotate: Float) {
        _uiState.update {
            val os = it.overlayState
            it.copy(
                overlayState = os.copy(
                    glassesX = os.glassesX + panX,
                    glassesY = os.glassesY + panY,
                    glassesScale = (os.glassesScale * zoom).coerceIn(0.4f, 3.0f),
                    glassesRotation = os.glassesRotation + rotate
                )
            )
        }
    }

    fun resetOverlayTransforms() {
        _uiState.update {
            it.copy(
                overlayState = it.overlayState.copy(
                    hairX = 0f,
                    hairY = -150f,
                    hairScale = 1.1f,
                    hairRotation = 0f,
                    glassesX = 0f,
                    glassesY = 50f,
                    glassesScale = 1.0f,
                    glassesRotation = 0f
                )
            )
        }
    }
}

// Extension to map Flow to StateFlow inside ViewModel init, avoiding direct DAO leak
private fun StyleRepository.allItemsStateFlow(scope: kotlinx.coroutines.CoroutineScope): StateFlow<List<StyleHistoryEntity>> {
    return this.allHistory.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}

@Suppress("UNCHECKED_CAST")
class StyleViewModelFactory(private val repository: StyleRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StyleViewModel::class.java)) {
            return StyleViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
