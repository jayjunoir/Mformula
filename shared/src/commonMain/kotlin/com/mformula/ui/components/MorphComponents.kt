package com.mformula.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mformula.domain.engine.DailyPuzzle
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mformula.shared.generated.resources.Res
import mformula.shared.generated.resources.ic_archive_lock
import mformula.shared.generated.resources.ic_stats_blue
import org.jetbrains.compose.resources.painterResource
import kotlin.math.pow
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

// Taste Contract Palette Constants
val InkBlack = Color(0xFF0A0A0E)
val DeepObsidian = Color(0xFF101014)
val VermilionRed = Color(0xFFFF3B30)
val WarmCream = Color(0xFFFDFBF7)
val ArcadeYellow = Color(0xFFFFCC00)
val ElectricMint = Color(0xFF00E599)
val CardDark = Color(0xFF1E1E26)

/**
 * 1. Neo-Brutalist Dot Matrix Canvas Background
 */
@Composable
fun NeoBrutalistDotMatrixBackground(
    modifier: Modifier = Modifier,
    backgroundColor: Color = DeepObsidian,
    dotColor: Color = Color(0xFF22232E),
    dotSpacingDp: Float = 24f,
    dotRadiusDp: Float = 2f,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .drawBehind {
                val step = dotSpacingDp.dp.toPx()
                val radius = dotRadiusDp.dp.toPx()

                if ((step > 0.5f) && (size.width > 0f) && (size.height > 0f)) {
                    var x = step / 2f
                    while (x < size.width) {
                        var y = step / 2f
                        while (y < size.height) {
                            drawCircle(
                                color = dotColor,
                                radius = radius,
                                center = Offset(x, y),
                            )
                            y += step
                        }
                        x += step
                    }
                }
            }
    ) {
        // Graphic Watermark / Stamp across middle canvas
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "04 / LETTERS",
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = Color.White.copy(alpha = 0.035f),
                    letterSpacing = (-2).sp
                )
                Text(
                    text = "MATRIX PLAYFIELD // NO. 718",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = Color.White.copy(alpha = 0.025f),
                    letterSpacing = 2.sp
                )
            }
        }

        content()
    }
}

/**
 * 2. Retro CRT Scanline & Monitor Overlay Modifier
 */
@Composable
fun Modifier.retroCrtOverlay(): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "CRTBeamTransition")
    val beamProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "CRTBeamProgress"
    )

    return this.drawWithContent {
        drawContent()

        val w = size.width
        val h = size.height

        // Horizontal scanlines every 4.dp (0.12f opacity)
        val scanlineStep = 4.dp.toPx()
        if ((scanlineStep > 0.5f) && (h > 0f) && (w > 0f)) {
            var y = 0f
            val scanlineColor = Color.Black.copy(alpha = 0.12f)
            while (y <= h) {
                drawLine(
                    color = scanlineColor,
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1f
                )
                y += scanlineStep
            }
        }

        // Downward-sweeping phosphor refresh beam
        val beamY = h * beamProgress
        val beamHeight = 60.dp.toPx()
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = 0.035f),
                    Color.Transparent
                ),
                startY = (beamY - beamHeight).coerceAtLeast(0f),
                endY = (beamY + beamHeight).coerceAtMost(h)
            ),
            topLeft = Offset(0f, (beamY - beamHeight).coerceAtLeast(0f)),
            size = Size(w, beamHeight * 2)
        )

        // Curved CRT glass vignette
        val center = Offset(w / 2f, h / 2f)
        val maxDim = maxOf(w, h)
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.Black.copy(alpha = 0.15f),
                    Color.Black.copy(alpha = 0.45f)
                ),
                center = center,
                radius = maxDim * 0.72f
            )
        )
    }
}

/**
 * 3. Invalid Move Feedback Controller (Shake + Red Border Flash)
 */
class ErrorFeedbackController {
    val shakeOffset = Animatable(0f)
    val borderAnim = Animatable(0f)

