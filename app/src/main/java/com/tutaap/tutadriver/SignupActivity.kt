package com.tutaap.tutadriver

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.google.android.material.snackbar.Snackbar
import com.tutaapp.tuta.VolleySingleton
import kotlinx.android.synthetic.main.activity_signup.*
import org.json.JSONObject


class SignupActivity : AppCompatActivity() {

    internal lateinit var viewDialog: ViewDialog
    internal lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        viewDialog = ViewDialog(this)


        btn_sigin.setOnClickListener {
            Signup()
        }

        link_signin.setOnClickListener {
            startActivity(Intent(this@SignupActivity, SigninActivity::class.java))
            finish()
        }
    }

    private fun Signup() {

        if (!validate()) {
            return
        }

        viewDialog.showDialog()

        val FirstName  = input_fname!!.text.toString()
        val LastName  = input_lname!!.text.toString()
        val Email = input_email!!.text.toString()
        val Password = input_password!!.text.toString()
        val ConfirmPassword = input_confirm_password!!.text.toString()
        val isDriver = 1

        val jsonObject = JSONObject()

        try {

            jsonObject.put("first_name", FirstName)
            jsonObject.put("last_name", LastName)
            jsonObject.put("email", Email)
            jsonObject.put("password", Password)
            jsonObject.put("password_confirmation", ConfirmPassword)
            jsonObject.put("is_driver", isDriver)

        } catch (e: Exception) {

        }


        val stringRequest = JsonObjectRequest(
            Request.Method.POST,URLs.URL_REGISTER,jsonObject,
            Response.Listener { response ->

                try {

                    val JsonRes = response.getJSONObject("data")
                    val Token = JsonRes.getString("token")

                    if(Token != null ){

                        getUser(Token)

                    } else {

                        onSiginFailed()
                    }


                }catch (e:Exception){
                    onSiginFailed()
                    Log.d("Exception:", "" + e)
                }

            }, Response.ErrorListener{
                onSiginFailed()
                Log.d("Volley error :", "" + it)

            })


        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest)
    }


    private fun getUser(token: String) {

        val jRequest = object : JsonObjectRequest(
            Method.GET, URLs.URL_ME, null,
            Response.Listener { response ->

                val JsonReq = response.getJSONObject("data")
                val UserArray = JsonReq.getJSONObject("user")

                Log.d("user" , "$JsonReq")

                user = User(

                    UserArray.getString("id"),
                    UserArray.getString("first_name"),
                    UserArray.getString("last_name"),
                    UserArray.getString("email"),
                    UserArray.getString("deleted_at"),
                    UserArray.getString("created_at"),
                    UserArray.getString("updated_at"),
                    UserArray.getString("email_verified_at"),
                    UserArray.getInt("is_driver"),
                    token
                )

                SharedPrefManager.getInstance(applicationContext).userLogin(user)
                onSiginSuccess()
            },
            Response.ErrorListener {
                onSiginFailed()

            }) {
            override fun parseNetworkError(volleyError: VolleyError): VolleyError {
                onSiginFailed()
                Log.d("volleyError", ""+ volleyError.message)
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



    private fun validate(): Boolean {
        var valid = true

        val FirstName  = input_fname!!.text.toString()
        val LastName  = input_lname!!.text.toString()
        val Email = input_email!!.text.toString()
        val Password = input_password!!.text.toString()
        val ConfirmPassword = input_confirm_password!!.text.toString()


        if (FirstName.isEmpty()) {
            input_fname!!.error = "enter a first name please !!"
            valid = false
        } else {
            input_fname!!.error = null
        }

        if (LastName.isEmpty()) {
            input_lname!!.error = "enter a last name please !!"
            valid = false
        } else {
            input_lname!!.error = null
        }

        if (Email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(Email).matches()) {
            input_lname!!.error = "enter a valid email address please !!"
            valid = false
        } else {
            input_lname!!.error = null
        }

        if (Password.isEmpty() || Password.length < 5 || Password.length > 10) {
            input_password!!.error = "between 5 and 10 alphanumeric characters"
            valid = false
        } else {
            input_password!!.error = null
        }

        if (ConfirmPassword.isEmpty() || !ConfirmPassword.contentEquals(Password)) {
            input_confirm_password!!.error = "password does'nt match !!"
            valid = false
        } else {
            input_confirm_password!!.error = null
        }

        return valid
    }


    private fun onSiginSuccess() {
        viewDialog.hideDialog()
        Snackbar.make(
            findViewById(android.R.id.content),
            "Sign up Successfully",
            Snackbar.LENGTH_LONG
        )
            .show()
        startActivity(Intent(this, VechicalInformatinActivity::class.java))
        finish()
    }

    private fun onSiginFailed() {
        viewDialog.hideDialog()
        Snackbar.make(findViewById<View>(android.R.id.content),
            "Sign up Failed",
            Snackbar.LENGTH_LONG
        )
            .setAction("Try Again") { v -> Signup() }.show()
    }

}

