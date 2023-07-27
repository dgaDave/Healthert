package com.example.healthert

import android.content.Intent
import android.net.Uri
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import com.example.healthert.databinding.ActivityAgregarSaludAvanzadaBinding
import com.example.healthert.databinding.ActivityAgregarSaludBasicaBinding
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage


class AgregarSaludAvanzadaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAgregarSaludAvanzadaBinding
    private lateinit var seguroRadioButton: RadioButton
    private lateinit var alergiasRadioButton: RadioButton
    private lateinit var padecimientosRadioButton: RadioButton
    private lateinit var numeroSeguroTextInputLayout: TextInputLayout
    private lateinit var alergiasTextInputLayout: TextInputLayout
    private lateinit var padecimientosTextInputLayout: TextInputLayout
    private lateinit var numeroSeguroEditText: EditText
    private lateinit var alergiasEditText: EditText
    private lateinit var padecimientosEditText: EditText
    private val db = Firebase.firestore
    private var storageRef = Firebase.storage.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgregarSaludAvanzadaBinding.inflate(layoutInflater)
        seguroRadioButton = binding.seguroRadioButton
        alergiasRadioButton = binding.alergiasRadioButton
        padecimientosRadioButton = binding.padecimientosRadioButton

        numeroSeguroTextInputLayout = binding.numeroSeguroTextField
        alergiasTextInputLayout = binding.alergiasTextField
        padecimientosTextInputLayout = binding.padecimientosTextField

        numeroSeguroEditText = binding.numeroSeguroEditText
        alergiasEditText = binding.alergiasEditText
        padecimientosEditText = binding.padecimientosEditText

        seguroRadioButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                numeroSeguroTextInputLayout.visibility = View.VISIBLE
            } else {
                numeroSeguroTextInputLayout.visibility = View.GONE
            }
        }

        alergiasRadioButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                alergiasTextInputLayout.visibility = View.VISIBLE
            } else {
                alergiasTextInputLayout.visibility = View.GONE
            }
        }

        padecimientosRadioButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                padecimientosTextInputLayout.visibility = View.VISIBLE
            } else {
                padecimientosTextInputLayout.visibility = View.GONE
            }
        }

        setContentView(binding.root)
    }


}