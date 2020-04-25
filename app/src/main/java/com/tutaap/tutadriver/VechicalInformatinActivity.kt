package com.tutaap.tutadriver

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.snackbar.Snackbar
import com.tutaapp.tuta.VolleySingleton
import kotlinx.android.synthetic.main.vechical_info_layout.*
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList
import java.util.HashMap

class VechicalInformatinActivity : AppCompatActivity() {

    lateinit var token: String
    internal lateinit var user: User

    internal lateinit var viewDialog: ViewDialog
    val truck = ArrayList<Trucks>()

    var TrucksArrayName: ArrayList<String>? = null
    private var IdTruck: Int = 0

    private var DriverId: Int = 0
    private var TRUCK_ID: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.vechical_info_layout)

        val user = SharedPrefManager.getInstance(this).user
        viewDialog = ViewDialog(this)

        TrucksArrayName = ArrayList()

        token = user.token.toString()
        DriverId = user.Id!!.toInt()

        getTrucks(token)

        spinner_trucks?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Please Choose Truck category  !!! ",
                    Snackbar.LENGTH_LONG
                ).show()
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                (view as TextView).setTextColor(Color.WHITE)
                IdTruck = truck[position].Id!!
            }
        }

        btn_insert_location.setOnClickListener {
            InsertInfo(token, IdTruck, DriverId)
        }

    }

    private fun InsertInfo(token: String, idTruck: Int, driverId: Int) {
        viewDialog.showDialog()
        val stringRequest: StringRequest = object : StringRequest( Method.POST, URLs.URL_STORE_TRUCK,
            Response.Listener { response ->
                try {

                    val jsonObject = JSONObject(response)
                    val JsonReq = jsonObject.getJSONObject("data")
                    val TrucksObject = JsonReq.getJSONObject("vehicle")

                    TRUCK_ID = TrucksObject.getInt("id")
                    SharedPrefManager.getInstance(applicationContext).TruckId(TRUCK_ID)
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
                params["driver_id"] = driverId.toString()
                params["vehicle_type_id"] =  idTruck.toString()
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




    private fun getTrucks(token: String) {
        viewDialog.showDialog()
        val jRequest = object : JsonObjectRequest(
            Method.GET, URLs.URL_GET_TRUCKS, null,
            Response.Listener { response ->

                val JsonReq = response.getJSONObject("data")
                val TrucksArray = JsonReq.getJSONArray("vehicle_types")

                for(i in 0 until TrucksArray.length()) {

                    val TruckObject = TrucksArray.getJSONObject(i)

                    truck.add(
                        Trucks(
                            TruckObject.getInt("id"),
                            TruckObject.getString("name"),
                            TruckObject.getString("description"),
                            TruckObject.getString("created_at"),
                            TruckObject.getString("updated_at"),
                            TruckObject.getString("deleted_at"),
                            TruckObject.getInt("base_charge"),
                            TruckObject.getDouble("price_per_kilometer"),
                            TruckObject.getDouble("price_per_second"),
                            TruckObject.getInt("average_speed")

                        )

                    )

                    TrucksArrayName!!.add(truck[i].name.toString())

                }

                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, this!!.TrucksArrayName!!)
                adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)

                spinner_trucks.adapter = adapter
                viewDialog.hideDialog()

            },
            Response.ErrorListener {
                viewDialog.hideDialog()
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Sorry we could'nt get truck categories !!! ",
                    Snackbar.LENGTH_LONG
                ).setAction("Try Again") {
                    getTrucks(token)
                }.show()

            }) {
            override fun parseNetworkError(volleyError: VolleyError): VolleyError {
                Log.d("volleyError", ""+ volleyError.message)
                viewDialog.hideDialog()
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Sorry we could'nt get truck categories !!! ",
                    Snackbar.LENGTH_LONG
                ).setAction("Try Again") {
                    getTrucks(token)
                }.show()
                return super.parseNetworkError(volleyError)
            }


            override fun getParams(): Map<String, String> {
                return HashMap()
            }

            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $token"
                return headers
            }
        }

        VolleySingleton.getInstance(this).addToRequestQueue(jRequest)

    }




    private fun onFailed(error: VolleyError) {
        viewDialog.hideDialog()
        Snackbar.make(
            findViewById(android.R.id.content),
            error.toString(), Snackbar.LENGTH_LONG).show()
        Log.d("error", error.toString())
    }

    private fun onSuccess() {
        startActivity(Intent(this, MapsActivity::class.java))
        finish()
        viewDialog.hideDialog()

    }


}