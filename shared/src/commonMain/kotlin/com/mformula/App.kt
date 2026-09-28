package com.mformula

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mformula.domain.engine.WordEngine
import com.mformula.domain.stats.StatsRepository
import com.mformula.ui.components.*
import com.mformula.ui.theme.MformulaTheme
import kotlinx.coroutines.launch
import mformula.shared.generated.resources.Res
import mformula.shared.generated.resources.ic_archive_lock
import org.jetbrains.compose.resources.painterResource

object RevenueCatHook {
    val isProUnlocked = mutableStateOf(value = false)

    fun purchasePro() {
        isProUnlocked.value = true
    }
}

@Composable
fun App() {
    MformulaTheme {
        val statsRepo = remember { StatsRepository() }
        val todayEpoch = remember { System.currentTimeMillis() / 86400000L }

        LaunchedEffect(Unit) {
            WordEngine.initialize()
        }

        var activeEpoch by remember { mutableLongStateOf(todayEpoch) }
        var currentTab by remember { mutableIntStateOf(0) }
        var showStats by remember { mutableStateOf(value = false) }

        val puzzle = remember(activeEpoch) { WordEngine.getDailyPuzzle(activeEpoch) }
        var activeWord by remember(puzzle) { mutableStateOf(puzzle.startWord) }
        var chain by remember(puzzle) { mutableStateOf(listOf(puzzle.startWord)) }
        var stepError by remember { mutableStateOf<String?>(null) }
        var copied by remember { mutableStateOf(value = false) }

        val isSolved = chain.last() == puzzle.targetWord
        val movesTaken = (chain.size - 1).coerceAtLeast(0)
        val isParCrushed = isSolved && (movesTaken <= puzzle.parMoves)

        val haptics = LocalHapticFeedback.current
        val errorController = rememberErrorFeedbackController()
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(isSolved) {
            if (isSolved) {
                statsRepo.recordSolve(activeEpoch, movesTaken, puzzle.parMoves)
            }
        }

        val clipboard = LocalClipboardManager.current

        Scaffold(
            bottomBar = {
                // Grounded Bottom Console
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(WarmCream)
                        .border(width = 2.5.dp, color = InkBlack)
                        .padding(vertical = 6.dp, horizontal = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Daily Tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (currentTab == 0) ArcadeYellow else Color.Transparent)
                                .border(
                                    width = if (currentTab == 0) 2.dp else 0.dp,
                                    color = if (currentTab == 0) InkBlack else Color.Transparent,
                                    shape = RoundedCornerShape(6.dp),
                                )
                                .clickable {
                                    currentTab = 0
                                    activeEpoch = todayEpoch
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⚡", fontSize = 16.sp)
                                Spacer(Modifier.width(6.dp))
                                Text("DAILY", fontWeight = FontWeight.Black, fontSize = 13.sp, color = InkBlack)
                            }
                        }

                        Spacer(Modifier.width(12.dp))

                        // Archive Tab (Using Custom Golden Padlock Award Icon)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (currentTab == 1) ArcadeYellow else Color.Transparent)
                                .border(
                                    width = if (currentTab == 1) 2.dp else 0.dp,
                                    color = if (currentTab == 1) InkBlack else Color.Transparent,
                                    shape = RoundedCornerShape(6.dp),
                                )
                                .clickable { currentTab = 1 },
                            contentAlignment = Alignment.Center
                        ) {
                            ArchiveLockBadge(
                                isUnlocked = RevenueCatHook.isProUnlocked.value,
                                onLockedTap = {
                                    currentTab = 1
                                },
                            )
                        }
                    }
                }
            }
        ) { padding ->
            // 1. Neo-Brutalist Dot Matrix Background
            NeoBrutalistDotMatrixBackground(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // 2. Retro CRT Scanline Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .retroCrtOverlay()
                ) {
                    if (currentTab == 0) {
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + slideInVertically { it / 8 }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Unified Two-Tone Arcade Marquee Header (With Custom Blue Statistics Icon)
                                UnifiedArcadeMarquee(
                                    dayNumber = puzzle.dayNumber,
                                    movesTaken = movesTaken,
                                    parMoves = puzzle.parMoves,
                                    targetWord = puzzle.targetWord,
                                ) { showStats = true }

                                // Graphic Matrix Playfield Container
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .offset(x = 3.dp, y = 3.dp)
                                            .background(InkBlack, RoundedCornerShape(8.dp)),
                                    )
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(CardDark)
                                            .border(3.dp, InkBlack, RoundedCornerShape(8.dp))
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        // Word Ladder Section
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxWidth(),
                                        ) {
                                            // Vertical Ink Circuit Line
                                            Canvas(modifier = Modifier.fillMaxHeight().width(32.dp).align(Alignment.CenterStart)) {
                                                drawLine(
                                                    color = InkBlack,
                                                    start = Offset(24.dp.toPx(), 8.dp.toPx()),
                                                    end = Offset(24.dp.toPx(), size.height - 8.dp.toPx()),
                                                    strokeWidth = 3.dp.toPx(),
                                                )
                                            }

                                            LazyColumn(
                                                modifier = Modifier.fillMaxSize(),
                                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                            ) {
                                                itemsIndexed(chain) { rowIndex, w ->
                                                    val isNewRow = (rowIndex == chain.lastIndex) && (chain.size > 1)
                                                    val angleTilt = if ((rowIndex % 2) == 0) -1f else 1f

                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .graphicsLayer { rotationZ = angleTilt },
                                                        horizontalArrangement = Arrangement.Center,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .padding(end = 8.dp)
                                                                .background(if (rowIndex == 0) VermilionRed else WarmCream, RoundedCornerShape(4.dp))
                                                                .border(1.5.dp, InkBlack, RoundedCornerShape(4.dp))
                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Text(
                                                                text = if (rowIndex < 9) "0${rowIndex + 1}" else (rowIndex + 1).toString(),
                                                                fontFamily = FontFamily.Monospace,
                                                                fontWeight = FontWeight.Black,
                                                                fontSize = 11.sp,
                                                                color = if (rowIndex == 0) WarmCream else InkBlack
                                                            )
                                                        }

                                                        w.forEachIndexed { charIdx, ch ->
                                                            val isTargetMatch = ch == puzzle.targetWord[charIdx]
                                                            FlippingLetterCard(
                                                                char = ch,
                                                                isTargetMatch = isTargetMatch,
                                                                index = charIdx,
                                                                isNewRow = isNewRow,
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // Error Banner Stamped Tag
                                        val currentError = stepError
                                        if (currentError != null) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                                    .background(VermilionRed, RoundedCornerShape(6.dp))
                                                    .border(1.5.dp, InkBlack, RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = currentError,
                                                    color = WarmCream,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }
                                        }

                                        // Hardware Dials Row or Victory Summary Sheet
                                        if (isSolved) {
                                            VictorySummaryModal(
                                                puzzle = puzzle,
                                                moves = chain,
                                            ) {
                                                val cardText = generateShareCardText(puzzle, chain)
                                                clipboard.setText(AnnotatedString(cardText))
                                                copied = true
                                            }
                                        } else {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 8.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .graphicsLayer { translationX = errorController.shakeOffset.value },
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    activeWord.forEachIndexed { i, ch ->
                                                        InteractiveLetterDial(
                                                            char = ch,
                                                            isTargetMatch = ch == puzzle.targetWord[i],
                                                            errorBorderAnim = errorController.borderAnim.value,
                                                            onCharChanged = { nextCh ->
                                                                val arr = activeWord.toCharArray()
                                                                if (i < arr.size) {
                                                                    arr[i] = nextCh
                                                                    activeWord = String(arr)
                                                                }
                                                            },
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .height(110.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Thick 64.dp Tall Slam Button
                                if (!isSolved) {
                                    ArcadeConfirmButton(
                                        enabled = true,
                                        text = "CONFIRM MOVE ➔",
                                    ) {
                                        val prev = chain.last()
                                        when {
                                            !WordEngine.trie.contains(activeWord) -> {
                                                stepError = "'$activeWord' is not in dictionary!"
                                                coroutineScope.launch { errorController.triggerError(haptics) }
                                            }
                                            !WordEngine.isOneEditAway(prev, activeWord) -> {
                                                stepError = "Must be 1 change from $prev!"
                                                coroutineScope.launch { errorController.triggerError(haptics) }
                                            }
                                            activeWord in chain -> {
                                                stepError = "Word already visited!"
                                                coroutineScope.launch { errorController.triggerError(haptics) }
                                            }
                                            else -> {
                                                stepError = null
                                                chain += activeWord
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        ArchivePaywallView(
                            todayEpoch = todayEpoch,
                            isUnlocked = RevenueCatHook.isProUnlocked.value,
                        ) { pastDay ->
                            activeEpoch = pastDay
                            currentTab = 0
                        }
                    }

                    // Arcade Confetti Particle Explosion
                    ArcadeConfetti(trigger = isParCrushed)

                    if (showStats) {
                        val stats = statsRepo.getStats()
                        AlertDialog(
                            onDismissRequest = { showStats = false },
                            containerColor = WarmCream,
                            shape = RoundedCornerShape(8.dp),
                            title = { Text("STATISTICS", fontWeight = FontWeight.Black, color = InkBlack) },
                            text = {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    StatBox("Played", stats.played.toString())
                                    StreakBadge(streakCount = stats.streak)
                                    StatBox("Max", stats.maxStreak.toString())
                                    StatBox("Best", if (stats.bestParDiff == 999) "-" else stats.bestParDiff.toString())
                                }
                            },
                            confirmButton = {
                                Button(
                                    onClick = { showStats = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = VermilionRed),
                                    border = BorderStroke(2.dp, InkBlack),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("CLOSE", color = WarmCream, fontWeight = FontWeight.Black)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ArchivePaywallView(
    todayEpoch: Long,
    isUnlocked: Boolean,
    onSelectPuzzle: (Long) -> Unit,
) {
    if (!isUnlocked) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .background(ArcadeYellow, RoundedCornerShape(6.dp))
                    .border(2.dp, InkBlack, RoundedCornerShape(6.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(Res.drawable.ic_archive_lock),
                        contentDescription = "Lock",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("MFORMULA PRO", fontWeight = FontWeight.Black, fontSize = 20.sp, color = InkBlack)
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Unlock the complete archive of past daily challenges with a single purchase.",
                color = WarmCream,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(24.dp))
            ArcadeConfirmButton(enabled = true) {
                RevenueCatHook.purchasePro()
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(14) { i ->
                val pastEpoch = todayEpoch - (i + 1)
                val p = WordEngine.getDailyPuzzle(pastEpoch)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectPuzzle(pastEpoch) }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .offset(x = 3.dp, y = 3.dp)
                            .background(InkBlack, RoundedCornerShape(6.dp))
                    )
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        color = WarmCream,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(2.dp, InkBlack)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(p.dateKey, color = InkBlack, fontWeight = FontWeight.Black)
                                Text("${p.startWord} ➔ ${p.targetWord}", color = Color(0xFF555560), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Box(
                                modifier = Modifier
                                    .background(ElectricMint, RoundedCornerShape(6.dp))
                                    .border(1.5.dp, InkBlack, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                            ) {
                                Text("Par ${p.parMoves}", color = InkBlack, fontWeight = FontWeight.Black, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatBox(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Black, color = InkBlack)
        Text(label, fontSize = 11.sp, color = Color(0xFF666670), fontWeight = FontWeight.Bold)
    }
}
