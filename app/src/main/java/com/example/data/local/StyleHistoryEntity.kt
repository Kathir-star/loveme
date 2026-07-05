package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.models.*

@Entity(tableName = "style_history")
data class StyleHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val selfieUri: String? = null,
    val fullBodyUri: String? = null,
    val gender: String? = null,
    val stylePref: String,
    val occasionPref: String,
    val budgetPref: String,
    val faceShape: String,
    val skinTone: String,
    val hairType: String,
    val facialStructure: String,
    val styleScore: Int,
    val hairstyleSuggestions: List<HairstyleSuggestion>,
    val glassesSuggestions: List<GlassesSuggestion>,
    val outfitSuggestions: List<OutfitSuggestion>,
    val colorPalette: List<ColorItem>,
    val styleTips: List<String>
)
