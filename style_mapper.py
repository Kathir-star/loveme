# -*- coding: utf-8 -*-
"""
AI Style Studio - Face Shape style mapping module.
This python module defines the rule-based matching mapping face shapes (Oval, Round, Square, Heart, Diamond)
to recommended hairstyles, glass frames, color palettes, and curated fashion recommendations.
Supports gender-specific filtering and tailored improvement tips.
"""

# The Master Style Mapping Dictionary for Glasses, Description, and Base Tips
STYLE_MAP = {
    "oval": {
        "face_shape_desc": "Balanced proportions, slightly curved jaw, and symmetrical features.",
        "glasses": [
            {
                "id": "square",
                "name": "Rectangular and Geometric Frames",
                "description": "Slightly wider rectangular glasses add structure and complement your natural facial symmetry.",
                "compatibility": "Flattering"
            },
            {
                "id": "clubmaster",
                "name": "Retro Browline Clubmasters",
                "description": "A prominent upper line draws attention upwards, accentuating cheek bones.",
                "compatibility": "Highly Recommended"
            }
        ],
        "tips": [
            "Fit is key: With an oval face shape, keep garment proportions clean and centered at the shoulders.",
            "Avoid overly long fringes that block your natural facial balance."
        ]
    },
    "round": {
        "face_shape_desc": "Soft curves, wide cheekbones, and a gentle curved jawline.",
        "glasses": [
            {
                "id": "square",
                "name": "Thick Rectangular / Angular Frames",
                "description": "Bold square or rectangular silhouettes break the roundness and add sharp structural angles.",
                "compatibility": "Highly Recommended"
            },
            {
                "id": "clubmaster",
                "name": "Slim Wayfarer Frames",
                "description": "Defined rectangular edges draw attention outward and slim the cheek line.",
                "compatibility": "Stylish"
            }
        ],
        "tips": [
            "Use vertical visual elements like V-necks or long necklaces to naturally elongate your neck and face silhouette.",
            "Avoid highly round wireframes which over-emphasize soft features."
        ]
    },
    "square": {
        "face_shape_desc": "Strong architectural jawline, broad forehead, and equal width-to-height ratio.",
        "glasses": [
            {
                "id": "round",
                "name": "Round Wireframes",
                "description": "Curved or circular lens silhouettes perfectly balance strong structural jaw boundaries.",
                "compatibility": "Highly Recommended"
            },
            {
                "id": "aviator",
                "name": "Classic Teardrop Aviators",
                "description": "Soft downward teardrop lines offset square chin lines for an effortlessly cool visual aesthetic.",
                "compatibility": "Sleek"
            }
        ],
        "tips": [
            "Soften strong tailoring with textured materials: pair knits or soft linen with rigid canvas jackets.",
            "Select eyewear where the frame width is slightly wider than your jawline."
        ]
    },
    "heart": {
        "face_shape_desc": "Broad forehead tapering down to a narrow, pointed elegant chin.",
        "glasses": [
            {
                "id": "cat_eye",
                "name": "Elevated Cat-Eye Frames",
                "description": "Upward sweeping wings match the high cheek contours and draw emphasis upwards.",
                "compatibility": "Highly Recommended"
            },
            {
                "id": "round",
                "name": "Slim Oval Wireframes",
                "description": "A rounded lower frame balances a sharp chin, adding smooth organic curves.",
                "compatibility": "Ideal"
            }
        ],
        "tips": [
            "Draw attention downward with interesting collars, crewnecks, or unique necklaces.",
            "Choose frames with heavier bottom details to add weight to the lower half of the face."
        ]
    },
    "diamond": {
        "face_shape_desc": "Narrow forehead and jawline with highly prominent, striking cheekbone contours.",
        "glasses": [
            {
                "id": "cat_eye",
                "name": "Fashion-Forward Cat-Eye",
                "description": "Slightly flared corners accentuate your gorgeous diamond cheeks while keeping focus balanced.",
                "compatibility": "Highly Recommended"
            },
            {
                "id": "round",
                "name": "Rimless Oval / Round Frames",
                "description": "Soft circles balance cheekbone dominance without creating wide visual blocks.",
                "compatibility": "Ideal"
            }
        ],
        "tips": [
            "Experiment with bold necklines and statement earrings that highlight your unique diamond bone structure.",
            "Keep hairstyles voluminous at the temples or jawline while keeping the cheeks clean."
        ]
    }
}

