package com.app.householdtracing.sensors

import android.content.Context
import java.util.Calendar

class GpsCalculation(private var context: Context) {

    private val cal = Calendar.getInstance()

    private var GPSSnrUpperThreshold = 0f
    private var GPSSnrMiddleThreshold = 0f
    private var GPSSnrLowerThreshold = 0f
    private var GPSCountUpperThreshold = 0f
    private var GPSCountMiddleThreshold = 0f
    private var GPSCountLowerThreshold = 0f

    private var ProximityValue = 0f
    private var LightValue = 0f
    private var GPSSnrTrendMax = 0.0f

    private var firstRound = true

    private var round_count = 0


    private val GPSSnrValue = floatArrayOf(
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        0.0f
    )
    private val GPSSnrTrend = floatArrayOf(
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        0.0f
    )

    private var indoorConfidence = 0f
    private var semioutdoorConfidence = 0f
    private var outdoorConfidence = 0f

    fun getUpper() = GPSSnrUpperThreshold
    fun getLower() = GPSSnrLowerThreshold
    fun getMiddle() = GPSSnrMiddleThreshold

    fun calculateTrend(avgSnrGps: Float) {

        for (i in 0..28) {
            GPSSnrValue[i] = GPSSnrValue[i + 1]
        }
        GPSSnrValue[29] = avgSnrGps


        //detection round count
        round_count++

        //the first 30-sec round ends
        if (round_count == 30) {
            firstRound = false
        }
        if (!firstRound) {
            for (i in 0..19) {
                GPSSnrTrend[i] = GPSSnrValue[i] - GPSSnrValue[i + 9]
            }

            //snr variation in past 30 secs
            GPSSnrTrendMax = GPSSnrTrend[0]

            for (i in 1 until GPSSnrTrend.size) {
                if (GPSSnrTrend[i] > GPSSnrTrendMax) {
                    GPSSnrTrendMax = GPSSnrTrend[i]
                }
            }

        }
    }

    fun updateProximity(value: Float) {
        ProximityValue = value
    }

    fun updateLight(value: Float) {
        LightValue = value
    }

    fun setWindowLevel(screenWidth: Int) {

        if (screenWidth < 1200) { // 1080
            GPSSnrUpperThreshold = 23f
            GPSSnrMiddleThreshold = 18f
            GPSSnrLowerThreshold = 15f
        } else { // 1440
            GPSSnrUpperThreshold = 25f
            GPSSnrMiddleThreshold = 22f
            GPSSnrLowerThreshold = 19f
        }
        GPSCountUpperThreshold = 4.5f
        GPSCountMiddleThreshold = 3.5f
        GPSCountLowerThreshold = 1.5f
    }

    fun getConfidenceLevelFromSatellites(count: Int, snr: Float) {

        /*updateAdapter(
            Item(
                title = "Proximity-> $proximityCount Light-> $LightValue Count-> $count SNR-> $snr GPSSnrTrendMax-> $GPSSnrTrendMax",
                1
            )
        )*/

        // logWriter?.startLoggingDetection("TOP Sensors $ProximityValue && $LightValue")

        if (ProximityValue > 3) {

            if (LightValue > 3000 && count > GPSCountMiddleThreshold) {
                outdoorConfidence += 10
            } else {
                if (snr > GPSSnrUpperThreshold) {
                    if (count > GPSCountMiddleThreshold) {
                        outdoorConfidence += 9
                    } else if (count > GPSCountLowerThreshold) {
                        semioutdoorConfidence += 8
                    } else {
                        indoorConfidence += 9
                    }
                } else {
                    if (snr > GPSSnrMiddleThreshold) {
                        if (count > GPSCountMiddleThreshold) {
                            if (GPSSnrTrendMax > 6.5) {
                                indoorConfidence += 9
                            } else {
                                semioutdoorConfidence += 8
                            }
                        } else {
                            indoorConfidence += 8
                        }
                    } else {
                        if (count < GPSCountUpperThreshold || snr < GPSSnrLowerThreshold) {
                            indoorConfidence += 10
                        } else {
                            if (cal[Calendar.HOUR_OF_DAY] in 10..16) { //daytime
                                if (LightValue < 1500) {
                                    indoorConfidence += 9
                                }
                            } else if (GPSSnrTrendMax > 6.5) {
                                indoorConfidence += 7
                            }
                        }
                    }
                }
            }
        } else {

            // logWriter?.startLoggingDetection("else Details (Count) $count && (SNR) $snr && Threshold $GPSSnrTrendMax")

            if (count > 4.5 && snr > GPSCountUpperThreshold - 2) {
                outdoorConfidence += 9
            }
            if (count > 4.5 && GPSSnrTrendMax > 6.5 && snr < GPSCountMiddleThreshold - 2) {
                indoorConfidence += 7
            }
            if (count > 2.5 && snr > GPSSnrMiddleThreshold - 2 && snr < GPSCountUpperThreshold - 2) {
                semioutdoorConfidence += 7
            }
            if (count < 2.5 && snr < GPSCountLowerThreshold - 2) {
                indoorConfidence += 9
            }
            if (count < 0.5) {
                indoorConfidence += 10
            }
        }
    }

    fun outDoorCon() = outdoorConfidence
    fun inDoorCon() = indoorConfidence
    fun semiOutDoorCon() = semioutdoorConfidence

    fun resetConfidence() {
        indoorConfidence = 0f
        outdoorConfidence = 0f
        semioutdoorConfidence = 0f
    }


}