    suspend fun triggerError(haptics: HapticFeedback? = null) {
        haptics?.performHapticFeedback(HapticFeedbackType.LongPress)
        coroutineScope {
            launch {
                shakeOffset.snapTo(0f)
                shakeOffset.animateTo(-14f, tween(40))
                shakeOffset.animateTo(14f, tween(50))
                shakeOffset.animateTo(-10f, tween(50))
                shakeOffset.animateTo(10f, tween(50))
                shakeOffset.animateTo(-5f, tween(50))
                shakeOffset.animateTo(5f, tween(50))
                shakeOffset.animateTo(0f, tween(60))
            }
            launch {
                borderAnim.snapTo(1f)
                borderAnim.animateTo(0f, tween(400, easing = FastOutSlowInEasing))
            }
        }
    }
}

@Composable
fun rememberErrorFeedbackController(): ErrorFeedbackController {
    return remember { ErrorFeedbackController() }
}

/**
 * Neo-Brutalist Hard Drop Shadow Box
 */
@Suppress("unused")
@Composable
fun NeoBox(
    modifier: Modifier = Modifier,
    backgroundColor: Color = WarmCream,
    borderColor: Color = InkBlack,
    borderWidth: Dp = 2.dp,
    shadowOffset: Dp = 3.dp,
    cornerRadius: Dp = 6.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = shadowOffset, y = shadowOffset)
                .background(borderColor, RoundedCornerShape(cornerRadius)),
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(cornerRadius))
                .background(backgroundColor)
                .border(borderWidth, borderColor, RoundedCornerShape(cornerRadius)),
            contentAlignment = Alignment.Center,
            content = content
        )
    }
}

/**
 * Unified Two-Tone Arcade Marquee
 */
