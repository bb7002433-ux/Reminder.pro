package com.example.util

import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

object AlertSoundVibrationHelper {

    /**
     * Plays the default system notification sound once.
     */
    fun playSystemAlertSound(context: Context) {
        try {
            val alertUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val ringtone = RingtoneManager.getRingtone(context, alertUri)
            if (ringtone != null && !ringtone.isPlaying) {
                ringtone.play()
            }
        } catch (e: Exception) {
            Log.e("AlertHelper", "Error playing notification sound", e)
        }
    }

    /**
     * Triggers a triple vibration pattern indicating alert/warning state.
     */
    fun triggerVibrationAlert(context: Context) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Pattern: Vibrate 200ms, pause 150ms, vibrate 200ms, pause 150ms, vibrate 300ms
                    val timings = longArrayOf(0, 200, 150, 200, 150, 300)
                    val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255) // max intensity
                    val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                    vibrator.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(500)
                }
            }
        } catch (e: Exception) {
            Log.e("AlertHelper", "Error triggering vibration", e)
        }
    }
}
