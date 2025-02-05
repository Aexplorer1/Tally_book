package com.mushroom.worklog.utils

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.mushroom.worklog.R

class SoundHelper(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    fun playSettleSound() {
        try {
            // 播放音效
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(context, R.raw.cash_register)
            mediaPlayer?.setOnCompletionListener { mp ->
                mp.release()
                mediaPlayer = null
            }
            mediaPlayer?.start()

            // 执行震动
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // 为新版本创建震动效果：短-长-短的震动模式
                val vibrationEffect = VibrationEffect.createWaveform(
                    longArrayOf(0, 50, 50, 100, 50, 50),  // 时间模式：等待-震动-等待-震动-等待-震动
                    intArrayOf(0, 80, 0, 180, 0, 80),     // 强度模式：对应每个时间段的强度
                    -1  // 不重复
                )
                vibrator.vibrate(vibrationEffect)
            } else {
                // 兼容旧版本
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 50, 50, 100, 50, 50), -1)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
        vibrator.cancel()
    }
} 