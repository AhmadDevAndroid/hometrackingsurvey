package com.app.householdtracing.sensors

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.TELEPHONY_SERVICE
import android.telephony.CellInfoCdma
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.CellInfoWcdma
import android.telephony.TelephonyManager

class CellularDetection(private var context: Context) {

    private var previousCellID = 0
    var currentCellID = 0
    var currentCellularSNR = 0f
    private val cellularSNR = FloatArray(10) { 0f } // Initialize the array with 10 elements

    private var outdoorConfidence = 0f
    private var indoorConfidence = 0f

    private val telephonyManager: TelephonyManager by lazy {
        context.getSystemService(TELEPHONY_SERVICE) as TelephonyManager
    }

    fun outDoorCon() = outdoorConfidence
    fun inDoorCon() = indoorConfidence

    fun confidenceLevelFromCellular() {
        // If cellularSNR[0] is 0, we assume the signal is inconsistent
        if (cellularSNR[0] != 0f) {
            val cellularVariation = cellularSNR[9] - cellularSNR[0]
            when {
                cellularVariation > 10 -> outdoorConfidence += 6
                cellularVariation < -10 -> indoorConfidence += 6
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun calculateCellularInfo() {
        telephonyManager.allCellInfo?.firstOrNull { it.isRegistered }?.let { cellInfo ->
            val (newCellID, newCellularSNR) = when (cellInfo) {
                is CellInfoWcdma -> cellInfo.cellSignalStrength.dbm.toFloat() to cellInfo.cellIdentity.cid
                is CellInfoGsm -> cellInfo.cellSignalStrength.dbm.toFloat() to cellInfo.cellIdentity.cid
                is CellInfoLte -> cellInfo.cellSignalStrength.dbm.toFloat() to cellInfo.cellIdentity.ci
                is CellInfoCdma -> cellInfo.cellSignalStrength.dbm.toFloat() to cellInfo.cellIdentity.basestationId
                else -> return
            }

            // If the cell ID changes, reset the signal strength array
            if (newCellID.toInt() == previousCellID) {
                updateCellularSNR(newCellularSNR)
            } else {
                resetCellularSNR(newCellularSNR)
            }
            previousCellID = newCellID.toInt()
        }
    }

    private fun updateCellularSNR(newSNR: Int) {
        for (i in 0 until cellularSNR.size - 1) {
            cellularSNR[i] = cellularSNR[i + 1]
        }
        cellularSNR[9] = newSNR.toFloat()
    }

    private fun resetCellularSNR(newSNR: Int) {
        cellularSNR.fill(0f)
        cellularSNR[9] = newSNR.toFloat()
    }

    fun resetConfidence() {
        indoorConfidence = 0f
        outdoorConfidence = 0f
    }
}
