package com.app.householdtracing.sensors

import android.content.Context

class MagneticCalculation(private var context: Context) {

    private var magnetismVariation = 0f
    private var MagnetismValue = 0f
    private val magnetismStrength =
        floatArrayOf(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f)


    private var indoorConfidence = 0f

    fun inDoorCon() = indoorConfidence

    fun confidenceLevelFromMagneticField() {

        /*updateAdapter(
            Item(
                title = "Magnetic ${magnetismStrength[9]} - ${magnetismStrength[0]}",
                type = 3
            )
        )*/

        var magnetismAvailable = true
        for (i in 0..8) {
            magnetismStrength[i] = magnetismStrength[i + 1]
        }
        magnetismStrength[9] = MagnetismValue
        if (magnetismStrength[0] == 0f) {
            magnetismAvailable = false
        } else {
            magnetismVariation = DetectSensorsHomeActivity.varianceImperative(magnetismStrength)
            if (magnetismVariation > 150) indoorConfidence += 3
        }
    }

    fun updateValue(sensorValue: Float) {
        MagnetismValue = sensorValue
    }

    fun resetConfidence() {
        indoorConfidence = 0f
    }
}