# The Master Style Mapping Dictionary incorporating gender-specific hairstyles
GENDER_HAIRSTYLES = {
    "oval": {
        "Male": [
            {
                "id": "undercut",
                "name": "Sleek Modern Undercut",
                "description": "Short sides with a slicked-back top highlight natural symmetry and cheekbone definition.",
                "compatibility": "Highly Recommended"
            },
            {
                "id": "textured_fringe",
                "name": "Textured Quiff",
                "description": "Adds slight volume on top without disrupting the elegant, balanced proportions of your face.",
                "compatibility": "Excellent"
            }
        ],
        "Female": [
            {
                "id": "long_layers",
                "name": "Long Soft Layers",
                "description": "Layers starting below the chin frame the perfect symmetry of your oval contours.",
                "compatibility": "Highly Recommended"
            },
            {
                "id": "curtain_bangs",
                "name": "Chic Curtain Bangs",
                "description": "Frames the eyes beautifully and accentuates a balanced facial structure.",
                "compatibility": "Excellent"
            }
        ],
        "Androgynous": [
            {
                "id": "shag_crop",
                "name": "Textured Shag Crop",
                "description": "A gender-neutral modern shag crop that plays off clean balanced structures with organic texture.",
                "compatibility": "Highly Recommended"
            },
            {
                "id": "classic_bob",
                "name": "Minimalist Blunt Bob",
                "description": "A crisp, unisex jawline-skimming bob highlighting superb facial balance.",
                "compatibility": "Excellent"
            }
        ]
    },
    "round": {
        "Male": [
            {
                "id": "pompadour",
                "name": "Voluminous Pompadour Fade",
                "description": "High top volume creates vertical elongation, offsetting round soft cheeks.",
                "compatibility": "Highly Recommended"
            },
            {
                "id": "quiff",
                "name": "Textured Side Quiff",
                "description": "An asymmetric sweep breaks up the perfect round symmetry, adding sharp lines.",
                "compatibility": "Excellent"
            }
        ],
        "Female": [
            {
                "id": "pixie",
                "name": "Asymmetrical Pixie",
                "description": "An angular pixie cut with side volume draws eyes upwards and adds structural contours.",
                "compatibility": "Highly Recommended"
            },
            {
                "id": "long_layers",
                "name": "Long Shag with Side-Swept Bangs",
                "description": "Elongates the facial structure while side bangs break roundness visually.",
                "compatibility": "Excellent"
            }
        ],
        "Androgynous": [
            {
                "id": "asym_undercut",
                "name": "Angular Side Undercut",
                "description": "Sharp shaved sides with an asymmetric top swoop to elongate and add visual structure.",
                "compatibility": "Highly Recommended"
            },
            {
                "id": "wolf_cut",
                "name": "Modern Wolf Cut",
                "description": "Textured crown layers and thin ends streamline round contours.",
                "compatibility": "Excellent"
            }
        ]
    },
    "square": {
        "Male": [
            {
                "id": "buzzcut",
                "name": "Textured Buzz Cut with Skin Fade",
                "description": "Accentuates your powerful masculine jawline and ultra-sharp architectural bones.",
                "compatibility": "Highly Recommended"
            },
            {
                "id": "slick_back",
                "name": "Classic Side-Part Slick Back",
                "description": "Slightly soft parted top tempers the heavy symmetry of square angles.",
                "compatibility": "Excellent"
            }
        ],
        "Female": [
            {
                "id": "long_waves",
                "name": "Soft Layered Waves",
                "description": "Cascading curls or waves starting below the jawline gently soften heavy square corners.",
                "compatibility": "Highly Recommended"
            },
            {
                "id": "lob_fringe",
                "name": "Textured Lob with Wispy Fringe",
                "description": "A long bob with thin, wispy front layers disrupts the rigid lines of the chin.",
                "compatibility": "Excellent"
            }
        ],
        "Androgynous": [
            {
                "id": "textured_crop",
                "name": "Messy Textured Crop",
                "description": "Disordered crown layers break up square forehead lines while remaining sharp and gender-neutral.",
                "compatibility": "Highly Recommended"
            },
            {
                "id": "mullet",
                "name": "Textured Modern Mullet",
                "description": "Shorter temple layers soften the upper corners while maintaining strong jaw framing.",
                "compatibility": "Excellent"
            }
        ]
    },
    "heart": {
        "Male": [
            {
                "id": "messy_fringe",
                "name": "Messy Textured Fringe",
                "description": "Adds casual volume over a wider forehead while keeping sides medium to balance a sharp chin.",
                "compatibility": "Highly Recommended"
            },
            {
                "id": "mid_length_sweep",
                "name": "Mid-Length Swept Back",
                "description": "Flowing locks behind the ears add width near the bottom of the face.",
                "compatibility": "Excellent"
            }
        ],
        "Female": [
            {
                "id": "lob_waves",
                "name": "Wavy Collarbone Lob",
                "description": "Lob waves add width around the narrow jawline, balancing the broad upper face.",
                "compatibility": "Highly Recommended"
            },
            {
                "id": "side_sweep",
                "name": "Deep Side Part with Waves",
                "description": "Cuts across a broad forehead line and builds thickness around a dainty chin.",
                "compatibility": "Excellent"
            }
        ],
        "Androgynous": [
            {
                "id": "french_bob",
                "name": "Textured French Bob",
                "description": "A gender-neutral bob hitting right at the lip level to fill out a narrower chin area.",
                "compatibility": "Highly Recommended"
            },
            {
                "id": "shaggy_fringe",
                "name": "Shaggy Fringe Crop",
                "description": "Conceals broad temples while wispy ends frame cheeks softly.",
                "compatibility": "Excellent"
            }
        ]
    },
    "diamond": {
        "Male": [
            {
                "id": "textured_crop",
                "name": "High Volume Textured Crop",
                "description": "Builds volume at the temples and crown to match prominent, high cheek bones.",
                "compatibility": "Highly Recommended"
            },
            {
                "id": "side_sweep_quiff",
                "name": "Swept Side Quiff with Beard",
                "description": "Adding facial hair adds volume to a narrower chin, completing high cheekbone contrast.",
                "compatibility": "Excellent"
            }
        ],
        "Female": [
            {
                "id": "curtain_lob",
                "name": "Curtain Bangs with Lob",
                "description": "Bangs frame the narrow forehead while long bob layers fill out a slender chin.",
                "compatibility": "Highly Recommended"
            },
            {
                "id": "side_fringe",
                "name": "Swept Side Fringe with Layers",
                "description": "Softens cheek contours and breaks up prominent diamond-like angles.",
                "compatibility": "Excellent"
            }
        ],
        "Androgynous": [
            {
                "id": "mullet",
                "name": "Soft Modern Shag Mullet",
                "description": "Temples are kept full with textured layers to complement cheek width without drowning features.",
                "compatibility": "Highly Recommended"
            },
            {
                "id": "pixie_bob",
                "name": "Textured Long Pixie",
                "description": "Tucked ears highlight prominent cheek contours while textured ends fill jaw hollows.",
                "compatibility": "Excellent"
            }
        ]
    }
}


