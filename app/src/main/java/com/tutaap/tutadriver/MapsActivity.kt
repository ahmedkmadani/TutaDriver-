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
import android.view.View
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
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.messaging.RemoteMessage
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.channel.ChannelEventListener
import com.pusher.client.connection.ConnectionState
import com.pusher.client.connection.ConnectionStateChange
import com.pusher.pushnotifications.PushNotificationReceivedListener
import com.pusher.pushnotifications.PushNotifications
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap
import javax.sql.ConnectionEvent
import javax.sql.ConnectionEventListener

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var map: GoogleMap
    val PERMISSION_ID = 42

    lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var TRUCK_ID: Int = 0

    lateinit var token: String
    internal lateinit var user: User

    internal lateinit var viewDialog: ViewDialog
    private var DriverId: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val user = SharedPrefManager.getInstance(this).user
        DriverId = user.Id!!.toInt()
        Log.d("Driver Id", DriverId.toString())

        viewDialog = ViewDialog(this)

        token = user.token.toString()
        TRUCK_ID = SharedPrefManager.getInstance(this).TRUCKID!!


        PushNotifications.start(this, "7e59a311-5158-4a17-b767-0fdd58610388")
        PushNotifications.addDeviceInterest("App.User.70")

        val interests = PushNotifications.getDeviceInterests()
        Log.d("intset", interests.toString())

//        val options = PusherOptions()
//        options.setCluster("eu")
//        val pusher = Pusher("0d7a677e7fd7526b0c97", options)
//        options.isEncrypted
//
//        pusher.connect(object :
//            com.pusher.client.connection.ConnectionEventListener {
//            override fun onConnectionStateChange(p0: ConnectionStateChange?) {
//                Log.d("msg One","" + p0!!.currentState)
//            }
//
//            override fun onError(p0: String?, p1: String?, p2: java.lang.Exception?) {
//                Log.d("msg Two", p0)
//            }
//
//        }, ConnectionState.ALL)


//        val channel = pusher.subscribePrivate("App.User.$DriverId")
//        val channel = pusher.getPrivateChannel("private-App.User.$DriverId")

//        var channel = pusher.subscribe("private-App.User.$DriverId", object: ChannelEventListener {
//            override fun onEvent(p0: String?, p1: String?, p2: String?) {
//                TODO("Not yet implemented")
//            }
//
//            override fun onSubscriptionSucceeded(channelName: String) {
//                println("Subscribed!")
//            }
//        })

//        val event = "Illuminate\\Notifications\\Events\\BroadcastNotificationCreated"
//        channel.bind(event.toString()) { channel, eventName, data ->
//            val jsonObject = JSONObject(data)
//            println(data)
//
//            Log.d("connect to channcel", "connecting")
//            Log.d("msg Three ", data.toString())
//            Log.d("msg four", eventName.toString())
//            Log.d("msg five", channel.toString())
//
//            runOnUiThread {
//                Log.d("connect to channcel", "try")
//            }
//        }
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


    private fun InsertInfo(token: String, lat: String, lon: String, TRUCK_ID: Int) {
        val stringRequest: StringRequest = object : StringRequest( Method.POST, URLs.URL_STORE_TRUCK_LOCATION,
            Response.Listener { response ->
                try {

                    val jsonObject = JSONObject(response)
                    onSuccess()

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error ->
                onFailed(error)
                    Log.d("debug", error.toString())
            }) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["vehicle_id"] = TRUCK_ID.toString()
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

    override fun onResume() {
        super.onResume()
        PushNotifications.setOnMessageReceivedListenerForVisibleActivity(this, object :
            PushNotificationReceivedListener {
            override fun onMessageReceived(remoteMessage: RemoteMessage) {

                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Message received: " +
                            "Title: \"${remoteMessage.notification?.title}\"" +
                            "Body \"${remoteMessage.notification?.body}\"",
                    Snackbar.LENGTH_LONG
                )
                    .show()

            }
        })

    }

}


