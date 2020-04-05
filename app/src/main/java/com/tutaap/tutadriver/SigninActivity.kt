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
import kotlinx.android.synthetic.main.activity_signin.*
import org.json.JSONObject


class SigninActivity: AppCompatActivity() {

//    private var callbackManager: CallbackManager? = null
//    val EMAIL = "email"
//    val PUBLIC_PROFILE = "public_profile"
//
//    val USER_PERMISSION = "user_friends"

    internal lateinit var viewDialog: ViewDialog
    internal lateinit var user: User


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        if (SharedPrefManager.getInstance(this).isLoggedIn) {
            finish()
            startActivity(Intent(this, MapsActivity::class.java))
            return
        }

        viewDialog = ViewDialog(this)

//        facebookLoginButton.setOnClickListener {
//            callbackManager = CallbackManager.Factory.create()
//            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList(EMAIL, PUBLIC_PROFILE, USER_PERMISSION))
//            LoginManager.getInstance().registerCallback(callbackManager,
//                object : FacebookCallback<LoginResult> {
//                    override fun onSuccess(loginResult: LoginResult) {
//                        val request = GraphRequest.newMeRequest(
//                            loginResult.accessToken
//                        ) { jsonObject, _ ->
//                            Log.d("Facebook JsonObject", jsonObject.toString())
//
//                            val email = jsonObject?.get("email")?.toString() ?: ""
//                            val name = jsonObject.get("name").toString()
//                            val id = jsonObject.get("id").toString()
//                            val profileObjectImage = jsonObject?.getJSONObject("picture")?.getJSONObject("data")?.get("url").toString()
//
//                            Toast.makeText(this@SigninActivity, "Welcome ${name} to Tuta App", Toast.LENGTH_LONG).show()
//                            Log.d("Facebook Name", name)
//                        }
//                        val parameters = Bundle()
//                        parameters.putString("fields", "id,name,email,picture.type(large)")
//                        request.parameters = parameters
//                        request.executeAsync()
//                        Log.d("SigninActivity", "Facebook token: " + loginResult.accessToken.token)
//                        onSiginSuccess()
//                    }
//
//                    override fun onCancel() {
//                        Log.d("SigninActivity", "Facebook onCancel.")
//
//                    }
//
//
//                    override fun onError(exception: FacebookException) {
//                        Log.d("SigninActivity", "Facebook onError.")
//                    }
//                })
//        }


        btn_login.setOnClickListener {
            Sigin()
        }

        link_signup.setOnClickListener {
            startActivity(Intent(this@SigninActivity, SignupActivity::class.java))
            finish()
        }
    }


    private fun Sigin() {
        if (!validate()) {
            return
        }

        viewDialog.showDialog()


        val Email = input_email!!.text.toString()
        val Password = input_password!!.text.toString()
        val isDriver = 0

        val jsonObject = JSONObject()

        try {

            jsonObject.put("email", Email)
            jsonObject.put("password", Password)
            jsonObject.put("is_driver", isDriver)

        } catch (e: Exception) {

        }


        val stringRequest = JsonObjectRequest(
            Request.Method.POST,URLs.URL_LOGIN,jsonObject,
            Response.Listener { response ->

                try {

                    val JsonRes = response.getJSONObject("data")
                    val Token = JsonRes.getString("token")

                    Log.d("Token", "$Token")
                    Log.d("res", "$JsonRes")

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




    fun validate(): Boolean {
        var valid = true

        val email = input_email!!.text.toString()
        val password = input_password!!.text.toString()

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            input_email!!.error = "enter a valid email address"
            valid = false
        } else {
            input_email!!.error = null
        }

        if (password.isEmpty() || password.length < 2 || password.length > 10) {
            input_password!!.error = "between 5 and 10 alphanumeric characters"
            valid = false
        } else {
            input_password!!.error = null
        }

        return valid
    }


    private fun onSiginSuccess() {
        viewDialog.hideDialog()
        Snackbar.make(
            findViewById(android.R.id.content),
            "Sign in Successfully",
            Snackbar.LENGTH_LONG
        )
            .show()
        startActivity(Intent(this, MapsActivity::class.java))
        finish()
    }

    private fun onSiginFailed() {
        viewDialog.hideDialog()
        Snackbar.make(findViewById<View>(android.R.id.content),
            "Sign in Failed",
            Snackbar.LENGTH_LONG
        )
            .setAction("Try Again") { v -> Sigin() }.show()
    }

}