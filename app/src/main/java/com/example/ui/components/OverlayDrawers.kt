package com.example.ui.components

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

object OverlayDrawers {

    /**
     * Draws stylized glasses vectors dynamically based on ID.
     */
    fun drawGlasses(drawScope: DrawScope, id: String, frameColor: Color, tintColor: Color) {
        drawScope.apply {
            val width = size.width
            val height = size.height
            val halfW = width / 2
            val centerY = height / 2

            val lensWidth = width * 0.38f
            val lensHeight = height * 0.7f
            val bridgeWidth = width * 0.15f
            val bridgeLeft = halfW - (bridgeWidth / 2)

            val leftLensLeft = halfW - bridgeWidth / 2 - lensWidth
            val rightLensLeft = halfW + bridgeWidth / 2

            when (id) {
                "aviator" -> {
                    // Left Lens
                    val leftPath = Path().apply {
                        moveTo(leftLensLeft, centerY - lensHeight * 0.2f)
                        lineTo(leftLensLeft + lensWidth * 0.8f, centerY - lensHeight * 0.4f)
                        quadraticTo(
                            leftLensLeft + lensWidth, centerY - lensHeight * 0.2f,
                            leftLensLeft + lensWidth, centerY + lensHeight * 0.1f
                        )
                        quadraticTo(
                            leftLensLeft + lensWidth * 0.8f, centerY + lensHeight * 0.6f,
                            leftLensLeft + lensWidth * 0.4f, centerY + lensHeight * 0.6f
                        )
                        quadraticTo(
                            leftLensLeft, centerY + lensHeight * 0.3f,
                            leftLensLeft, centerY - lensHeight * 0.2f
                        )
                    }
                    // Right Lens (mirrored)
                    val rightPath = Path().apply {
                        moveTo(rightLensLeft + lensWidth, centerY - lensHeight * 0.2f)
                        lineTo(rightLensLeft + lensWidth * 0.2f, centerY - lensHeight * 0.4f)
                        quadraticTo(
                            rightLensLeft, centerY - lensHeight * 0.2f,
                            rightLensLeft, centerY + lensHeight * 0.1f
                        )
                        quadraticTo(
                            rightLensLeft + lensWidth * 0.2f, centerY + lensHeight * 0.6f,
                            rightLensLeft + lensWidth * 0.6f, centerY + lensHeight * 0.6f
                        )
                        quadraticTo(
                            rightLensLeft + lensWidth, centerY + lensHeight * 0.3f,
                            rightLensLeft + lensWidth, centerY - lensHeight * 0.2f
                        )
                    }

                    // Draw lens tints
                    drawPath(leftPath, color = tintColor.copy(alpha = 0.35f))
                    drawPath(rightPath, color = tintColor.copy(alpha = 0.35f))

                    // Draw frames
                    drawPath(leftPath, color = frameColor, style = Stroke(width = 4f))
                    drawPath(rightPath, color = frameColor, style = Stroke(width = 4f))

                    // Bridges (Aviator double-bridge)
                    drawLine(
                        color = frameColor,
                        start = Offset(halfW - bridgeWidth * 0.6f, centerY - lensHeight * 0.25f),
                        end = Offset(halfW + bridgeWidth * 0.6f, centerY - lensHeight * 0.25f),
                        strokeWidth = 4f
                    )
                    drawLine(
                        color = frameColor,
                        start = Offset(halfW - bridgeWidth * 0.5f, centerY - lensHeight * 0.05f),
                        end = Offset(halfW + bridgeWidth * 0.5f, centerY - lensHeight * 0.05f),
                        strokeWidth = 4f
                    )
                }
                "round" -> {
                    val radius = lensWidth * 0.5f
                    val leftCenter = Offset(leftLensLeft + radius, centerY)
                    val rightCenter = Offset(rightLensLeft + radius, centerY)

                    // Draw tinted circles
                    drawCircle(color = tintColor.copy(alpha = 0.25f), radius = radius, center = leftCenter)
                    drawCircle(color = tintColor.copy(alpha = 0.25f), radius = radius, center = rightCenter)

                    // Draw wireframe outlines
                    drawCircle(color = frameColor, radius = radius, center = leftCenter, style = Stroke(width = 3f))
                    drawCircle(color = frameColor, radius = radius, center = rightCenter, style = Stroke(width = 3f))

                    // Classic bridge arch
                    val bridgePath = Path().apply {
                        moveTo(leftCenter.x + radius, leftCenter.y)
                        quadraticTo(halfW, leftCenter.y - radius * 0.3f, rightCenter.x - radius, rightCenter.y)
                    }
                    drawPath(bridgePath, color = frameColor, style = Stroke(width = 3.5f))
                }
                "square" -> {
                    // Thick plastic frames
                    val leftRect = Rect(leftLensLeft, centerY - lensHeight / 2, leftLensLeft + lensWidth, centerY + lensHeight / 2)
                    val rightRect = Rect(rightLensLeft, centerY - lensHeight / 2, rightLensLeft + lensWidth, centerY + lensHeight / 2)

                    // Tint
                    drawRoundRect(
                        color = tintColor.copy(alpha = 0.3f),
                        topLeft = leftRect.topLeft,
                        size = leftRect.size,
                        cornerRadius = CornerRadius(12f, 12f)
                    )
                    drawRoundRect(
                        color = tintColor.copy(alpha = 0.3f),
                        topLeft = rightRect.topLeft,
                        size = rightRect.size,
                        cornerRadius = CornerRadius(12f, 12f)
                    )

                    // Outer thick frame
                    drawRoundRect(
                        color = frameColor,
                        topLeft = leftRect.topLeft,
                        size = leftRect.size,
                        cornerRadius = CornerRadius(12f, 12f),
                        style = Stroke(width = 8f)
                    )
                    drawRoundRect(
                        color = frameColor,
                        topLeft = rightRect.topLeft,
                        size = rightRect.size,
                        cornerRadius = CornerRadius(12f, 12f),
                        style = Stroke(width = 8f)
                    )

                    // Bridge
                    drawLine(
                        color = frameColor,
                        start = Offset(leftLensLeft + lensWidth, centerY),
                        end = Offset(rightLensLeft, centerY),
                        strokeWidth = 10f
                    )

                    // Small rivets (stylized silver dots)
                    drawCircle(Color.White, radius = 2f, center = Offset(leftLensLeft + 8f, centerY - lensHeight * 0.3f))
                    drawCircle(Color.White, radius = 2f, center = Offset(rightLensLeft + lensWidth - 8f, centerY - lensHeight * 0.3f))
                }
                "cat_eye" -> {
                    // Flared points on outer corners
                    val leftPath = Path().apply {
                        moveTo(leftLensLeft, centerY + lensHeight * 0.1f)
                        quadraticTo(leftLensLeft + lensWidth * 0.1f, centerY - lensHeight * 0.4f, leftLensLeft + lensWidth, centerY - lensHeight * 0.3f)
                        lineTo(leftLensLeft + lensWidth, centerY + lensHeight * 0.3f)
                        quadraticTo(leftLensLeft + lensWidth * 0.5f, centerY + lensHeight * 0.5f, leftLensLeft, centerY + lensHeight * 0.1f)
                    }
                    val leftFlare = Path().apply {
                        moveTo(leftLensLeft + lensWidth, centerY - lensHeight * 0.3f)
                        lineTo(leftLensLeft - lensWidth * 0.15f, centerY - lensHeight * 0.5f)
                        lineTo(leftLensLeft, centerY + lensHeight * 0.1f)
                        close()
                    }

                    val rightPath = Path().apply {
                        moveTo(rightLensLeft + lensWidth, centerY + lensHeight * 0.1f)
                        quadraticTo(rightLensLeft + lensWidth * 0.9f, centerY - lensHeight * 0.4f, rightLensLeft, centerY - lensHeight * 0.3f)
                        lineTo(rightLensLeft, centerY + lensHeight * 0.3f)
                        quadraticTo(rightLensLeft + lensWidth * 0.5f, centerY + lensHeight * 0.5f, rightLensLeft + lensWidth, centerY + lensHeight * 0.1f)
                    }
                    val rightFlare = Path().apply {
                        moveTo(rightLensLeft, centerY - lensHeight * 0.3f)
                        lineTo(rightLensLeft + lensWidth * 1.15f, centerY - lensHeight * 0.5f)
                        lineTo(rightLensLeft + lensWidth, centerY + lensHeight * 0.1f)
                        close()
                    }

                    // Lens
                    drawPath(leftPath, color = tintColor.copy(alpha = 0.4f))
                    drawPath(rightPath, color = tintColor.copy(alpha = 0.4f))

                    // Thick frame base
                    drawPath(leftPath, color = frameColor, style = Stroke(width = 4f))
                    drawPath(rightPath, color = frameColor, style = Stroke(width = 4f))

                    // Wing flares
                    drawPath(leftFlare, color = frameColor)
                    drawPath(rightFlare, color = frameColor)

                    // Thin gold bridge
                    val bridgePath = Path().apply {
                        moveTo(leftLensLeft + lensWidth, centerY - lensHeight * 0.2f)
                        quadraticTo(halfW, centerY - lensHeight * 0.28f, rightLensLeft, centerY - lensHeight * 0.2f)
                    }
                    drawPath(bridgePath, color = Color(0xFFF1C40F), style = Stroke(width = 4.5f))
                }
                else -> { // clubmaster
                    // Semi-rimless browline style
                    val leftRim = Path().apply {
                        moveTo(leftLensLeft, centerY - lensHeight * 0.4f)
                        lineTo(leftLensLeft + lensWidth, centerY - lensHeight * 0.4f)
                        quadraticTo(leftLensLeft + lensWidth * 0.95f, centerY + lensHeight * 0.4f, leftLensLeft + lensWidth * 0.5f, centerY + lensHeight * 0.4f)
                        quadraticTo(leftLensLeft + lensWidth * 0.05f, centerY + lensHeight * 0.4f, leftLensLeft, centerY - lensHeight * 0.1f)
                    }

                    // Draw lens tint
                    drawPath(leftRim, color = tintColor.copy(alpha = 0.3f))
                    // Draw lower thin wire outline
                    drawPath(leftRim, color = Color(0xFFD4AC0D), style = Stroke(width = 2.5f))

                    // Thick browline cap
                    val leftCap = Path().apply {
                        moveTo(leftLensLeft - 4f, centerY - lensHeight * 0.4f)
                        lineTo(leftLensLeft + lensWidth + 2f, centerY - lensHeight * 0.4f)
                        lineTo(leftLensLeft + lensWidth, centerY - lensHeight * 0.15f)
                        quadraticTo(leftLensLeft + lensWidth * 0.5f, centerY - lensHeight * 0.25f, leftLensLeft - 2f, centerY - lensHeight * 0.2f)
                        close()
                    }
                    drawPath(leftCap, color = frameColor)

                    // Right Lens (mirrored)
                    val rightRim = Path().apply {
                        moveTo(rightLensLeft + lensWidth, centerY - lensHeight * 0.4f)
                        lineTo(rightLensLeft, centerY - lensHeight * 0.4f)
                        quadraticTo(rightLensLeft + lensWidth * 0.05f, centerY + lensHeight * 0.4f, rightLensLeft + lensWidth * 0.5f, centerY + lensHeight * 0.4f)
                        quadraticTo(rightLensLeft + lensWidth * 0.95f, centerY + lensHeight * 0.4f, rightLensLeft + lensWidth, centerY - lensHeight * 0.1f)
                    }
                    drawPath(rightRim, color = tintColor.copy(alpha = 0.3f))
                    drawPath(rightRim, color = Color(0xFFD4AC0D), style = Stroke(width = 2.5f))

                    val rightCap = Path().apply {
                        moveTo(rightLensLeft + lensWidth + 4f, centerY - lensHeight * 0.4f)
                        lineTo(rightLensLeft - 2f, centerY - lensHeight * 0.4f)
                        lineTo(rightLensLeft, centerY - lensHeight * 0.15f)
                        quadraticTo(rightLensLeft + lensWidth * 0.5f, centerY - lensHeight * 0.25f, rightLensLeft + lensWidth + 2f, centerY - lensHeight * 0.2f)
                        close()
                    }
                    drawPath(rightCap, color = frameColor)

                    // Gold metal bridge
                    drawLine(
                        color = Color(0xFFD4AC0D),
                        start = Offset(leftLensLeft + lensWidth, centerY - lensHeight * 0.32f),
                        end = Offset(rightLensLeft, centerY - lensHeight * 0.32f),
                        strokeWidth = 5f
                    )
                }
            }

            // Draw arms/temples fading off slightly
            drawLine(
                color = frameColor.copy(alpha = 0.8f),
                start = Offset(leftLensLeft, centerY - lensHeight * 0.1f),
                end = Offset(leftLensLeft - width * 0.15f, centerY - lensHeight * 0.2f),
                strokeWidth = 6f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = frameColor.copy(alpha = 0.8f),
                start = Offset(rightLensLeft + lensWidth, centerY - lensHeight * 0.1f),
                end = Offset(rightLensLeft + lensWidth + width * 0.15f, centerY - lensHeight * 0.2f),
                strokeWidth = 6f,
                cap = StrokeCap.Round
            )
        }
    }

