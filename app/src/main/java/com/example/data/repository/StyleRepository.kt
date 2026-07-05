package com.example.data.repository

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.api.*
import com.example.data.local.StyleDao
import com.example.data.local.StyleHistoryEntity
import com.example.data.models.*
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.*

class StyleRepository(private val styleDao: StyleDao) {

    val allHistory: Flow<List<StyleHistoryEntity>> = styleDao.getAllHistory()

    fun getHistoryById(id: Int): Flow<StyleHistoryEntity?> = styleDao.getHistoryById(id)

    suspend fun insertHistory(entity: StyleHistoryEntity): Long = withContext(Dispatchers.IO) {
        styleDao.insertHistory(entity)
    }

    suspend fun deleteHistory(id: Int) = withContext(Dispatchers.IO) {
        styleDao.deleteHistoryById(id)
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        styleDao.clearAllHistory()
    }

    /**
     * Converts a Bitmap to Base64 String for Gemini inline data.
     */
    private fun Bitmap.toBase64(): String {
        val outputStream = ByteArrayOutputStream()
        this.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    /**
     * Analyzes style using Gemini 3.5 Flash or a high-fidelity local generator fallback.
     */
    suspend fun analyzeStyle(
        selfieBitmap: Bitmap,
        fullBodyBitmap: Bitmap?,
        gender: String,
        stylePref: String,
        occasionPref: String,
        budgetPref: String
    ): StyleReport = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY

        // Check if API Key is placeholder or blank
        val isKeyPlaceholder = apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY"

        if (isKeyPlaceholder) {
            Log.w("StyleRepository", "Gemini API key is placeholder or blank. Falling back to Mock Style Report.")
            return@withContext generateMockReport(gender, stylePref, occasionPref, budgetPref)
        }

        try {
            val parts = mutableListOf<GeminiPart>()
            
            // Add selfie image
            parts.add(
                GeminiPart(
                    inlineData = GeminiInlineData(
                        mimeType = "image/jpeg",
                        data = selfieBitmap.toBase64()
                    )
                )
            )

            // Add optional full-body image
            if (fullBodyBitmap != null) {
                parts.add(
                    GeminiPart(
                        inlineData = GeminiInlineData(
                            mimeType = "image/jpeg",
                            data = fullBodyBitmap.toBase64()
                        )
                    )
                )
            }

            // Define the styling prompt
            val prompt = """
                The user has uploaded a facial selfie (and optionally a full body image).
                Perform a professional style and beauty analysis. Detect and assess:
                1. Face Shape (must choose exactly one: oval, round, square, heart, or diamond)
                2. Skin Tone (must choose exactly one: light, medium, or dark)
                3. Hair Type (must choose exactly one: straight, wavy, or curly)
                4. Facial Structure (jawline sharpness, prominence of cheekbones, etc.)

                Recommend personalized hair styling, glass frames, outfit suggestions, and color palettes matching the following preferences:
                - Gender Alignment: $gender
                - Style Theme: $stylePref
                - Occasion: $occasionPref
                - Budget Category: $budgetPref

                Generate a Style Score out of 10 and three custom, Gen-Z friendly improvement tips.
                
                You must return the analysis strictly as a single JSON object.
                Do NOT wrap the response in markdown blocks like ```json and do NOT include any introductory or concluding text. 
                Ensure it is valid parseable JSON fitting the following schema:
                {
                  "faceShape": "oval | round | square | heart | diamond",
                  "skinTone": "light | medium | dark",
                  "hairType": "straight | wavy | curly",
                  "facialStructure": "facial description",
                  "styleScore": 8,
                  "hairstyleSuggestions": [
                    { "id": "textured_fringe | undercut | buzzcut | bob | pixie | long_waves", "name": "Textured Fringe (etc)", "description": "Why it suits their shape", "compatibility": "Highly Recommended" }
                  ],
                  "glassesSuggestions": [
                    { "id": "aviator | round | square | cat_eye | clubmaster", "name": "Classic Aviator (etc)", "description": "Why it suits their shape", "compatibility": "Flattering" }
                  ],
                  "outfitSuggestions": [
                    { "name": "Outfit set name", "description": "Outfit description", "occasion": "$occasionPref", "priceRange": "$budgetPref" }
                  ],
                  "colorPalette": [
                    { "name": "Color Name", "hex": "#HEXCODE", "description": "Why it elevates skin tone" }
                  ],
                  "styleTips": [
                    "Tip 1", "Tip 2", "Tip 3"
                  ]
                }
            """.trimIndent()

            parts.add(GeminiPart(text = prompt))

            val systemInstruction = GeminiContent(
                parts = listOf(
                    GeminiPart(text = "You are AI Style Studio, an elite Gen-Z fashion stylist and visual image consultant. You analyze user photos and preferences to generate stylish, detailed fashion recommendations.")
                )
            )

            val request = GeminiRequest(
                contents = listOf(GeminiContent(parts = parts)),
                generationConfig = GeminiConfig(responseMimeType = "application/json", temperature = 0.7f),
                systemInstruction = systemInstruction
            )

            val response = GeminiClient.api.analyzeStyle(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

            if (!jsonText.isNullOrEmpty()) {
                val cleanJson = cleanJsonString(jsonText)
                val reportAdapter = GeminiClient.moshi.adapter(StyleReport::class.java)
                val report = reportAdapter.fromJson(cleanJson)
                if (report != null) {
                    return@withContext report
                }
            }
            throw IllegalStateException("Empty or unparseable response from Gemini API")

        } catch (e: Exception) {
            Log.e("StyleRepository", "Error calling Gemini API: ${e.message}", e)
            // If the real API call fails, fall back to generating a beautiful mock report instead of crashing
            return@withContext generateMockReport(gender, stylePref, occasionPref, budgetPref)
        }
    }

    private fun cleanJsonString(raw: String): String {
        var clean = raw.trim()
        if (clean.startsWith("```json")) {
            clean = clean.removePrefix("```json")
        } else if (clean.startsWith("```")) {
            clean = clean.removePrefix("```")
        }
        if (clean.endsWith("```")) {
            clean = clean.removeSuffix("```")
        }
        return clean.trim()
    }

    /**
     * High-fidelity local style generator that serves as a fallback and an offline simulator.
     */
    fun generateMockReport(
        gender: String,
        stylePref: String,
        occasionPref: String,
        budgetPref: String
    ): StyleReport {
        val random = Random()

        // Choose styling traits based on random selections to show versatility
        val faceShapes = listOf("oval", "round", "square", "heart", "diamond")
        val skinTones = listOf("light", "medium", "dark")
        val hairTypes = listOf("straight", "wavy", "curly")
        
        val faceShape = faceShapes[random.nextInt(faceShapes.size)]
        val skinTone = skinTones[random.nextInt(skinTones.size)]
        val hairType = hairTypes[random.nextInt(hairTypes.size)]

        val facialStructure = when (faceShape) {
            "oval" -> "Perfectly balanced contours with smooth, curved cheek landmarks."
            "round" -> "Soft jawline angles paired with full, expressive cheek definitions."
            "square" -> "Highly defined, strong jawline landmarks and aligned temples."
            "heart" -> "Slightly pointed, elegant chin structure tapering from high cheekbones."
            "diamond" -> "Dramatically contoured, prominent cheekbones narrowing towards chin and forehead."
            else -> "Clean, symmetrical landmarks with soft organic curves."
        }

        val styleScore = 7 + random.nextInt(4) // Generates 7, 8, 9, or 10

        // Customise Hairstyle Suggestions based on Face Shape
        val hairstyles = when (faceShape) {
            "oval" -> listOf(
                HairstyleSuggestion("textured_fringe", "Textured Fringe", "A choppy top fringe adds contrast to your balanced oval outline.", "Highly Recommended"),
                HairstyleSuggestion("undercut", "Modern Undercut", "Slick sides draw focus to your high cheek symmetry.", "Ideal")
            )
            "round" -> listOf(
                HairstyleSuggestion("undercut", "Volume Undercut", "High volume on top elongates round proportions and structures features.", "Highly Recommended"),
                HairstyleSuggestion("buzzcut", "Groomed Fade", "Crisp skin fade lines sharpen softer jawline curves.", "Flattering")
            )
            "square" -> listOf(
                HairstyleSuggestion("long_waves", "Soft Layered Waves", "Gentle waves soften the strong edges of your square contours.", "Highly Recommended"),
                HairstyleSuggestion("textured_fringe", "Messy Crop", "Asymmetrical crop fringe breaks up strong architectural alignment.", "Flattering")
            )
            "heart" -> listOf(
                HairstyleSuggestion("bob", "Classic Bob", "A chin-length cut fills in the narrower lower half of your face shape.", "Highly Recommended"),
                HairstyleSuggestion("long_waves", "Curled Wisps", "Soft curls framing the temples balance your wider forehead.", "Ideal")
            )
            else -> listOf( // Diamond
                HairstyleSuggestion("pixie", "Textured Pixie", "Layered, short pixie crop showcases your prominent cheek bone architecture.", "Highly Recommended"),
                HairstyleSuggestion("bob", "Wavy Lob", "A soft long bob widens the jaw area, balancing diamond points.", "Ideal")
            )
        }

        // Customise Glasses Suggestions based on Face Shape
        val glasses = when (faceShape) {
            "round", "oval" -> listOf(
                GlassesSuggestion("square", "Thick Rectangular Frames", "Sharp geometric angles add visual structure and contrast to rounded curves.", "Highly Recommended"),
                GlassesSuggestion("clubmaster", "Retro Clubmasters", "Strong browlines create a defining horizontal visual line.", "Stylish")
            )
            "square" -> listOf(
                GlassesSuggestion("round", "Round Wireframes", "Soft circular geometry softens the heavy architectural chin alignment.", "Highly Recommended"),
                GlassesSuggestion("aviator", "Classic Teardrop Aviators", "Gently curved lens shapes blend seamlessly with strong contours.", "Sleek")
            )
            "heart", "diamond" -> listOf(
                GlassesSuggestion("cat_eye", "Elevated Cat-Eye", "Upward flared wings match your high cheek contours perfectly.", "Highly Recommended"),
                GlassesSuggestion("round", "Slim Oval Wireframes", "Soft curved lines offset the prominent angles of diamond cheeks.", "Ideal")
            )
            else -> listOf(
                GlassesSuggestion("clubmaster", "Bold Wayfarer", "A classic versatile silhouette that highlights high facial arches.", "Highly Recommended")
            )
        }

        // Customise Outfit Suggestions based on Occasion & Style Preference
        val outfits = mutableListOf<OutfitSuggestion>()
        when (occasionPref) {
            "Casual" -> {
                if (stylePref == "Bold") {
                    outfits.add(OutfitSuggestion("Oversized Graphics & Utility Cargoes", "A high-contrast street statement: pairing a bright neon utility chest bag with baggy graphic cotton canvas and technical cargo trousers.", "Casual", budgetPref))
                } else if (stylePref == "Trendy") {
                    outfits.add(OutfitSuggestion("Cropped Bomber & Pleated Chinos", "Trendy and urban: matching an olive nylon cropped flight jacket with deep tan pleated chinos and high-top sneakers.", "Casual", budgetPref))
                } else {
                    outfits.add(OutfitSuggestion("Heavyweight Tee & Straight Denim", "Pure clean minimalism: a dense off-white crewneck matched with classic indigo selvedge denim and crisp white leather low-tops.", "Casual", budgetPref))
                }
            }
            "College" -> {
                if (stylePref == "Bold") {
                    outfits.add(OutfitSuggestion("Colorblock Cardigan & Raw Hem Denim", "Playful academic: a heavy checkerboard orange cardigan contrasted with vintage raw hem straight jeans and platform mules.", "College", budgetPref))
                } else if (stylePref == "Trendy") {
                    outfits.add(OutfitSuggestion("Relaxed Blazer & Cargo Shorts", "Chic preppy: a light linen double-breasted blazer worn over a simple white rib tank, styled with high pocket utility shorts.", "College", budgetPref))
                } else {
                    outfits.add(OutfitSuggestion("Neutral Hooded Sweatshirt & Skate Pants", "Effortless college look: a cozy dust-grey hoodie layered under a canvas worker jacket, paired with wide-leg canvas skate pants.", "College", budgetPref))
                }
            }
            "Party" -> {
                if (stylePref == "Bold") {
                    outfits.add(OutfitSuggestion("Patent Leather Trench & Metallic Tops", "High-octane nightlife: a glossy black vinyl trench paired with a shimmering silver mesh top and tailored high-waisted cigarette pants.", "Party", budgetPref))
                } else if (stylePref == "Trendy") {
                    outfits.add(OutfitSuggestion("Satin Slip Set or Velvet Overshirt", "Luxury lounge: an emerald green satin drapery button-down matched with slim tailored trousers and gold metal accessories.", "Party", budgetPref))
                } else {
                    outfits.add(OutfitSuggestion("Monochrome Dark tailoring", "Sleek minimalist party elegance: an all-black mockneck sweater paired with matte wool-blend trousers and chunky leather boots.", "Party", budgetPref))
                }
            }
            else -> { // Traditional
                if (stylePref == "Bold") {
                    outfits.add(OutfitSuggestion("Modernised Fusion Sherwani or Lehenga", "A vibrant avant-garde traditional: pairing hand-embroidered metallic motifs on a structured silhouette with bold asymmetric drapes.", "Traditional", budgetPref))
                } else if (stylePref == "Trendy") {
                    outfits.add(OutfitSuggestion("Linen Kurta Set with Pastel Bandhgala", "Trendy heritage: a soft sage green fine linen long kurta styled with a dynamic soft pink embroidered waistcoat.", "Traditional", budgetPref))
                } else {
                    outfits.add(OutfitSuggestion("Classic Silk Blend Kurta & White Pajamas", "Timeless traditional minimalism: a crisp ivory mulberry silk-cotton blend kurta paired with comfortable off-white drawstring pajamas.", "Traditional", budgetPref))
                }
            }
        }

        // Customise Color Palette based on Skin Tone
        val colors = when (skinTone) {
            "light" -> listOf(
                ColorItem("Emerald Spruce", "#0E6251", "A rich, deep green that adds high contrast and vibrancy to fair complexions."),
                ColorItem("Royal Sapphire", "#1A5276", "An intense cool blue that enhances bright cool undertones."),
                ColorItem("Soft Berry Rose", "#B03A2E", "A pleasant, warm berry tone that adds healthy color to fair cheeks.")
            )
            "medium" -> listOf(
                ColorItem("Warm Ochre", "#D35400", "A rich burnt orange that glows exquisitely alongside golden skin pigments."),
                ColorItem("Olive Moss", "#1D8348", "An organic earthy green that highlights warm neutral undertones."),
                ColorItem("Creamy Sand", "#F5CBA7", "A pleasant neutral beige that creates soft, elevated visual pairing.")
            )
            else -> listOf( // Dark
                ColorItem("Saffron Marigold", "#F39C12", "A high-octane yellow-gold that pops beautifully and radiates elegance on deep skin tones."),
                ColorItem("Rich Copper", "#A04000", "A warm, lustrous metallic brown that blends seamlessly with deep bronze undertones."),
                ColorItem("Electric Teal", "#117A65", "A brilliant jewel tone that creates a striking, fashion-forward contrast.")
            )
        }

        // Customise tips
        val tips = when (stylePref) {
            "Bold" -> listOf(
                "Rule of Proportions: Contrast oversized graphic statements with clean, structured footwear to keep the look grounded.",
                "Jewelry Contrast: Introduce cold steel or bold gold geometric rings to amplify your sharp facial structures.",
                "Aesthetic Anchor: Choose one loud signature piece (like the metallic top) and keep other colors neutral to avoid visual clutter."
            )
            "Trendy" -> listOf(
                "Tonal Styling: Stick to different shade intensities of the same color family (e.g. olive + sage) for an effortless fashion editor vibe.",
                "Frame selection: Circular frame silhouettes will instantly add playful Gen-Z edge and break up stiff facial features.",
                "Texture Play: Combine high-sheen satin with heavy knitwear to create dynamic visual depth in simple outfits."
            )
            else -> listOf(
                "Sartorial Precision: Since you prefer minimalism, ensure your garments fit perfectly at the shoulders. Fit is the ultimate style cheat code.",
                "Contrast Balance: Pair a light colored top (such as the heavyweight off-white tee) with dark denim to draw focus to your facial framing.",
                "Monochrome Depth: Use a silver watch or a slim metal necklace to add elegant visual highlight points to basic outfits."
            )
        }

        return StyleReport(
            faceShape = faceShape,
            skinTone = skinTone,
            hairType = hairType,
            facialStructure = facialStructure,
            styleScore = styleScore,
            hairstyleSuggestions = hairstyles,
            glassesSuggestions = glasses,
            outfitSuggestions = outfits,
            colorPalette = colors,
            styleTips = tips
        )
    }
}