@Composable
fun UnifiedArcadeMarquee(
    dayNumber: Long,
    movesTaken: Int,
    parMoves: Int,
    targetWord: String,
    onStatsClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(112.dp)
                .offset(x = 3.dp, y = 3.dp)
                .background(InkBlack, RoundedCornerShape(8.dp))
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(112.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(3.dp, InkBlack, RoundedCornerShape(8.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(VermilionRed)
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "MFORMULA",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = WarmCream,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .graphicsLayer { rotationZ = -1.5f }
                            .background(WarmCream, RoundedCornerShape(4.dp))
                            .border(1.5.dp, InkBlack, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "DAY #$dayNumber",
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            color = InkBlack
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(WarmCream, RoundedCornerShape(6.dp))
                            .border(1.5.dp, InkBlack, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("MOVES: ", fontSize = 10.sp, fontWeight = FontWeight.Black, color = InkBlack)
                            Text("$movesTaken ", fontSize = 11.sp, fontWeight = FontWeight.Black, color = VermilionRed)
                            Text("/ PAR: $parMoves", fontSize = 10.sp, fontWeight = FontWeight.Black, color = InkBlack)
                        }
                    }

                    Spacer(Modifier.width(6.dp))

                    // Statistics Icon (Custom Blue 3D Stats Icon)
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(ArcadeYellow)
                            .border(1.5.dp, InkBlack, RoundedCornerShape(6.dp))
                            .clickable { onStatsClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.ic_stats_blue),
                            contentDescription = "Statistics",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(InkBlack)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(CardDark)
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(VermilionRed, RoundedCornerShape(4.dp))
                        .border(1.5.dp, InkBlack, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "TARGET LOCK",
                        color = WarmCream,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.2.sp,
                        fontSize = 10.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    targetWord.forEach { ch ->
                        Box(
                            modifier = Modifier
                                .size(width = 34.dp, height = 38.dp)
                                .background(ElectricMint, RoundedCornerShape(6.dp))
                                .border(2.dp, InkBlack, RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = ch.toString(),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = InkBlack
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Scaled-up Proportional Letter Dial Cartridge
 */
@Composable
fun InteractiveLetterDial(
    char: Char,
    isTargetMatch: Boolean,
    modifier: Modifier = Modifier,
    errorBorderAnim: Float = 0f,
    onCharChanged: (Char) -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    var dragAccumulator by remember { mutableFloatStateOf(0f) }
    var isPressed by remember { mutableStateOf(value = false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "DialScale"
    )

    val bgColor = if (isTargetMatch) ElectricMint else CardDark
    val textColor = if (isTargetMatch) InkBlack else WarmCream
    val currentBorderColor = if (errorBorderAnim > 0f) {
        lerp(InkBlack, VermilionRed, errorBorderAnim)
    } else {
        InkBlack
    }

    Box(
        modifier = modifier
            .height(110.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = 3.dp, y = 3.dp)
                .background(InkBlack, RoundedCornerShape(8.dp))
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .background(bgColor)
                .border(2.5.dp, currentBorderColor, RoundedCornerShape(8.dp))
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .background(if (isTargetMatch) ElectricMint else Color(0xFF2B2C3A))
                        .clickable {
                            isPressed = true
                            val nextChar = if (char == 'Z') 'A' else char + 1
                            onCharChanged(nextChar)
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "▲",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isTargetMatch) InkBlack else WarmCream
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .pointerInput(char) {
                            detectVerticalDragGestures(
                                onDragStart = { isPressed = true },
                                onDragEnd = { isPressed = false; dragAccumulator = 0f },
                                onDragCancel = { isPressed = false; dragAccumulator = 0f }
                            ) { _, dragAmount ->
                                dragAccumulator += dragAmount
                                if (dragAccumulator > 20f) {
                                    val prevChar = if (char == 'A') 'Z' else char - 1
                                    onCharChanged(prevChar)
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    dragAccumulator = 0f
                                } else if (dragAccumulator < -20f) {
                                    val nextChar = if (char == 'Z') 'A' else char + 1
                                    onCharChanged(nextChar)
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    dragAccumulator = 0f
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = char.toString(),
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = textColor
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .background(if (isTargetMatch) ElectricMint else Color(0xFF2B2C3A))
                        .clickable {
                            isPressed = true
                            val prevChar = if (char == 'A') 'Z' else char - 1
                            onCharChanged(prevChar)
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "▼",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isTargetMatch) InkBlack else WarmCream
                    )
                }
            }
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100.milliseconds)
            isPressed = false
        }
    }
}

/**
 * Stamped Receipt Slip Card
 */
@Composable
fun FlippingLetterCard(
    char: Char,
    isTargetMatch: Boolean,
    index: Int,
    isNewRow: Boolean
) {
    val density = LocalDensity.current.density
    val rotationX = remember(char, isNewRow) { Animatable(if (isNewRow) -90f else 0f) }

    LaunchedEffect(isNewRow, char) {
        if (isNewRow) {
            delay((index * 60).milliseconds)
            rotationX.animateTo(0f, animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
        }
    }

    Box(
        modifier = Modifier
            .padding(horizontal = 2.dp)
            .size(width = 44.dp, height = 48.dp)
            .graphicsLayer {
                this.rotationX = rotationX.value
                cameraDistance = 12f * density
            }
            .clip(RoundedCornerShape(6.dp))
            .background(if (isTargetMatch) ElectricMint else WarmCream)
            .border(2.dp, InkBlack, RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = char.toString(),
            color = InkBlack,
            fontWeight = FontWeight.Black,
            fontSize = 22.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

/**
 * Thick 64.dp Tall Arcade Switch CTA Slam-Button
 */
@Composable
fun ArcadeConfirmButton(
    enabled: Boolean,
    text: String = "CONFIRM MOVE ➔",
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(value = false) }

    val translateY by animateDpAsState(
        targetValue = if (isPressed) 3.dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "ButtonTranslateY"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable(enabled = enabled) {
                isPressed = true
                onClick()
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = 0.dp, y = 4.dp)
                .background(InkBlack, RoundedCornerShape(10.dp))
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = translateY)
                .clip(RoundedCornerShape(10.dp))
                .background(if (enabled) VermilionRed else Color(0xFF4A4B5A))
                .border(3.dp, InkBlack, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = WarmCream,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                letterSpacing = 0.5.sp
            )
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(90.milliseconds)
            isPressed = false
        }
    }
}

/**
 * Arcade Confetti Particle Explosion
 */
private data class ConfettiChit(
    val color: Color,
    val initialXRatio: Float,
    val initialYRatio: Float,
    val vx: Float,
    val vy: Float,
    val sizeWidth: Float,
    val sizeHeight: Float,
    val rotationSpeed: Float,
    val isRectangle: Boolean
)

@Composable
fun ArcadeConfetti(
    trigger: Boolean,
    modifier: Modifier = Modifier
) {
    if (!trigger) return

    val progress = remember { Animatable(0f) }
    val chits = remember {
        val palette = listOf(
            VermilionRed,
            ElectricMint,
            ArcadeYellow,
            WarmCream,
            Color(0xFF5856D6)
        )
        List(75) {
            ConfettiChit(
                color = palette.random(),
                initialXRatio = 0.5f + ((Random.nextFloat() - 0.5f) * 0.25f),
                initialYRatio = 0.55f + ((Random.nextFloat() - 0.5f) * 0.1f),
                vx = (Random.nextFloat() - 0.5f) * 480f,
                vy = (-Random.nextFloat() * 450f) - 250f,
                sizeWidth = Random.nextFloat() * 10f + 6f,
                sizeHeight = (Random.nextFloat() * 14f) + 6f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 1080f,
                isRectangle = Random.nextBoolean()
            )
        }
    }

    LaunchedEffect(Unit) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(1500, easing = LinearEasing))
    }

    if (progress.value < 1f) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val t = progress.value
            val dt = t * 1.5f
            val gravity = 900f * 1.25f

            val alpha = if (t > 0.75f) ((1f - t) / 0.25f).coerceIn(0f, 1f) else 1f

            chits.forEach { chit ->
                val drag = 0.97.pow(t * 60.0).toFloat()
                val x = size.width * chit.initialXRatio + chit.vx * dt * drag
                val y = (size.height * chit.initialYRatio) + (chit.vy * dt * drag) + (0.5f * gravity * dt * dt)
                val rot = chit.rotationSpeed * dt

                withTransform(
                    transformBlock = {
                        translate(left = x, top = y)
                        rotate(degrees = rot, pivot = Offset.Zero)
                    }
                ) {
                    if (chit.isRectangle) {
                        drawRect(
                            color = chit.color.copy(alpha = alpha),
                            topLeft = Offset(-chit.sizeWidth / 2f, -chit.sizeHeight / 2f),
                            size = Size(chit.sizeWidth, chit.sizeHeight)
                        )
                    } else {
                        drawRect(
                            color = chit.color.copy(alpha = alpha),
                            topLeft = Offset(-chit.sizeWidth / 2f, -chit.sizeWidth / 2f),
                            size = Size(chit.sizeWidth, chit.sizeWidth)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Wordle-Style Clipboard Share Text Generator
 */
fun generateShareCardText(
    puzzle: DailyPuzzle,
    moves: List<String>
): String {
    val movesCount = moves.size - 1
    val diff = movesCount - puzzle.parMoves
    val rating = when {
        diff <= -2 -> "EAGLE!"
        diff == -1 -> "BIRDIE!"
        diff == 0 -> "PAR EXACT"
        diff == 1 -> "BOGEY"
        else -> "+$diff OVER PAR"
    }

    val grid = buildString {
        for (i in 1 until moves.size) {
            val word = moves[i]
            for (c in 0 until 4) {
                if (word[c] == puzzle.targetWord[c]) {
                    append("🟩")
                } else {
                      append("⬜")
                }
            }
            appendLine()
        }
    }.trimEnd()

    return """
        MFORMULA Day #${puzzle.dayNumber}
        Result: $movesCount / Par ${puzzle.parMoves} ($rating)

        $grid
    """.trimIndent()
}

/**
 * Victory Receipt Modal Sheet
 */
@Composable
fun VictorySummaryModal(
    puzzle: DailyPuzzle,
    moves: List<String>,
    onCopyShare: () -> Unit
) {
    val movesCount = (moves.size - 1).coerceAtLeast(0)
    val diff = movesCount - puzzle.parMoves
    val ratingBadge = when {
        diff <= -2 -> "EAGLE! 🦅"
        diff == -1 -> "BIRDIE! ⚡"
        diff == 0 -> "PAR EXACT 🎯"
        diff == 1 -> "BOGEY 🎯"
        else -> "+$diff OVER PAR"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 0.dp, y = 4.dp)
                .background(InkBlack, RoundedCornerShape(10.dp))
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = WarmCream,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(2.5.dp, InkBlack)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .graphicsLayer { rotationZ = -2.5f }
                        .background(if (diff <= 0) VermilionRed else ArcadeYellow, RoundedCornerShape(6.dp))
                        .border(2.dp, InkBlack, RoundedCornerShape(6.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = ratingBadge,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = if (diff <= 0) WarmCream else InkBlack
                    )
                }

                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardDark, RoundedCornerShape(8.dp))
                        .border(2.dp, InkBlack, RoundedCornerShape(8.dp))
                        .padding(vertical = 10.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("TOTAL MOVES", fontSize = 10.sp, fontWeight = FontWeight.Black, color = WarmCream)
                        Text(movesCount.toString(), fontSize = 22.sp, fontWeight = FontWeight.Black, color = ElectricMint)
                    }
                    Text("/", fontSize = 22.sp, fontWeight = FontWeight.Black, color = WarmCream)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("TARGET PAR", fontSize = 10.sp, fontWeight = FontWeight.Black, color = WarmCream)
                        Text(puzzle.parMoves.toString(), fontSize = 22.sp, fontWeight = FontWeight.Black, color = VermilionRed)
                    }
                }

                Spacer(Modifier.height(16.dp))

                ArcadeConfirmButton(
                    enabled = true,
                    text = "COPY SHARE CARD ➔",
                    onClick = onCopyShare
                )
            }
        }
    }
}

/**
 * Kinetic Fire Streak Badge
 */
@Composable
fun StreakBadge(
    streakCount: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "StreakFlameTransition")

    val yOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "FlameYOffset"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 450, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "FlameRotation"
    )

    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "FlameGlowPulse"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val centerOffset = Offset(size.width / 2f, size.height / 2f)
            val radius = maxOf(size.width, size.height) * 0.75f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        VermilionRed.copy(alpha = 0.35f * glowPulse),
                        ArcadeYellow.copy(alpha = 0.15f * glowPulse),
                        Color.Transparent
                    ),
                    center = centerOffset,
                    radius = radius
                ),
                radius = radius,
                center = centerOffset
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .background(ArcadeYellow, RoundedCornerShape(6.dp))
                .border(2.dp, InkBlack, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "🔥",
                fontSize = 16.sp,
                modifier = Modifier.graphicsLayer {
                    translationY = yOffset
                    rotationZ = rotationAngle
                }
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = streakCount.toString(),
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                color = InkBlack
            )
        }
    }
}

