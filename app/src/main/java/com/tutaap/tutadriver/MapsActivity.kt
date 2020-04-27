package com.tutaap.tutadriver

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.LinearLayout
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
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.messaging.RemoteMessage
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.channel.PrivateChannelEventListener
import com.pusher.client.connection.ConnectionEventListener
import com.pusher.client.connection.ConnectionState
import com.pusher.client.connection.ConnectionStateChange
import com.pusher.client.util.HttpAuthorizer
import com.pusher.pushnotifications.PushNotificationReceivedListener
import com.pusher.pushnotifications.PushNotifications
import kotlinx.android.synthetic.main.bottom_sheet.*
import kotlinx.android.synthetic.main.bottom_sheet.btn_cancel
import kotlinx.android.synthetic.main.bottom_sheet.user_location_drop
import kotlinx.android.synthetic.main.bottom_sheet.user_location_pickup
import kotlinx.android.synthetic.main.bottom_sheet_start_trip.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var map: GoogleMap
    val PERMISSION_ID = 42

    lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var TRUCK_ID: Int = 0

    lateinit var token: String
    internal lateinit var user: User

    internal lateinit var viewDialog: ViewDialog
    private var DriverId: Int = 0

    private lateinit var sheetBehaviorOne: BottomSheetBehavior<LinearLayout>
    private lateinit var sheetBehaviorTwo: BottomSheetBehavior<LinearLayout>

    lateinit var addressText: String
    lateinit var TRIPID: String

    lateinit var user_pickup_location: String
    lateinit var user_drop_Location: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        sheetBehaviorOne = BottomSheetBehavior.from(bottom_sheet)
        sheetBehaviorOne.state = BottomSheetBehavior.STATE_HIDDEN

        sheetBehaviorTwo = BottomSheetBehavior.from(bottom_sheet_start_trip)
        sheetBehaviorTwo.state = BottomSheetBehavior.STATE_HIDDEN

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val user = SharedPrefManager.getInstance(this).user
        DriverId = user.Id!!.toInt()

        viewDialog = ViewDialog(this)

        token = user.token.toString()
        TRUCK_ID = SharedPrefManager.getInstance(this).TRUCKID!!

        Log.d("token", token)
        val options = PusherOptions()

        options.setCluster("eu")

        val headers: HashMap<String, String> = HashMap()
        headers["Authorization"] = "Bearer $token"
        headers["Content-Type"] = "application/x-www-form-urlencoded"
        headers["Accept"] = "application/json"

        val authorizer = HttpAuthorizer(URLs.URL_AUTH)
        options.setAuthorizer(authorizer).isEncrypted
        authorizer.setHeaders(headers)

        val pusher = Pusher("0d7a677e7fd7526b0c97", options)

        pusher.connect(object: ConnectionEventListener {
            override fun onConnectionStateChange(change:ConnectionStateChange) {
                println(("State changed from " + change.previousState +
                        " to " + change.currentState))
            }

            override fun onError(message:String, code:String, e:Exception) {
                println(("There was a problem connecting! " +
                        "\ncode: " + code +
                        "\nmessage: " + message +
                        "\nException: " + e)
                )
            }
        }, ConnectionState.ALL)


        val channel = pusher.subscribePrivate("private-App.User.$TRUCK_ID", object:
            PrivateChannelEventListener {
            override fun onEvent(channel: String?, eventName: String?, data: String?) {
                Log.d("Channel", channel)
                Log.d("Event Name", eventName)
                Log.d("Data", data)
            }

            override fun onAuthenticationFailure(p0: String?, p1: java.lang.Exception?) {
                Log.d("error", p1.toString())
            }

            override fun onSubscriptionSucceeded(p0: String?) {
                Log.d("res", p0)
            }
        })


        channel.bind("Illuminate\\Notifications\\Events\\BroadcastNotificationCreated", object : PrivateChannelEventListener {
            override fun onEvent(channel: String?, eventName: String?, data: String?) {
                Log.d("Channel", channel)
                Log.d("Event Name", eventName)
                Log.d("Data", data)

                val jsonObject = JSONObject(data)

                val start_latitude = jsonObject.getString("start_latitude")
                val start_longitude = jsonObject.getString("start_longitude")

                val stop_latitude = jsonObject.getString("stop_latitude")
                val stop_longitude = jsonObject.getString("stop_longitude")

                TRIPID = jsonObject.getString("trip_id")

            runOnUiThread {

                val pickup_location = LatLng(start_latitude!!.toDouble(), start_longitude!!.toDouble())
                val dropoff_location = LatLng(stop_latitude!!.toDouble(), stop_longitude!!.toDouble())

                map.addMarker(MarkerOptions().position(pickup_location).title("Pick Location"))
                map.addMarker(MarkerOptions().position(dropoff_location).title("Drop off Location"))

                expandCloseSheet(pickup_location,dropoff_location)


            }
        }

            override fun onAuthenticationFailure(p0: String?, p1: java.lang.Exception?) {
                Log.d("error", p1.toString())
            }

            override fun onSubscriptionSucceeded(p0: String?) {
                Log.d("res", p0)
            }

        })


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
                    Log.d("Response ", response.toString())
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

    private fun expandCloseSheet(pickup_location: LatLng, dropoffLocation: LatLng) {
        if (sheetBehaviorOne.state != BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehaviorOne.state = BottomSheetBehavior.STATE_EXPANDED

             user_pickup_location = getAddress(pickup_location)
             user_drop_Location = getAddress(dropoffLocation)

            user_location_pickup.text = user_pickup_location
            user_location_drop.text = user_drop_Location

            btn_accept.setOnClickListener {
                AcceptTrip(TRIPID,TRUCK_ID,token,user_pickup_location,user_drop_Location)
            }

            btn_cancel.setOnClickListener {
                sheetBehaviorOne.state = BottomSheetBehavior.STATE_HIDDEN
                map.clear()
            }

        } else {
            sheetBehaviorOne.state = BottomSheetBehavior.STATE_COLLAPSED

             user_pickup_location = getAddress(pickup_location)
             user_drop_Location = getAddress(dropoffLocation)

            user_location_pickup.text = user_pickup_location
            user_location_drop.text = user_drop_Location

        }

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(pickup_location, 12f))
    }


    private fun AcceptTrip(
        tripid: String,
        truckId: Int,
        token: String,
        userPickupLocation: String,
        userDropLocation: String
    ) {
        viewDialog.showDialog()
        val stringRequest: StringRequest = object : StringRequest( Method.GET, URLs.URL_START_TRIP + "${tripid.toInt()}/start/${truckId}",
            Response.Listener { response ->
                try {

                    val jsonObject = JSONObject(response)
                    val data = jsonObject.getJSONObject("data")
                    val trip = data.getJSONObject("trip")
                    val client = trip.getJSONObject("client")

                    val firstName = client.getString("first_name")
                    val lastName = client.getString("last_name")

                    TripStart(firstName,lastName,userPickupLocation,userDropLocation)

                } catch (e: JSONException) {
                    e.printStackTrace()
                    viewDialog.hideDialog()
                }
            },
            Response.ErrorListener { error ->
                onFailed(error)
                Log.d("debug", error.toString())
                viewDialog.hideDialog()
            }) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
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

    private fun TripStart(firstName: String, lastName: String, userPickupLocation: String, userDropLocation: String) {

            viewDialog.hideDialog()

            sheetBehaviorOne.state = BottomSheetBehavior.STATE_HIDDEN
            sheetBehaviorTwo.state = BottomSheetBehavior.STATE_EXPANDED

            user_name.text = firstName + " " + lastName
            user_trip_pickup.text = userPickupLocation
            user_trip_drop.text = userDropLocation


            btn_cancel.setOnClickListener {

            }

    }

    private fun getAddress(location: LatLng): String {
        val geocoder = Geocoder(this)
        val addresses: List<Address>?
        addressText = ""

        try {

            addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (null != addresses && !addresses.isEmpty()) {

                addressText = addresses[0].getAddressLine(0)

            }
        } catch (e: IOException) {
            Log.d("MapsActivity", e.localizedMessage)
            Snackbar.make(
                findViewById(android.R.id.content),
                e.localizedMessage, Snackbar.LENGTH_LONG).show()
        }

        return addressText
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




