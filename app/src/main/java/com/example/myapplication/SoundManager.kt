package com.example.myapplication


import android.content.Context
import android.media.SoundPool
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SoundManager(private val context: Context) {
    private var soundPool: SoundPool
    private var bonusSoundId: Int = 0
    private var soundLoaded = false

    init {
        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .build()

        soundPool.setOnLoadCompleteListener { _, _, status ->
            soundLoaded = status == 0
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                bonusSoundId = soundPool.load(context, R.raw.gravity_bonus, 1)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playBonusSound() {
        if (soundLoaded && bonusSoundId != 0) {
            soundPool.play(bonusSoundId, 1.0f, 1.0f, 1, 0, 1.0f)
        }
    }

    fun release() {
        soundPool.release()
    }
}