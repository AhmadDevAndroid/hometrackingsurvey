package com.app.householdtracing.sensors

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.GnssStatus
import android.location.GpsStatus
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class DetectSensorsHomeActivity : AppCompatActivity(), SensorEventListener, View.OnClickListener {

    private var IOSensorManager: SensorManager? = null
    private var IOProximity: Sensor? = null
    private var ProximitySensorAvailable = false

    private var IOLight: Sensor? = null
    private var LightSensorAvailable = false

    private var IOMagnetism: Sensor? = null
    private var MagnetismSensorAvailable = false

    private var cellulardetection: CellularDetection? = null
    private var magneticCalculation: MagneticCalculation? = null
    private var gpsCalculation: GpsCalculation? = null


//    private var logWriter: LogWriter? = null
//    lateinit var recyclerView: RecyclerView
//    lateinit var adapter: ItemAdapter

    lateinit var values: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED //  Fixed Portrait orientation

      //  values = findViewById(R.id.values)
        cellulardetection = CellularDetection(this)
        gpsCalculation = GpsCalculation(this)
        magneticCalculation = MagneticCalculation(this)

//        recyclerView = findViewById(R.id.recyclerView)
//        recyclerView.layoutManager = LinearLayoutManager(this)
//        adapter = ItemAdapter(arrayListOf())
//        recyclerView.adapter = adapter
//
//        logWriter = LogWriter(this)
        //        View view = this.getWindow().getDecorView();
//        view.setBackgroundColor(Color.BLUE);

//        val getStarted = findViewById<Button>(R.id.getStarted)
//        getStarted.setOnClickListener(this)


        setProperThreshold()

        IOSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        //list all available sensors
        val IOList = IOSensorManager!!.getSensorList(Sensor.TYPE_ALL)
        for (sensor in IOList) {
            if (sensor.type == Sensor.TYPE_LIGHT) {
                LightSensorAvailable = true
            }
            if (sensor.type == Sensor.TYPE_PROXIMITY) {
                ProximitySensorAvailable = true
            }
            if (sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                MagnetismSensorAvailable = true
            }
        }
        if (LightSensorAvailable) {
            IOLight = IOSensorManager!!.getDefaultSensor(Sensor.TYPE_LIGHT)
            IOSensorManager!!.registerListener(this, IOLight, SensorManager.SENSOR_DELAY_NORMAL)
        }
        if (ProximitySensorAvailable) {
            IOProximity = IOSensorManager!!.getDefaultSensor(Sensor.TYPE_PROXIMITY)
            IOSensorManager!!.registerListener(this, IOProximity, SensorManager.SENSOR_DELAY_NORMAL)
        }
        if (MagnetismSensorAvailable) {
            IOMagnetism = IOSensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
            IOSensorManager!!.registerListener(this, IOMagnetism, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }


  //  private fun getScreenWidthAndHeight(context: Activity): Pair<Int, Int> {
       // return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Use WindowMetrics for API level 30+ (Android 11)
//            val windowMetrics: androidx.window.layout.WindowMetrics =
//                WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(context)
//            val bounds = windowMetrics.bounds
//            Pair(bounds.width(), bounds.height())
//        } else {
//            // Use DisplayMetrics for older versions
//            val displayMetrics = DisplayMetrics()
//            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
//            @Suppress("DEPRECATION") windowManager.defaultDisplay.getMetrics(displayMetrics)
//            Pair(displayMetrics.widthPixels, displayMetrics.heightPixels)
//        }
//    }

    fun setProperThreshold() {

      //  val data = getScreenWidthAndHeight(this)

      //  printLogs("\nDimension -> ${data.first}:${data.second}\n")

      //  gpsCalculation?.setWindowLevel(data.first)

        values.text =
            "GPSUpper: [4.5f] GPSMiddle: [3.5] GPSLower: [1.5f]\nGPSSnrUpper: [${gpsCalculation?.getUpper()}] GPSSnrLow: [${gpsCalculation?.getLower()}] GPSSnrMid: [${gpsCalculation?.getMiddle()}]"
    }

    private fun printLogs(text: String) {
       // logWriter?.startLogging(text)
    }

    override fun onSensorChanged(event: SensorEvent) {

        if (event.sensor.type == Sensor.TYPE_PROXIMITY) {
            gpsCalculation?.updateProximity(event.values[0])
        }

        if (event.sensor.type == Sensor.TYPE_LIGHT) {
            gpsCalculation?.updateLight(event.values[0])
        }

        if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            val sensorValue =
                Math.sqrt((event.values[0] * event.values[0] + event.values[1] * event.values[1] + event.values[2] * event.values[2]).toDouble())
                    .toFloat()
            magneticCalculation?.updateValue(sensorValue)
        }

    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    val manager: LocationManager by lazy {
        getSystemService(LOCATION_SERVICE) as LocationManager
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun initLocation() {
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Please open GPS service", Toast.LENGTH_SHORT).show()
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivityForResult(intent, 0)
            return
        }
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder(this).setTitle("Location permission required")
                    .setMessage("You have to give the permission to access location")
                    .setPositiveButton("OK") { dialogInterface, i ->
                        ActivityCompat.requestPermissions(
                            this@DetectSensorsHomeActivity,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
                        )
                    }
                    .setNegativeButton("Cancel") { dialogInterface, i -> dialogInterface.dismiss() }
                    .create().show()
            } else {
                ActivityCompat.requestPermissions(
                    this@DetectSensorsHomeActivity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
                )
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                manager.registerGnssStatusCallback(callback, Handler(Looper.getMainLooper()))
            } else {
                manager.addGpsStatusListener(gpsStatusListener)
            }
            manager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 2000, 1f, locationListener
            )
        }
    }

    @SuppressLint("NewApi")
    private val callback: GnssStatus.Callback = object : GnssStatus.Callback() {
        override fun onSatelliteStatusChanged(status: GnssStatus) {
            super.onSatelliteStatusChanged(status)
            onGpsFound(status)
        }

        override fun onFirstFix(ttffMillis: Int) {
            super.onFirstFix(ttffMillis)
            Log.e("Gps", "onFirstFix")
        }

        override fun onStarted() {
            super.onStarted()
            Log.e("Gps", "onStarted")
        }

        override fun onStopped() {
            super.onStopped()
            Log.e("Gps", "onStopped")
        }
    }

    private val gpsStatusListener = GpsStatus.Listener {
        //onGpsFound();
    }

    @SuppressLint("NewApi")
    private fun onGpsFound(status: GnssStatus) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        var countGps = 0
        var snrGps = 0f

        for (index in 0 until status.satelliteCount) {
            val prn = status.getSvid(index)
            val snr = status.getCn0DbHz(index)
            if (snr > 0.0) {
                if (prn < 33) { // gps 1-32
                    countGps++
                    snrGps += snr
                }
            }
        }

        val avgSnrGps: Float = snrGps / countGps
        onNewSignalFound(avgSnrGps, countGps)

    }

    private fun onNewSignalFound(avgSnrGps: Float, countGps: Int) {
        gpsCalculation?.calculateTrend(avgSnrGps)
        cellulardetection?.calculateCeullularInfo()
        IODetection(countGps, avgSnrGps)
    }

    fun IODetection(count: Int, snr: Float) {
      //  val statusNow = findViewById<TextView>(R.id.showResult)
        gpsCalculation?.getConfidenceLevelFromSatellites(count, snr)
        cellulardetection?.confidenceLevelFromCellular()
        magneticCalculation?.confidenceLevelFromMagneticField()

        val outdoorConfidence =
            gpsCalculation?.outDoorCon()?.plus(cellulardetection?.outDoorCon() ?: 0f) ?: 0f
        val indoorConfidence =
            gpsCalculation?.inDoorCon()?.plus(cellulardetection?.inDoorCon() ?: 0f)
                ?.plus(magneticCalculation?.inDoorCon() ?: 0f) ?: 0f
        val semioutDoorConfidence = gpsCalculation?.semiOutDoorCon() ?: 0f

        if (outdoorConfidence > indoorConfidence && outdoorConfidence > semioutDoorConfidence) {
            window.decorView.setBackgroundColor(Color.parseColor("#2cb457")) //outdoor

            val status = "OutDoor"
         //   statusNow.text = "Detection result: $status"
            checkStatus("${status}: $outdoorConfidence")

        } else if (semioutDoorConfidence > indoorConfidence && semioutDoorConfidence > outdoorConfidence) {
            window.decorView.setBackgroundColor(Color.parseColor("#ffce26")) // semi

            val status = "semi-outdoor"
         //   statusNow.text = "Detection result: $status"
            checkStatus("${status}: $semioutDoorConfidence")

        } else if (indoorConfidence > outdoorConfidence && indoorConfidence > semioutDoorConfidence) {
            window.decorView.setBackgroundColor(Color.parseColor("#ff6714")) //indoor
          //  statusNow.text = "Detection result: indoor"
            checkStatus("indoor: $indoorConfidence")

        } else if (indoorConfidence == outdoorConfidence && indoorConfidence == semioutDoorConfidence) {
            window.decorView.setBackgroundColor(Color.parseColor("#00c3e3")) //unknown
          //  statusNow.text = "Detection result: unknown"
            checkStatus("unknown")
        }

        gpsCalculation?.resetConfidence()
        cellulardetection?.resetConfidence()
        magneticCalculation?.resetConfidence()

    }

    private fun checkStatus(status: String) {
//        if (adapter.currentState != status) {
//            adapter.currentState = status
//            updateAdapter(Item("", -1, status))
//        }
    }

