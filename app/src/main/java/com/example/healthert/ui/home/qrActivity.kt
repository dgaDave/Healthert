package com.example.healthert.ui.home

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.example.healthert.R
import com.example.healthert.databinding.ActivityQrBinding
import net.glxn.qrgen.android.QRCode

class qrActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQrBinding
    private lateinit var qrImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrBinding.inflate(layoutInflater)
        qrImageView = binding.qrImageView

        var bitmap: Bitmap = if (isDarkModeEnabled(this)) {
            QRCode.from(intent.getStringExtra("link")).withSize(300, 300).bitmap()
        } else {
            QRCode.from(intent.getStringExtra("link")).withSize(300, 300).bitmap()
        }
        qrImageView.setImageBitmap(bitmap)




        setContentView(binding.root)
    }

    private fun isDarkModeEnabled(context: Context): Boolean {
        val currentNightMode =
            context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }
}