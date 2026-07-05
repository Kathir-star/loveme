package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.models.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class StyleConverters {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @TypeConverter
    fun fromHairstyleList(list: List<HairstyleSuggestion>?): String? {
        if (list == null) return null
        val type = Types.newParameterizedType(List::class.java, HairstyleSuggestion::class.java)
        return moshi.adapter<List<HairstyleSuggestion>>(type).toJson(list)
    }

    @TypeConverter
    fun toHairstyleList(json: String?): List<HairstyleSuggestion>? {
        if (json.isNullOrEmpty()) return null
        val type = Types.newParameterizedType(List::class.java, HairstyleSuggestion::class.java)
        return moshi.adapter<List<HairstyleSuggestion>>(type).fromJson(json)
    }

    @TypeConverter
    fun fromGlassesList(list: List<GlassesSuggestion>?): String? {
        if (list == null) return null
        val type = Types.newParameterizedType(List::class.java, GlassesSuggestion::class.java)
        return moshi.adapter<List<GlassesSuggestion>>(type).toJson(list)
    }

    @TypeConverter
    fun toGlassesList(json: String?): List<GlassesSuggestion>? {
        if (json.isNullOrEmpty()) return null
        val type = Types.newParameterizedType(List::class.java, GlassesSuggestion::class.java)
        return moshi.adapter<List<GlassesSuggestion>>(type).fromJson(json)
    }

    @TypeConverter
    fun fromOutfitList(list: List<OutfitSuggestion>?): String? {
        if (list == null) return null
        val type = Types.newParameterizedType(List::class.java, OutfitSuggestion::class.java)
        return moshi.adapter<List<OutfitSuggestion>>(type).toJson(list)
    }

    @TypeConverter
    fun toOutfitList(json: String?): List<OutfitSuggestion>? {
        if (json.isNullOrEmpty()) return null
        val type = Types.newParameterizedType(List::class.java, OutfitSuggestion::class.java)
        return moshi.adapter<List<OutfitSuggestion>>(type).fromJson(json)
    }

    @TypeConverter
    fun fromColorList(list: List<ColorItem>?): String? {
        if (list == null) return null
        val type = Types.newParameterizedType(List::class.java, ColorItem::class.java)
        return moshi.adapter<List<ColorItem>>(type).toJson(list)
    }

    @TypeConverter
    fun toColorList(json: String?): List<ColorItem>? {
        if (json.isNullOrEmpty()) return null
        val type = Types.newParameterizedType(List::class.java, ColorItem::class.java)
        return moshi.adapter<List<ColorItem>>(type).fromJson(json)
    }

    @TypeConverter
    fun fromStringList(list: List<String>?): String? {
        if (list == null) return null
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        return moshi.adapter<List<String>>(type).toJson(list)
    }

    @TypeConverter
    fun toStringList(json: String?): List<String>? {
        if (json.isNullOrEmpty()) return null
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        return moshi.adapter<List<String>>(type).fromJson(json)
    }
}
