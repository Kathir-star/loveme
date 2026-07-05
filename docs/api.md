# Style Mapping Engine API

The `style_mapper.py` module handles the rules mapping anatomical ratios to recommended styles and structures.

## Core Functions

### `get_recommendations`
Retrieves tailored styling advice, hairstyles, eyewear, and guidelines matching the detected shape and user-selected gender.

```python
def get_recommendations(face_shape: str, gender: str = "Male") -> dict:
    """
    Args:
        face_shape (str): 'oval', 'round', 'square', 'heart', or 'diamond'
        gender (str): 'Male', 'Female', or 'Androgynous'
    Returns:
        dict: Curated style suggestions.
    """
```

### `get_improvement_tips`
Provides structure-improvement tips designed to optimize grooming, tailoring, and styling based on facial proportions.

```python
def get_improvement_tips(face_shape: str) -> list:
    """
    Args:
        face_shape (str): 'oval', 'round', 'square', 'heart', or 'diamond'
    Returns:
        list: 4-5 actionable guidelines.
    """
```

## Data Models

The mappings are stored in structured nested dictionaries containing `id`, `name`, `description`, and `compatibility`. This allows standard APIs or rendering frontends to parse and render them as clean, visual cards with dynamic status badges.
