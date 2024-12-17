package com.app.householdtracing.sensors

import android.content.Context
import kotlin.math.pow

class MagneticCalculation(context: Context) {

    private val magnetismValues = FloatArray(10)
    private var indoorConfidence = 0f
    fun getIndoorConfidence() = indoorConfidence

    fun updateValue(sensorValue: Float) {
        System.arraycopy(magnetismValues, 1, magnetismValues, 0, magnetismValues.size - 1)
        magnetismValues[magnetismValues.size - 1] = sensorValue
    }

    fun calculateConfidence() {
        if (magnetismValues[0] != 0f) {
            val magnetismVariation = calculateVariance(magnetismValues)
            if (magnetismVariation > 150) {
                indoorConfidence += 3
            }
        }
    }

    fun resetConfidence() {
        indoorConfidence = 0f
    }

    private fun calculateVariance(values: FloatArray): Float {
        val average = values.average().toFloat()
        return values.fold(0f) { acc, value ->
            acc + (value - average).pow(2)
        } / values.size
    }
}