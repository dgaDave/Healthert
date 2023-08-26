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
import java.text.SimpleDateFormat


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
    private lateinit var registrarButton: Button
    private val db = Firebase.firestore
    private var storageRef = Firebase.storage.reference
    private val uid = FirebaseAuth.getInstance().uid

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

        registrarButton = binding.registrarButton

        seguroRadioButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                numeroSeguroTextInputLayout.visibility = View.VISIBLE
            } else {
                numeroSeguroTextInputLayout.visibility = View.GONE
                numeroSeguroEditText.text = null
            }
        }

        alergiasRadioButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                alergiasTextInputLayout.visibility = View.VISIBLE
            } else {
                alergiasTextInputLayout.visibility = View.GONE
                numeroSeguroEditText.text = null
            }
        }

        padecimientosRadioButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                padecimientosTextInputLayout.visibility = View.VISIBLE
            } else {
                padecimientosTextInputLayout.visibility = View.GONE
                numeroSeguroEditText.text = null
            }
        }

        registrarButton.setOnClickListener {
            registrarUsuario(
                intent.getStringExtra("nombre").toString(),
                intent.getStringExtra("apellidoP").toString(),
                intent.getStringExtra("apellidoM").toString(),
                intent.getStringExtra("uri").toString(),
                intent.getStringExtra("sexo").toString(),
                intent.getStringExtra("grupoSanguineo").toString(),
                intent.getStringExtra("curp").toString(),
                convertirFechaALong(intent.getStringExtra("fechaNacimiento").toString()),
                intent.getStringExtra("altura").toString().toInt(),
                intent.getStringExtra("peso").toString().toInt(),
                numeroSeguroEditText.text.toString(),
                alergiasEditText.text.toString(),
                padecimientosEditText.text.toString()
            )
        }

        setContentView(binding.root)
    }

    private fun convertirFechaALong(fecha: String): Long {
        val format = SimpleDateFormat("dd/MM/yyyy")
        return format.parse(fecha).time
    }

    private fun registrarUsuario(
        nombre: String,
        apellidoP: String,
        apellidoM: String,
        uri: String,
        sexo: String,
        grupoSanguineo: String,
        curp: String,
        fechaNacimiento: Long,
        altura: Int,
        peso: Int,
        seguro: String,
        alergias: String,
        padecimientos: String
    ) {
        val nombrec = mapOf(
            "nombres" to nombre,
            "apellidoP" to apellidoP,
            "apellidoM" to apellidoM
        )

        val paciente = mutableMapOf(
            "nombrec" to nombrec,
            "sexo" to sexo,
            "grupoSanguineo" to grupoSanguineo,
            "curp" to curp,
            "fechaNacimiento" to fechaNacimiento,
            "altura" to altura,
            "peso" to peso,
            "usuarioCuidador" to uid.toString()
        )

        if(!seguro.isNullOrEmpty()) paciente["seguro"] = seguro
        if(!alergias.isNullOrEmpty()) paciente["alergias"] = alergias
        if(!padecimientos.isNullOrEmpty()) paciente["padecimientos"] = padecimientos

        //Se sube la informacion
        db.collection("users").document(uid.toString() + curp).set(paciente).addOnSuccessListener {

            //Se sube la foto
            val file = Uri.parse(uri)
            val imgsRef =
                storageRef.child("images/" + FirebaseAuth.getInstance().uid.toString() + curp)
            val uploadTask = imgsRef.putFile(file)

            uploadTask
                .addOnSuccessListener {
                    //Mandamos a la actividad de vinculacion
                    val intent = Intent(this, VincularActivity::class.java)
                    intent.putExtra("documento", FirebaseAuth.getInstance().uid.toString() + curp)
                    startActivity(intent)
                    finishAffinity()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "No se pudo registrar en el sistema", Toast.LENGTH_SHORT)
                        .show()
                }

        }.addOnFailureListener {
            Toast.makeText(this, "No se pudo registrar los datos del paciente", Toast.LENGTH_SHORT)
                .show()
        }

    }


}