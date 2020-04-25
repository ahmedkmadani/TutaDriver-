package com.tutaap.tutadriver

import androidx.appcompat.app.AppCompatActivity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import com.airbnb.lottie.LottieAnimationView

class ViewDialog(internal var activity: AppCompatActivity) {

    internal lateinit var mLottieAnimationView: LottieAnimationView
    internal var mAnimFile = "world-locations.json"
    internal lateinit var dialog: Dialog

    fun showDialog() {

        dialog = Dialog(activity)


        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog.setCancelable(false)
        dialog.setContentView(R.layout.custom_dailog_layout)

        mLottieAnimationView = dialog.findViewById(R.id.lottie_animation_view)
        dialog.show()
    }


    fun hideDialog() {
        dialog.dismiss()
    }

}