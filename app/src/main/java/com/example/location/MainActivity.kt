package com.example.location
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.location.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.lang.Exception
import java.util.Locale
import SensorData
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.widget.TextView

class MainActivity : AppCompatActivity(), SensorEventListener {
    lateinit var binding:ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var cityName=""
    private var country=""
    private var myaddress=""
    private lateinit var gyroscopeXTextView: TextView
    private lateinit var gyroscopeYTextView: TextView
    private lateinit var gyroscopeZTextView: TextView

    //Definieren der Sensoren
    private lateinit var sensorManager: SensorManager
    private lateinit var gyroscopeSensor: Sensor

    private var gyroData: SensorData? = null

    //Messwerte default auf 0 setzen
    private var gyroscopeX=0.0F
    private var gyroscopeY=0.0F
    private var gyroscopeZ=0.0F
    private var timeGyroscope:Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fusedLocationClient =LocationServices.getFusedLocationProviderClient(this)
        gyroscopeXTextView = binding.gyroscopeX
        gyroscopeYTextView = binding.gyroscopeY
        gyroscopeZTextView = binding.gyroscopeZ

        initSensors()

        binding.locationBtn.setOnClickListener{
            if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
                //request the premition
                ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_CODE

                    )

                    }else{
                        // premission has already been granted
                        getLocation()
                    }
        }
    }

    private fun getLocation(){
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null){
                getCityName(location.latitude,location.longitude)
                binding.cityName.text="City Name: "+cityName
                binding.countryName.text="Country Name: "+country
                binding.Address.text="Address: "+myaddress
                binding.Latitude.text="Latitude: "+location.latitude.toString()
                binding.Longitude.text="Longitude: "+location.longitude.toString()

            }
        }
    }
    //Die Sensoren werden initalisiert falls noch nicht vorhanden
    private fun initSensors(){
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        if(sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null){
            gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)!!
        }
    }

    //Registrieren der Sensoren um nutzungsbereit zu sein
    private fun registerListener(){
        if (sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
            sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    private fun unregisterListener(){
        sensorManager.unregisterListener(this,gyroscopeSensor)
    }

    override fun onResume() {
        super.onResume()
        registerListener() // Aktiviere den Gyroskop-Sensor, wenn die Aktivität wieder aufgenommen wird
    }

    override fun onPause() {
        super.onPause()
        unregisterListener() // Deaktiviere den Gyroskop-Sensor, wenn die Aktivität pausiert wird
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onSensorChanged(event: SensorEvent?) {
        if(event!!.sensor.type == Sensor.TYPE_GYROSCOPE){
            getGyroscopeData(event)

            runOnUiThread {
                gyroscopeXTextView.text = "Gyroscope X: $gyroscopeX"
                gyroscopeYTextView.text = "Gyroscope Y: $gyroscopeY"
                gyroscopeZTextView.text = "Gyroscope Z: $gyroscopeZ"
            }
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == LOCATION_PERMISSION_CODE){
            if(grantResults.isNotEmpty()&& grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getLocation()
            }
        }
    }
    companion object{
        const val LOCATION_PERMISSION_CODE=1001
    }
    private fun getCityName(lat:Double,long:Double){
        try {
            val geoCoder=Geocoder(this, Locale.getDefault())
            val address = geoCoder.getFromLocation(lat,long,3)
            if (address!=null){
                myaddress=address[0].getAddressLine(0)
                country = address[0].countryName
                cityName=address[0].locality

            }
        }catch (e:Exception){
            Toast.makeText( this,"loading city",Toast.LENGTH_SHORT).show()
        }
    }

    private fun getGyroscopeData(e: SensorEvent){
        if(gyroData == null){
            gyroData = SensorData(e!!.values[0], e!!.values[1], e!!.values[2], e!!.timestamp)
            timeGyroscope = System.currentTimeMillis()
        }
        else{
            val deltaTime = (e.timestamp - gyroData!!.timestamp) / 1_000_000_000f // Zeitdifferenz in Sekunden
            gyroscopeX += gyroData!!.dataA * deltaTime
            gyroscopeY += gyroData!!.dataB * deltaTime
            gyroscopeZ += gyroData!!.dataC * deltaTime
            gyroData = SensorData(e.values[0], e.values[1], e.values[2], e.timestamp)

        }
    }
}