package com.app.householdtracing.sensors

import android.content.Context
import java.util.Calendar

class GpsCalculation(private val context: Context) {

    private val calendar = Calendar.getInstance()
    private val gpsSnrValues = FloatArray(30)
    private val gpsSnrTrends = FloatArray(20)

    private var gpsSnrUpperThreshold = 0f
    private var gpsSnrMiddleThreshold = 0f
    private var gpsSnrLowerThreshold = 0f

    private var gpsCountUpperThreshold = 0f
    private var gpsCountMiddleThreshold = 0f
    private var gpsCountLowerThreshold = 0f

    private var proximityValue = 0f
    private var lightValue = 0f
    private var gpsSnrTrendMax = 0f

    private var roundCount = 0
    private var firstRound = true

    private var indoorConfidence = 0f
    private var semiOutdoorConfidence = 0f
    private var outdoorConfidence = 0f

    fun updateProximity(value: Float) {
        proximityValue = value
    }

    fun updateLight(value: Float) {
        lightValue = value
    }

    fun setWindowLevel(screenWidth: Int) {
        if (screenWidth < 1200) {
            gpsSnrUpperThreshold = 23f
            gpsSnrMiddleThreshold = 18f
            gpsSnrLowerThreshold = 15f
        } else {
            gpsSnrUpperThreshold = 25f
            gpsSnrMiddleThreshold = 22f
            gpsSnrLowerThreshold = 19f
        }
        gpsCountUpperThreshold = 4.5f
        gpsCountMiddleThreshold = 3.5f
        gpsCountLowerThreshold = 1.5f
    }

    fun calculateTrend(avgSnrGps: Float) {
        gpsSnrValues.copyInto(gpsSnrValues, 0, 1, 30)
        gpsSnrValues[29] = avgSnrGps

        roundCount++
        if (roundCount == 30) firstRound = false

        if (!firstRound) {
            for (i in gpsSnrTrends.indices) {
                gpsSnrTrends[i] = gpsSnrValues[i] - gpsSnrValues[i + 9]
            }
            gpsSnrTrendMax = gpsSnrTrends.maxOrNull() ?: 0f
        }
    }

    fun getConfidenceLevelFromSatellites(count: Int, snr: Float) {
        if (proximityValue > 3) {
            when {
                lightValue > 3000 && count > gpsCountMiddleThreshold -> outdoorConfidence += 10
                snr > gpsSnrUpperThreshold -> adjustConfidence(count, 9, 8, 9)
                snr > gpsSnrMiddleThreshold -> evaluateMiddleSnr(count)
                count < gpsCountUpperThreshold || snr < gpsSnrLowerThreshold -> indoorConfidence += 10
                isDaytime() && lightValue < 1500 -> indoorConfidence += 9
                gpsSnrTrendMax > 6.5 -> indoorConfidence += 7
            }
        } else {
            evaluateLowProximity(count, snr)
        }
    }

    private fun adjustConfidence(count: Int, outdoor: Int, semiOutdoor: Int, indoor: Int) {
        when {
            count > gpsCountMiddleThreshold -> outdoorConfidence += outdoor
            count > gpsCountLowerThreshold -> semiOutdoorConfidence += semiOutdoor
            else -> indoorConfidence += indoor
        }
    }

    private fun evaluateMiddleSnr(count: Int) {
        when {
            count > gpsCountMiddleThreshold && gpsSnrTrendMax <= 6.5 -> semiOutdoorConfidence += 8
            count > gpsCountMiddleThreshold -> indoorConfidence += 9
            else -> indoorConfidence += 8
        }
    }

    private fun evaluateLowProximity(count: Int, snr: Float) {
        when {
            count > 4.5 && snr > gpsCountUpperThreshold - 2 -> outdoorConfidence += 9
            count > 4.5 && gpsSnrTrendMax > 6.5 && snr < gpsSnrMiddleThreshold - 2 -> indoorConfidence += 7
            count > 2.5 && snr in (gpsSnrMiddleThreshold - 2)..(gpsCountUpperThreshold - 2) -> semiOutdoorConfidence += 7
            count < 2.5 && snr < gpsSnrLowerThreshold - 2 -> indoorConfidence += 9
            count < 0.5 -> indoorConfidence += 10
        }
    }

    private fun isDaytime() = calendar[Calendar.HOUR_OF_DAY] in 10..16

    fun resetConfidence() {
        indoorConfidence = 0f
        outdoorConfidence = 0f
        semiOutdoorConfidence = 0f
    }

    fun getOutdoorConfidence() = outdoorConfidence
    fun getIndoorConfidence() = indoorConfidence
    fun getSemiOutdoorConfidence() = semiOutdoorConfidence
}