def get_recommendations(face_shape: str, gender: str = "Male") -> dict:
    """
    Retrieve style advice, hairstyles, glasses, and tips for a given face shape and gender.
    Args:
        face_shape (str): 'oval', 'round', 'square', 'heart', or 'diamond' (case-insensitive)
        gender (str): 'Male', 'Female', or 'Androgynous'
    Returns:
        dict: Curated styles matching the detected face shape and gender.
    """
    normalized_shape = face_shape.strip().lower()
    normalized_gender = gender.strip()
    if normalized_gender not in ["Male", "Female", "Androgynous"]:
        normalized_gender = "Male"

    # Get base glasses and tips
    if normalized_shape in STYLE_MAP:
        result = STYLE_MAP[normalized_shape].copy()
    else:
        # Fallback to oval
        result = STYLE_MAP["oval"].copy()
        normalized_shape = "oval"

    # Fetch gender-specific hairstyles
    hairstyles = GENDER_HAIRSTYLES.get(normalized_shape, {}).get(normalized_gender, [])
    result["hairstyles"] = hairstyles
    return result


def get_improvement_tips(face_shape: str) -> list:
    """
    Create personal styling / improvement tips tailored strictly to the face shape structure.
    """
    normalized_shape = face_shape.strip().lower()
    if normalized_shape == "oval":
        return [
            "Maintain Equilibrium: Since your face proportions are perfectly balanced, avoid heavy vertical bangs or long, dense sidelocks that obscure this symmetry.",
            "Eyewear Alignment: Pick frames that are exactly as wide as the broadest part of your face to preserve the proportional golden ratio.",
            "Hair Volume: Keep the top volume moderate. High pompadours can over-elongate your balanced head shape.",
            "Accessorizing: Simple geometric studs or small drop earrings complement the gentle curves of an oval jawline."
        ]
    elif normalized_shape == "round":
        return [
            "Inject Architectural Angles: Use sharp rectangular eyeglasses or angular frames to cut through rounded cheek lines and define borders.",
            "Elevate the Crown: Style your hair with height at the top (like a quiff or blowout) while keeping the side hair short to visually elongate your facial frame.",
            "Avoid Circular Shapes: Circular earrings, round sunglasses, and dense rounded bangs can make the face look broader than it is.",
            "Tailoring Structure: Opt for sharp structured clothing (V-necks, lapels, jackets with structured shoulders) to add definition."
        ]
    elif normalized_shape == "square":
        return [
            "Soften Sharp Edges: Counteract your strong, prominent jawline with round wireframes, aviators, or soft curved sunglasses.",
            "Incorporate Fluid Layers: Avoid extremely blunt haircuts or rigid straight cuts that terminate right at your jawline. Soft, cascading layers help break rigid lines.",
            "Asymmetrical Styling: A deep side-part or sweeping asymmetrical locks disrupt square facial symmetry with natural-looking motion.",
            "Organic Textures: Wear soft, textured fabrics like wool knits, linen blends, or draping scarves near your face to complement your strong bone contours."
        ]
    elif normalized_shape == "heart":
        return [
            "Balance the Jawline: Since your forehead is wider and chin is narrow, focus on building visual volume near the bottom of your face.",
            "Eyewear Rules: Choose frames that are wider at the bottom (like aviators or light rimless styles) to offset a broader forehead.",
            "Mid-Length Fringe: A side-swept soft fringe helps reduce the apparent width of the forehead, balancing visual lines perfectly.",
            "Collar Choice: Crewnecks, cowl necks, and high collars add soft width around the lower neck, anchoring a delicate pointed chin."
        ]
    elif normalized_shape == "diamond":
        return [
            "Emphasize the Forehead and Jaw: Soften prominent, wide cheekbones by choosing hairstyles that are fuller at the temples and base of the neck.",
            "Cat-Eye Magic: Curved glasses with flared top corners (like classic cat-eye frames) pair beautifully with prominent diamond cheeks.",
            "Highlight the Lips: Use beard styles (for males) or lip details to ground the narrow chin and create a robust vertical flow.",
            "Avoid Rimless Rectangles: Very narrow rectangular spectacles can make the broad cheeks look wider and highlight facial pinches."
        ]
    else:
        return [
            "Highlight your unique structure by choosing sunglasses that align with your eyebrow line.",
            "Experiment with textures to add volume and movement to your daily hairstyle.",
            "Keep outfits proportional: shoulders should line up cleanly with your natural frame limits."
        ]


# Quick diagnostic script for testing the module
if __name__ == "__main__":
    test_shapes = ["round", "Square", "oval", "triangle"]
    print("--- STYLE MAP DICTIONARY DIAGNOSTIC WITH GENDER ---")
    for shape in test_shapes:
        for gender in ["Male", "Female", "Androgynous"]:
            rec = get_recommendations(shape, gender)
            print(f"\n[Detected Shape]: {shape.upper()} | [Gender]: {gender}")
            print(f"Description: {rec.get('face_shape_desc', 'N/A')}")
            print("Recommended Hairstyles:")
            for hs in rec.get("hairstyles", []):
                print(f" - {hs['name']} ({hs['compatibility']}): {hs['description']}")
            print("Improvement Tips:")
            for tip in get_improvement_tips(shape):
                print(f" * {tip}")