    /**
     * Draws stylized hairstyle outlines dynamically based on ID.
     */
    fun drawHairstyle(drawScope: DrawScope, id: String, hairColor: Color) {
        drawScope.apply {
            val width = size.width
            val height = size.height
            val centerX = width / 2
            val bottomY = height * 0.8f

            val path = Path()
            val accentPath = Path()

            when (id) {
                "textured_fringe" -> {
                    // Voluminous top, choppy spikes hanging over forehead
                    path.moveTo(width * 0.15f, bottomY * 0.5f)
                    path.cubicTo(
                        width * 0.1f, bottomY * 0.05f,
                        width * 0.9f, bottomY * 0.05f,
                        width * 0.85f, bottomY * 0.5f
                    )
                    // Choppy bottom fringe spikes hanging down
                    path.lineTo(width * 0.8f, bottomY * 0.58f)
                    path.lineTo(width * 0.73f, bottomY * 0.52f)
                    path.lineTo(width * 0.65f, bottomY * 0.62f)
                    path.lineTo(width * 0.58f, bottomY * 0.54f)
                    path.lineTo(width * 0.50f, bottomY * 0.65f) // central drop
                    path.lineTo(width * 0.42f, bottomY * 0.55f)
                    path.lineTo(width * 0.34f, bottomY * 0.63f)
                    path.lineTo(width * 0.26f, bottomY * 0.53f)
                    path.lineTo(width * 0.20f, bottomY * 0.58f)
                    path.close()

                    // Add textured highlights
                    accentPath.moveTo(width * 0.3f, bottomY * 0.25f)
                    accentPath.quadraticTo(width * 0.5f, bottomY * 0.15f, width * 0.7f, bottomY * 0.25f)
                    accentPath.moveTo(width * 0.25f, bottomY * 0.4f)
                    accentPath.quadraticTo(width * 0.5f, bottomY * 0.3f, width * 0.75f, bottomY * 0.4f)
                }
                "undercut" -> {
                    // Shaved, slim bottom sides with massive layered swept-back block on top
                    // Big top pompadour swept right
                    path.moveTo(width * 0.25f, bottomY * 0.45f)
                    path.cubicTo(
                        width * 0.1f, bottomY * 0.01f,
                        width * 0.8f, bottomY * -0.1f, // high loft
                        width * 0.85f, bottomY * 0.35f
                    )
                    path.quadraticTo(width * 0.6f, bottomY * 0.48f, width * 0.25f, bottomY * 0.45f)
                    path.close()

                    // Shaved side burns fading downwards
                    val leftSide = Path().apply {
                        moveTo(width * 0.25f, bottomY * 0.45f)
                        lineTo(width * 0.21f, bottomY * 0.46f)
                        lineTo(width * 0.21f, bottomY * 0.68f)
                        lineTo(width * 0.26f, bottomY * 0.62f)
                        close()
                    }
                    val rightSide = Path().apply {
                        moveTo(width * 0.75f, bottomY * 0.45f)
                        lineTo(width * 0.79f, bottomY * 0.46f)
                        lineTo(width * 0.79f, bottomY * 0.68f)
                        lineTo(width * 0.74f, bottomY * 0.62f)
                        close()
                    }

                    drawPath(leftSide, color = hairColor.copy(alpha = 0.45f))
                    drawPath(rightSide, color = hairColor.copy(alpha = 0.45f))

                    // Flow lines in pompadour
                    accentPath.moveTo(width * 0.35f, bottomY * 0.3f)
                    accentPath.cubicTo(width * 0.4f, bottomY * 0.15f, width * 0.7f, bottomY * 0.12f, width * 0.78f, bottomY * 0.28f)
                    accentPath.moveTo(width * 0.45f, bottomY * 0.4f)
                    accentPath.cubicTo(width * 0.5f, bottomY * 0.28f, width * 0.68f, bottomY * 0.25f, width * 0.74f, bottomY * 0.35f)
                }
                "bob" -> {
                    // Framing curtains falling straight down to chin lines on left/right
                    path.moveTo(width * 0.32f, bottomY * 0.42f)
                    path.quadraticTo(width * 0.5f, bottomY * 0.12f, width * 0.68f, bottomY * 0.42f)
                    path.lineTo(width * 0.82f, bottomY * 0.48f)
                    // cascading down right side
                    path.cubicTo(width * 0.88f, bottomY * 0.7f, width * 0.82f, bottomY * 0.95f, width * 0.78f, bottomY * 1.0f)
                    path.quadraticTo(width * 0.72f, bottomY * 0.75f, width * 0.68f, bottomY * 0.48f)
                    // split peak in middle
                    path.lineTo(centerX, bottomY * 0.38f)
                    // cascading down left side
                    path.lineTo(width * 0.32f, bottomY * 0.48f)
                    path.quadraticTo(width * 0.28f, bottomY * 0.75f, width * 0.22f, bottomY * 1.0f)
                    path.cubicTo(width * 0.12f, bottomY * 0.95f, width * 0.12f, bottomY * 0.7f, width * 0.18f, bottomY * 0.48f)
                    path.close()

                    // Hair texture streaks
                    accentPath.moveTo(width * 0.25f, bottomY * 0.55f)
                    accentPath.quadraticTo(width * 0.22f, bottomY * 0.75f, width * 0.26f, bottomY * 0.9f)
                    accentPath.moveTo(width * 0.75f, bottomY * 0.55f)
                    accentPath.quadraticTo(width * 0.78f, bottomY * 0.75f, width * 0.74f, bottomY * 0.9f)
                }
                "long_waves" -> {
                    // Long wavy cascading hair
                    path.moveTo(width * 0.35f, bottomY * 0.35f)
                    path.quadraticTo(width * 0.5f, bottomY * 0.05f, width * 0.65f, bottomY * 0.35f)
                    path.lineTo(width * 0.85f, bottomY * 0.45f)
                    // wavy right lock
                    path.cubicTo(
                        width * 0.95f, bottomY * 0.68f,
                        width * 0.72f, bottomY * 0.85f,
                        width * 0.88f, bottomY * 1.15f
                    )
                    path.cubicTo(
                        width * 0.72f, bottomY * 1.12f,
                        width * 0.8f, bottomY * 0.78f,
                        width * 0.65f, bottomY * 0.45f
                    )
                    path.lineTo(centerX, bottomY * 0.32f)
                    // wavy left lock
                    path.lineTo(width * 0.35f, bottomY * 0.45f)
                    path.cubicTo(
                        width * 0.2f, bottomY * 0.78f,
                        width * 0.28f, bottomY * 1.12f,
                        width * 0.12f, bottomY * 1.15f
                    )
                    path.cubicTo(
                        width * 0.28f, bottomY * 0.85f,
                        width * 0.05f, bottomY * 0.68f,
                        width * 0.15f, bottomY * 0.45f
                    )
                    path.close()

                    // Flowing waves streaks
                    accentPath.moveTo(width * 0.22f, bottomY * 0.58f)
                    accentPath.cubicTo(width * 0.18f, bottomY * 0.75f, width * 0.24f, bottomY * 0.92f, width * 0.19f, bottomY * 1.05f)
                    accentPath.moveTo(width * 0.78f, bottomY * 0.58f)
                    accentPath.cubicTo(width * 0.82f, bottomY * 0.75f, width * 0.76f, bottomY * 0.92f, width * 0.81f, bottomY * 1.05f)
                }
                else -> { // pixie / short crop (Default spiky cut)
                    // Edgy, dynamic spiky pixie crop
                    path.moveTo(width * 0.22f, bottomY * 0.48f)
                    path.lineTo(width * 0.18f, bottomY * 0.38f) // left peak
                    path.lineTo(width * 0.26f, bottomY * 0.35f)
                    path.lineTo(width * 0.24f, bottomY * 0.22f) // higher spiky top left
                    path.lineTo(width * 0.36f, bottomY * 0.24f)
                    path.lineTo(width * 0.42f, bottomY * 0.12f) // top peak 1
                    path.lineTo(width * 0.50f, bottomY * 0.18f)
                    path.lineTo(width * 0.58f, bottomY * 0.10f) // top peak 2
                    path.lineTo(width * 0.64f, bottomY * 0.22f)
                    path.lineTo(width * 0.76f, bottomY * 0.20f) // spiky top right
                    path.lineTo(width * 0.74f, bottomY * 0.35f)
                    path.lineTo(width * 0.82f, bottomY * 0.38f) // right peak
                    path.lineTo(width * 0.78f, bottomY * 0.48f)
                    path.lineTo(width * 0.72f, bottomY * 0.48f)
                    // soft uneven baby fringe at forehead
                    path.lineTo(width * 0.65f, bottomY * 0.52f)
                    path.lineTo(width * 0.58f, bottomY * 0.46f)
                    path.lineTo(width * 0.50f, bottomY * 0.54f)
                    path.lineTo(width * 0.42f, bottomY * 0.46f)
                    path.lineTo(width * 0.35f, bottomY * 0.52f)
                    path.lineTo(width * 0.28f, bottomY * 0.48f)
                    path.close()

                    // Pixie crown crown highlights
                    accentPath.moveTo(width * 0.38f, bottomY * 0.32f)
                    accentPath.lineTo(width * 0.42f, bottomY * 0.24f)
                    accentPath.moveTo(width * 0.55f, bottomY * 0.30f)
                    accentPath.lineTo(width * 0.52f, bottomY * 0.22f)
                }
            }

            // Draw primary hair color fill
            drawPath(path, color = hairColor)

            // Draw styling outlines & accent highlights
            drawPath(
                path = path,
                color = Color.White.copy(alpha = 0.2f),
                style = Stroke(width = 3f, join = StrokeJoin.Round, cap = StrokeCap.Round)
            )

            drawPath(
                path = accentPath,
                color = Color.White.copy(alpha = 0.4f),
                style = Stroke(width = 4f, join = StrokeJoin.Round, cap = StrokeCap.Round)
            )
        }
    }
}
