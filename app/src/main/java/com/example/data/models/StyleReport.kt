package com.example.data.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StyleReport(
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

@JsonClass(generateAdapter = true)
data class HairstyleSuggestion(
    val id: String,
    val name: String,
    val description: String,
    val compatibility: String
)

@JsonClass(generateAdapter = true)
data class GlassesSuggestion(
    val id: String,
    val name: String,
    val description: String,
    val compatibility: String
)

@JsonClass(generateAdapter = true)
data class OutfitSuggestion(
    val name: String,
    val description: String,
    val occasion: String,
    val priceRange: String
)

@JsonClass(generateAdapter = true)
data class ColorItem(
    val name: String,
    val hex: String,
    val description: String
)
