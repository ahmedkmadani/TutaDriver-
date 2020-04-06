package com.tutaap.tutadriver

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class VechicalInformatinActivity : AppCompatActivity() {

    lateinit var token: String
    internal lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.vechical_info_layout)


        token = user.token.toString()
        Log.d("Token", token)

    }
}