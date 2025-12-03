package com.example.myapplication

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.Player
import com.example.myapplication.data.Score
import com.example.myapplication.SoundManager
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

// Новый класс для бонуса
data class Bonus(
    val id: Int,
    val x: Float,
    val y: Float,
    val type: BonusType = BonusType.Gravity,
    var active: Boolean = true
)

enum class BonusType(val imageRes: Int, val points: Int) {
    Gravity(R.drawable.bug1, 50) // Используем существующее изображение
}

// Sensor State для хранения данных акселерометра
class SensorState {
    var accelerometerX by mutableStateOf(0f)
    var accelerometerY by mutableStateOf(0f)
    var accelerometerZ by mutableStateOf(0f)
}

@Composable
fun rememberSensorState(): SensorState {
    val sensorState = remember { SensorState() }
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(android.content.Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
                    // Инвертируем значения для более интуитивного управления
                    sensorState.accelerometerX = -event.values[0] // Наклон влево/вправо
                    sensorState.accelerometerY = event.values[1]  // Наклон вперед/назад
                    sensorState.accelerometerZ = event.values[2]
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(
            sensorEventListener,
            accelerometer,
            SensorManager.SENSOR_DELAY_GAME
        )

        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
        }
    }

    return sensorState
}

@Composable
fun GameTab(settingsState: SettingsState, player: Player) {
    var score by remember { mutableStateOf(0) }
    var timeLeft by remember { mutableStateOf(settingsState.roundDuration.toInt()) }
    var bugs by remember { mutableStateOf<List<Bug>>(emptyList()) }
    var bonuses by remember { mutableStateOf<List<Bonus>>(emptyList()) }
    var gameActive by remember { mutableStateOf(false) }
    var nextBugId by remember { mutableStateOf(0) }
    var nextBonusId by remember { mutableStateOf(0) }
    var containerSize by remember { mutableStateOf<IntSize?>(null) }
    var gravityEnabled by remember { mutableStateOf(false) }
    var gravityTimeLeft by remember { mutableStateOf(0) }

    val sensorState = rememberSensorState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)

    val soundManager = remember { SoundManager(context) }

    // Очистка ресурсов при уничтожении композиции
    DisposableEffect(Unit) {
        onDispose {
            soundManager.release()
        }
    }


    // Таймер игры
    LaunchedEffect(gameActive) {
        if (gameActive) {
            while (timeLeft > 0 && gameActive) {
                delay(1000)
                timeLeft--
            }
            gameActive = false

            coroutineScope.launch {
                db.playerDao().insertScore(Score(playerId = player.id, score = score, difficulty = player.difficulty))
            }
        }
    }

    // Генерация насекомых
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

    // Генерация бонусов с интервалом из настроек
    LaunchedEffect(gameActive, settingsState.bonusInterval) {
        if (gameActive) {
            while (gameActive) {
                delay((settingsState.bonusInterval * 1000).toLong())
                if (gameActive) {
                    val newBonus = Bonus(
                        id = nextBonusId++,
                        x = Random.nextFloat() * 0.8f + 0.1f, // Не слишком близко к краям
                        y = Random.nextFloat() * 0.8f + 0.1f
                    )
                    bonuses = bonuses + newBonus

                    // Автоматическое удаление бонуса через 5 секунд, если не собран
                    coroutineScope.launch {
                        delay(5000)
                        bonuses = bonuses.filter { it.id != newBonus.id }
                    }
                }
            }
        }
    }

    // Таймер гравитации
    LaunchedEffect(gravityEnabled) {
        if (gravityEnabled) {
            gravityTimeLeft = 10 // 10 секунд действия гравитации
            while (gravityTimeLeft > 0 && gravityEnabled) {
                delay(1000)
                gravityTimeLeft--
            }
            gravityEnabled = false
        }
    }

    // Основной цикл движения насекомых
    LaunchedEffect(gameActive, gravityEnabled) {
        if (!gameActive) return@LaunchedEffect

        while (gameActive) {
            // Обновляем всех жуков
            bugs.forEach { bug ->
                val currentX = bug.x.value
                val currentY = bug.y.value
                var dx = bug.dx
                var dy = bug.dy

                // Если гравитация активна, используем данные акселерометра
                if (gravityEnabled) {
                    // Применяем ускорение от акселерометра
                    val gravityStrength = 0.0005f // Уменьшаем для более плавного движения
                    dx += sensorState.accelerometerX * gravityStrength
                    dy += sensorState.accelerometerY * gravityStrength

                    // Добавляем небольшое трение
                    dx *= 0.98f
                    dy *= 0.98f

                    // Ограничиваем максимальную скорость
                    val maxSpeed = 0.015f
                    val currentSpeed = sqrt(dx * dx + dy * dy)
                    if (currentSpeed > maxSpeed) {
                        dx = dx / currentSpeed * maxSpeed
                        dy = dy / currentSpeed * maxSpeed
                    }

                    // Обновляем направление
                    bug.rotation = (atan2(dy, dx) * (180 / Math.PI)).toFloat() + 90f

                    // Сохраняем новые скорости
                    bug.dx = dx
                    bug.dy = dy
                }

                var nextX = currentX + dx * settingsState.speed
                var nextY = currentY + dy * settingsState.speed

                // Обработка столкновений со стенами
                var bounced = false
                if (nextX <= 0f || nextX >= 1f) {
                    dx = if (gravityEnabled) {
                        -dx * 0.8f // Больше замедления при гравитации
                    } else {
                        -dx
                    }
                    bug.dx = dx
                    nextX = currentX.coerceIn(0.01f, 0.99f)
                    bounced = true
                }
                if (nextY <= 0f || nextY >= 1f) {
                    dy = if (gravityEnabled) {
                        -dy * 0.8f
                    } else {
                        -dy
                    }
                    bug.dy = dy
                    nextY = currentY.coerceIn(0.01f, 0.99f)
                    bounced = true
                }

                // Обновляем поворот только при отскоке или без гравитации
                if (bounced && !gravityEnabled) {
                    bug.rotation = (atan2(dy, dx) * (180 / Math.PI)).toFloat() + 90f
                }

                // Анимируем движение
                coroutineScope.launch {
                    bug.x.animateTo(
                        targetValue = nextX,
                        animationSpec = tween(durationMillis = 100, easing = LinearEasing)
                    )
                }
                coroutineScope.launch {
                    bug.y.animateTo(
                        targetValue = nextY,
                        animationSpec = tween(durationMillis = 100, easing = LinearEasing)
                    )
                }
            }

            delay(50) // Более частые обновления для плавности
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
        Spacer(Modifier.height(8.dp))

        // Отображение статуса гравитации и информации о наклоне
        if (gravityEnabled) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Гравитация активна! Наклоняйте телефон: $gravityTimeLeft сек",
                    style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(8.dp))
        }

        if (!gameActive && timeLeft == 0) {
            Text("Игра окончена! Ваш счет: $score", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))
        }

        if (!gameActive) {
            Button(onClick = {
                score = 0
                timeLeft = settingsState.roundDuration.toInt()
                bugs = emptyList()
                bonuses = emptyList()
                gravityEnabled = false
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
            .pointerInput(gameActive, bugs, bonuses) {
                detectTapGestures(
                    onTap = { tapOffset ->
                        if (!gameActive) return@detectTapGestures

                        // Проверка попадания по бонусу
                        val tappedBonus = bonuses.find { bonus ->
                            val bonusX = bonus.x * size.width
                            val bonusY = bonus.y * size.height

                            val distance = sqrt(
                                (tapOffset.x - bonusX).pow(2) + (tapOffset.y - bonusY).pow(2)
                            )
                            distance < imageSizePx / 2 && bonus.active
                        }

                        if (tappedBonus != null) {
                            soundManager.playBonusSound()
                            gravityEnabled = true
                            score += tappedBonus.type.points
                            // Удаляем собранный бонус
                            bonuses = bonuses.filter { it.id != tappedBonus.id }
                        } else {
                            // Проверка попадания по насекомому
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
                                score = (score - 5).coerceAtLeast(0)
                            }
                        }
                    }
                )
            }) {
            containerSize?.let { size ->
                if (gameActive) {
                    // Отображение бонусов
                    bonuses.forEach { bonus ->
                        if (bonus.active) {
                            Image(
                                painter = painterResource(id = bonus.type.imageRes),
                                contentDescription = "Bonus",
                                modifier = Modifier
                                    .size(imageSizeDp)
                                    .offset {
                                        IntOffset(
                                            x = (size.width * bonus.x - imageSizePx / 2).toInt(),
                                            y = (size.height * bonus.y - imageSizePx / 2).toInt()
                                        )
                                    }
                            )
                        }
                    }

                    // Отображение насекомых
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