//    private fun updateAdapter(item: Item) {
//        adapter.update(item)
//        recyclerView.scrollToPosition(adapter.itemCount - 1)
//    }


    override fun onClick(view: View) {
        searchGPSButtonClick(view)
    }

    private var clickOnce = true

    private fun searchGPSButtonClick(v: View?) {
        if (clickOnce) {
           // adapter.clearData()
           // logWriter?.deleteFileFromScopedStorage(this)
            clickOnce = false
//            val startButton = findViewById<TextView>(R.id.getStarted)
//            startButton.text = "Click to stop"
//            val detectionResult = findViewById<TextView>(R.id.showResult)
//            detectionResult.text = "Detection result"
//            val status = findViewById<TextView>(R.id.status)
//            status.text = "Updating every 5 sec"
            window.decorView.setBackgroundColor(Color.parseColor("#ffffff"))
            initLocation()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                manager!!.unregisterGnssStatusCallback(callback)
            }
            clickOnce = true
            /*finish() //restart current activity
            startActivity(intent)*/
            //window.decorView.setBackgroundColor(Color.parseColor("#FFFFFF"))
//            val startButton = findViewById<TextView>(R.id.getStarted)
//            startButton.text = "Click to start"
//            val detectionResult = findViewById<TextView>(R.id.showResult)
//            detectionResult.text = "Detection result"
//            val status = findViewById<TextView>(R.id.status)
//            status.text = "Updating frequency"
        }
    }

    fun indoorMarker(view: View?) {
        val inLabel = "indoor"
        println("indoor marker was clicked.")
        window.decorView.setBackgroundColor(Color.parseColor("#ff6714"))
    }

    fun outdoorMarker(view: View?) {
        val inLabel = "outdoor"
        println("outdoor marker was clicked.")
        window.decorView.setBackgroundColor(Color.parseColor("#2cb457"))
    }

    fun semiMarker(view: View?) {
        val inLabel = "semi"
        println("semi-outdoor marker was clicked.")
        window.decorView.setBackgroundColor(Color.parseColor("#ffce26"))
    }

    fun unknownMarker(view: View?) {
        val inLabel = "unknown"
        println("unknown marker was clicked.")
        window.decorView.setBackgroundColor(Color.parseColor("#00c3e3"))
    }

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            Log.i(
                "location", "latitude and longitude：" + location.latitude + "，" + location.longitude
            )
        }

        override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {}
        override fun onProviderEnabled(s: String) {}
        override fun onProviderDisabled(s: String) {}
    }

    companion object {
        private const val MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
        fun varianceImperative(signal: FloatArray): Float {
            /* var average = 0.0
             for (p in signal) {
                 average += p
             }
             average /= signal.size.toDouble()*/
            val average = signal.average()
            var variance = 0.0
            for (p in signal) {
                variance += (p - average) * (p - average)
            }
            return variance.toFloat() / signal.size
        }
    }
}