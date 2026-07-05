# Face Shape Detector & Geometry

The face shape classification engine processes face mesh coordinate landmarks to determine structural proportions.

## Ratios Evaluated

The system tracks three primary facial ratios to classify shape:

1. **Width-to-Height Ratio (`width_to_height`)**:
   - Calculated as: `f_width / f_height`
   - Measures how elongated or wide the face structure is.
   
2. **Jaw-to-Cheekbone Ratio (`jaw_to_cheek`)**:
   - Calculated as: `j_width / f_width`
   - High ratios (> 0.85) imply a wide, strong angular jawline.

3. **Forehead-to-Cheekbone Ratio (`forehead_to_cheek`)**:
   - Calculated as: `fh_width / f_width`
   - High ratios (> 0.90) indicate a wider upper facial contour (typical of Heart-shaped structures).

## Rule-Based Classification Logic

The system runs a deterministic classifier over the calculated ratios:

| Ratio Width/Height | Jaw/Cheek | Forehead/Cheek | Classified Shape |
| :--- | :--- | :--- | :--- |
| **> 0.88** | > 0.85 | - | **Square** |
| **> 0.88** | <= 0.85 | - | **Round** |
| **< 0.78** | - | > 0.90 | **Heart** |
| **< 0.78** | - | <= 0.90 | **Diamond** |
| **0.78 to 0.88** | - | > 0.92 | **Heart** |
| **0.78 to 0.88** | - | <= 0.92 | **Oval** |

This guarantees robust, lightning-fast edge classification without requiring heavy deep-learning model hosting!
