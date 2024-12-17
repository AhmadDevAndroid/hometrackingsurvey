package com.app.householdtracing.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.app.householdtracing.App.Companion.APP_TAG
import com.app.householdtracing.util.AppNotificationManager
import com.app.householdtracing.util.AppUtil.showLogError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SensorDetectionManager(private val context: Context) {

    private var evaluationJob: Job? = null
    private val evaluationResults = mutableListOf<String>()
    private val gpsCalc = GpsCalculation(context)
    private val magCalc = MagneticCalculation(context)
    private val cellDetection = CellularDetection(context)
    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val notificationManager by lazy { AppNotificationManager(context) }
    private var detectionCallback: ((String) -> Unit)? = null
    private val sensorListeners = mutableListOf<SensorEventListener>()

    //Sensors
    fun startSensorDetection() {
        evaluationResults.clear()
        registerSensors()
        startEvaluationTimer()
    }

    fun stopSensorDetection() {
        evaluationJob?.cancel()
        unregisterSensors()
    }

    private fun startEvaluationTimer() {
        evaluationJob = CoroutineScope(Dispatchers.IO).launch {
            repeat(4) {
                delay(75000L)
                evaluateEnvironment()
            }
            finalizeDetection()
        }
    }

    private fun finalizeDetection() {
        val mostFrequent =
            evaluationResults.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key
        showLogError(APP_TAG, "Final Result: User is $mostFrequent")
        notificationManager.setUpNotification(APP_TAG, "User is $mostFrequent")
        detectionCallback?.invoke(mostFrequent ?: "Unknown")
        stopSensorDetection()
    }

    private fun registerSensors() {
        unregisterSensors()
        listOf(
            Sensor.TYPE_LIGHT to ::onLightChanged,
            Sensor.TYPE_PROXIMITY to ::onProximityChanged,
            Sensor.TYPE_MAGNETIC_FIELD to ::onMagneticFieldChanged
        ).forEach { (type, handler) ->
            sensorManager.getDefaultSensor(type)?.let {
                val listener = createSensorListener(handler)
                sensorManager.registerListener(
                    listener, it, SensorManager.SENSOR_DELAY_NORMAL
                )
                sensorListeners.add(listener)
            }
        }
    }

    private fun unregisterSensors() {
        sensorListeners.forEach { sensorManager.unregisterListener(it) }
        sensorListeners.clear()
    }

    private fun createSensorListener(handler: (Float) -> Unit) = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                handler(it.values[0])
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    private fun onLightChanged(value: Float) = gpsCalc.updateLight(value)
    private fun onProximityChanged(value: Float) = gpsCalc.updateProximity(value)
    private fun onMagneticFieldChanged(value: Float) = magCalc.updateValue(value)

    private fun evaluateEnvironment() {
        magCalc.calculateConfidence()
        cellDetection.calculateCellularInfo()
        cellDetection.confidenceLevelFromCellular()

        gpsCalc.getConfidenceLevelFromSatellites(
            count = cellDetection.currentCellID, snr = cellDetection.currentCellularSNR
        )

        val confidenceScores = listOf(
            "Indoor" to maxOf(
                gpsCalc.getIndoorConfidence(),
                magCalc.getIndoorConfidence(),
                cellDetection.inDoorCon()
            ),
            "Outdoor" to gpsCalc.getOutdoorConfidence(),
            "Semi-Outdoor" to gpsCalc.getSemiOutdoorConfidence()
        )

        val highestConfidence = confidenceScores.maxByOrNull { it.second }?.first ?: "Unknown"
        evaluationResults.add(highestConfidence)
        showLogError(APP_TAG, "User is $highestConfidence")
        resetAllConfidences()
    }

    private fun resetAllConfidences() {
        gpsCalc.resetConfidence()
        magCalc.resetConfidence()
        cellDetection.resetConfidence()
    }
}