/**
 * Tactile Lock / Unlock Badge (Using Golden Padlock Award Icon)
 */
@Composable
fun ArchiveLockBadge(
    isUnlocked: Boolean,
    modifier: Modifier = Modifier,
    onLockedTap: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val shakeOffset = remember { Animatable(0f) }

    val shackleRotation by animateFloatAsState(
        targetValue = if (isUnlocked) -18f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "ShackleRotation"
    )

    val lockScale by animateFloatAsState(
        targetValue = if (isUnlocked) 1.0f else 0.95f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "LockScale"
    )

    fun triggerShake() {
        coroutineScope.launch {
            shakeOffset.animateTo(-6f, tween(40))
            shakeOffset.animateTo(6f, tween(40))
            shakeOffset.animateTo(-4f, tween(40))
            shakeOffset.animateTo(4f, tween(40))
            shakeOffset.animateTo(0f, tween(40))
        }
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                translationX = shakeOffset.value
                scaleX = lockScale
                scaleY = lockScale
            }
            .clickable {
                if (!isUnlocked) {
                    triggerShake()
                    onLockedTap()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.graphicsLayer {
                    rotationZ = shackleRotation
                }
            ) {
                Image(
                    painter = painterResource(Res.drawable.ic_archive_lock),
                    contentDescription = "Archive Lock",
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.width(6.dp))
            Text(
                text = "ARCHIVE",
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                color = InkBlack
            )
        }
    }
}
