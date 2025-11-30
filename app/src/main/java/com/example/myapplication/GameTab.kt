package com.example.myapplication

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

data class Bug(
    val id: Int,
    val type: BugType,
    val x: Animatable<Float, *>,
    val y: Animatable<Float, *>,
    var dx: Float,
    var dy: Float,
    var rotation: Float = 0f
)

enum class BugType(val imageRes: Int, val points: Int) {
    Bug1(R.drawable.bug1, 10),
    Bug2(R.drawable.bug2, 20),
    Bug3(R.drawable.bug3, 30),
}

@Composable
fun GameTab(settingsState: SettingsState) {
    var score by remember { mutableStateOf(0) }
    var timeLeft by remember { mutableStateOf(settingsState.roundDuration.toInt()) }
    var bugs by remember { mutableStateOf<List<Bug>>(emptyList()) }
    var gameActive by remember { mutableStateOf(false) }
    var nextBugId by remember { mutableStateOf(0) }
    var containerSize by remember { mutableStateOf<IntSize?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(gameActive) {
        if (gameActive) {
            while (timeLeft > 0 && gameActive) {
                delay(1000)
                timeLeft--
            }
            gameActive = false // Game over
        }
    }

    LaunchedEffect(gameActive, settingsState.maxCockroaches) {
        if (gameActive) {
            while (gameActive) {
                if (bugs.size < settingsState.maxCockroaches.toInt()) {
                    val angle = Random.nextFloat() * 2 * Math.PI.toFloat()
                    val startX = Random.nextFloat()
                    val startY = Random.nextFloat()
                    val newBug = Bug(
                        id = nextBugId++,
                        x = Animatable(startX),
                        y = Animatable(startY),
                        dx = kotlin.math.cos(angle) * 0.01f,
                        dy = kotlin.math.sin(angle) * 0.01f,
                        type = BugType.entries.toTypedArray().random(),
                        rotation = (atan2(kotlin.math.sin(angle), kotlin.math.cos(angle)) * (180 / Math.PI)).toFloat() + 90f
                    )
                    bugs = bugs + newBug
                }
                delay(1000)
            }
        }
    }
    LaunchedEffect(bugs, gameActive, settingsState.speed) {
        if (!gameActive) return@LaunchedEffect

        bugs.forEach { bug ->
            coroutineScope.launch {
                while (gameActive && bugs.any { it.id == bug.id }) {
                    val currentX = bug.x.value
                    val currentY = bug.y.value
                    var dx = bug.dx
                    var dy = bug.dy

                    var nextX = currentX + dx * settingsState.speed
                    var nextY = currentY + dy * settingsState.speed

                    if (nextX <= 0f || nextX >= 1f) {
                        dx = -dx
                        bug.dx = dx
                        nextX = currentX + dx * settingsState.speed
                        bug.rotation = (atan2(dy, dx) * (180 / Math.PI)).toFloat() + 90f
                    }
                    if (nextY <= 0f || nextY >= 1f) {
                        dy = -dy
                        bug.dy = dy
                        nextY = currentY + dy * settingsState.speed
                        bug.rotation = (atan2(dy, dx) * (180 / Math.PI)).toFloat() + 90f
                    }

                    launch {
                        bug.x.animateTo(
                            targetValue = nextX,
                            animationSpec = tween(durationMillis = 100, easing = LinearEasing)
                        )
                    }
                    launch {
                        bug.y.animateTo(
                            targetValue = nextY,
                            animationSpec = tween(durationMillis = 100, easing = LinearEasing)
                        )
                    }

                    delay(100)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Игра 'Жуки'", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Text("Счет: $score")
            Text("Время: $timeLeft")
        }
        Spacer(Modifier.height(16.dp))

        if (!gameActive && timeLeft == 0) {
            Text("Игра окончена! Ваш счет: $score", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))
        }

        if (!gameActive) {
            Button(onClick = {
                score = 0
                timeLeft = settingsState.roundDuration.toInt()
                bugs = emptyList()
                gameActive = true
            }) {
                Text(if (timeLeft == 0) "Играть снова" else "Начать игру")
            }
        }

        val imageSizeDp = 48.dp
        val imageSizePx = with(LocalDensity.current) { imageSizeDp.toPx() }

        BoxWithConstraints(modifier = Modifier
            .fillMaxSize()
            .weight(1f)
            .onSizeChanged { containerSize = it }
            .pointerInput(gameActive, bugs) {
                detectTapGestures(
                    onTap = { tapOffset ->
                        if (!gameActive) return@detectTapGestures

                        val tappedBug = bugs.find { bug ->
                            val bugX = bug.x.value * size.width
                            val bugY = bug.y.value * size.height

                            val distance = sqrt(
                                (tapOffset.x - bugX).pow(2) + (tapOffset.y - bugY).pow(2)
                            )
                            distance < imageSizePx / 2
                        }

                        if (tappedBug != null) {
                            score += tappedBug.type.points
                            bugs = bugs.filter { it.id != tappedBug.id }
                        } else {
                            score -= 5
                        }
                    }
                )
            }) {
            containerSize?.let { size ->
                if (gameActive) {
                    bugs.forEach { bug ->
                        Image(
                            painter = painterResource(id = bug.type.imageRes),
                            contentDescription = "Bug",
                            modifier = Modifier
                                .size(imageSizeDp)
                                .offset {
                                    IntOffset(
                                        x = (size.width * bug.x.value - imageSizePx / 2).toInt(),
                                        y = (size.height * bug.y.value - imageSizePx / 2).toInt()
                                    )
                                }
                                .rotate(bug.rotation)
                        )
                    }
                } else if (bugs.isEmpty() && timeLeft > 0) {
                    Text("Нажмите 'Начать игру'", modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}
