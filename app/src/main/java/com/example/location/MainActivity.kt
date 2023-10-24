package com.example.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.GeolocationPermissions
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.location.databinding.ActivityMainBinding
import com.google.android.gms.location.ActivityRecognitionClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.lang.Exception
import java.util.Locale

class MainActivity : AppCompatActivity() {
    lateinit var binding:ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var cityName=""
    private var country=""
    private var myaddress=""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fusedLocationClient =LocationServices.getFusedLocationProviderClient(this)

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
}