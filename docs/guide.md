# Getting Started with AI Style Studio

Welcome to the AI Style Studio setup and user guide. This document explains how to set up, launch, and maximize the effectiveness of the application.

## System Overview

AI Style Studio consists of:
1. **Facial Tracking Core**: Uses MediaPipe's high-fidelity Face Mesh to map coordinate landmarks in real time.
2. **Style Mapping Rules (`style_mapper.py`)**: A rule-based database mapping specific geometries and gender choices to hairstyles and glasses.
3. **Interactive Control Room (`app.py`)**: A Streamlit interface to visualize the mesh overlay, diagnostic metrics, and selected style items.

## Quick Start (Streamlit Web Interface)

To launch the web interface, run the following command in your terminal:

```bash
streamlit run app.py
```

### Steps for Best Results
1. **Use a High-Quality Camera**: Ensure your room is well-lit and your face is facing the camera directly.
2. **Select Style Preferences**: Use the sidebar to set your gender, style context, occasion, and budget.
3. **Analyze**: Upload a clear selfie. The tracker will map your key facial ratios instantly.
4. **Discover**: Browse the tailored hairstyles, eyewear frames, and custom improvement tips.
