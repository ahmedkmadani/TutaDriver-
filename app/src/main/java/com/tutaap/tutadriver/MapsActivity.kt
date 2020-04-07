package com.tutaap.tutadriver

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.material.snackbar.Snackbar
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var map: GoogleMap
    val PERMISSION_ID = 42

    lateinit var mFusedLocationClient: FusedLocationProviderClient
    lateinit var TRUCK_ID: String

    lateinit var token: String
    internal lateinit var user: User

    internal lateinit var viewDialog: ViewDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val user = SharedPrefManager.getInstance(this).user
        viewDialog = ViewDialog(this)

        token = user.token.toString()
        TRUCK_ID = SharedPrefManager.getInstance(this).TRUCKID.toString()

    }


    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map = googleMap

        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMarkerClickListener(this)
        map.uiSettings.isZoomControlsEnabled = true

        map.setOnMarkerClickListener(this)

        getLastLocation()

    }




    private fun InsertInfo(token: String, lat: String, lon: String, TRUCK_ID: String) {

        val stringRequest: StringRequest = object : StringRequest( Method.POST, URLs.URL_STORE_TRUCK_LOCATION,
            Response.Listener { response ->
                try {

                    val jsonObject = JSONObject(response)
                    Log.d("ressTORE", response)

                    onSuccess()

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error ->
                onFailed(error)

            }) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["vehicle_id"] = TRUCK_ID
                params["latitude"] =  lat
                params["longitude"] = lon

                return params
            }

            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $token"
                headers["Content-Type"] = "application/x-www-form-urlencoded"
                return headers
            }

        }
        val requestQueue = Volley.newRequestQueue(this)
        stringRequest.retryPolicy =
            DefaultRetryPolicy(20 * 1000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        requestQueue.add(stringRequest)

    }


    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {

                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    var location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
//                        Log.d("Location", location.latitude.toString() + "" + location.longitude.toString())
//                        InsertInfo(token, location.latitude.toString(), location.longitude.toString(), TRUCK_ID)
                        requestNewLocationData()
                    }
                }
            } else {

                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Turn on location", Snackbar.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 150000
        mLocationRequest.fastestInterval = 20000

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            var mLastLocation: Location = locationResult.lastLocation
            InsertInfo(token, mLastLocation.latitude.toString(), mLastLocation.longitude.toString(), TRUCK_ID)
            map.isMyLocationEnabled = true
            var CurrentLocation = LatLng(mLastLocation.latitude,mLastLocation.longitude)
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(CurrentLocation, 12f))

        }
    }

    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_ID
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_ID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLastLocation()
            }
        }
    }
    override fun onMarkerClick(p0: Marker?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    private fun onFailed(error: VolleyError) {
        Snackbar.make(
            findViewById(android.R.id.content),
            error.toString(), Snackbar.LENGTH_LONG).show()
        Log.d("error", error.toString())
    }

    private fun onSuccess() {

    }
}
