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
    private var currentCellID = 0
    private var currentCellularSNR = 0f
    private val cellularSNR =
        floatArrayOf(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f)

    private var outdoorConfidence = 0f
    private var indoorConfidence = 0f

    private val telephonyManager by lazy {
        context.getSystemService(TELEPHONY_SERVICE) as TelephonyManager
    }

    fun outDoorCon() = outdoorConfidence
    fun inDoorCon() = indoorConfidence

    fun confidenceLevelFromCellular() {

        //updateAdapter(Item(title = "cellularSNR ${cellularSNR[9]} - ${cellularSNR[0]}", type = 2))

        var cellularConsistent = true
        var cellularVariation = 0f
        if (cellularSNR[0] == 0f) {
            cellularConsistent = false
        } else {
            cellularVariation = cellularSNR[9] - cellularSNR[0]
            if (cellularVariation > 10) {
                outdoorConfidence += 6
            } else if (cellularVariation < -10) {
                indoorConfidence += 6
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun calculateCeullularInfo() {

        var servingCellTower: String? = null

        val cellInfo = telephonyManager.allCellInfo

        if (cellInfo.isNotEmpty()) {
            for (i in cellInfo.indices) {
                if (cellInfo[i].isRegistered) { //current cell tower is the serving cell tower
                    if (cellInfo[i] is CellInfoWcdma) {
                        val cellInfoWcdma = cellInfo[i] as CellInfoWcdma
                        val cellSignalStrengthWcdma = cellInfoWcdma.cellSignalStrength
                        currentCellID = cellInfoWcdma.cellIdentity.cid
                        currentCellularSNR = cellSignalStrengthWcdma.dbm.toFloat()
                        servingCellTower =
                            "WCDMA cell " + cellInfoWcdma.cellIdentity.cid + "," + cellSignalStrengthWcdma.dbm
                    } else if (cellInfo[i] is CellInfoGsm) {
                        val cellInfogsm = cellInfo[i] as CellInfoGsm
                        val cellSignalStrengthGsm = cellInfogsm.cellSignalStrength
                        currentCellID = cellInfogsm.cellIdentity.cid
                        currentCellularSNR = cellSignalStrengthGsm.dbm.toFloat()
                        servingCellTower =
                            "GSM cell " + cellInfogsm.cellIdentity.cid + "," + cellSignalStrengthGsm.dbm
                    } else if (cellInfo[i] is CellInfoLte) {
                        val cellInfoLte = cellInfo[i] as CellInfoLte
                        val cellSignalStrengthLte = cellInfoLte.cellSignalStrength
                        currentCellID = cellInfoLte.cellIdentity.ci
                        currentCellularSNR = cellSignalStrengthLte.dbm.toFloat()
                        servingCellTower =
                            "LTE cell " + cellInfoLte.cellIdentity.ci + "," + cellSignalStrengthLte.dbm
                    } else if (cellInfo[i] is CellInfoCdma) {
                        val cellInfoCdma = cellInfo[i] as CellInfoCdma
                        val cellSignalStrengthCdma = cellInfoCdma.cellSignalStrength
                        currentCellID = cellInfoCdma.cellIdentity.basestationId
                        currentCellularSNR = cellSignalStrengthCdma.dbm.toFloat()
                        servingCellTower =
                            "CDMA cell " + cellInfoCdma.cellIdentity.basestationId + "," + cellSignalStrengthCdma.dbm
                    }
                    if (currentCellID == previousCellID) {
                        for (j in 0..8) {
                            cellularSNR[j] = cellularSNR[j + 1]
                        }
                        cellularSNR[9] = currentCellularSNR
                        previousCellID = currentCellID
                    } else {
                        for (k in 0..8) {
                            cellularSNR[k] = 0f
                        }
                        cellularSNR[9] = currentCellularSNR
                        previousCellID = currentCellID
                    }
                }
            }
        }
    }

    fun resetConfidence() {
        indoorConfidence = 0f
        outdoorConfidence = 0f
    }
}