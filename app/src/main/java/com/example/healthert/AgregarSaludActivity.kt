package com.example.healthert

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import com.example.healthert.databinding.ActivityAgregarSaludBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage


class AgregarSaludActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAgregarSaludBinding
    private lateinit var registrarButton: Button
    private val db = Firebase.firestore
    private var storageRef = Firebase.storage.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        iniciarElementosVisuales()
        registrarButton.setOnClickListener {
            if (validarCampos()) obtenerDatosRegistro()
        }
        setContentView(binding.root)
    }

    //Inicializa variables de la interfaz
    private fun iniciarElementosVisuales() {
        binding = ActivityAgregarSaludBinding.inflate(layoutInflater)
        registrarButton = binding.registrarBoton
        val sexos = resources.getStringArray(R.array.sexosStrings)
        val adapter = ArrayAdapter(this, R.layout.list_item, sexos)
        with(binding.autoCompleteTextView) {
            setAdapter(adapter)
        }
    }


    //Valida que los campos no estén vacios
    private fun validarCampos(): Boolean {
        var i = 0
        if (binding.edadEditText.text.toString().isEmpty()) {
            binding.edadTextField.error = "El campo no puede estar vacio"
        } else {
            binding.edadTextField.error = null
            i++
        }
        if (binding.alturaEditText.text.toString().isEmpty()) {
            binding.alturaTextField.error = "El campo no puede estar vacio"
        } else {
            binding.alturaTextField.error = null
            i++
        }
        if (binding.pesoEditText.text.toString().isEmpty()) {
            binding.pesoTextField.error = "El campo no puede estar vacio"
        } else {
            binding.pesoTextField.error = null
            i++
        }
        if (binding.autoCompleteTextView.text.toString().isEmpty()) {
            binding.sexoTextField.error = "El campo no puede estar vacio"
        } else {
            binding.sexoTextField.error = null
            i++
        }
        if (binding.alergiasEditText.text.toString().isEmpty()) {
            binding.alergiasTextField.error = "El campo no puede estar vacio"
        } else {
            binding.alergiasTextField.error = null
            i++
        }
        if (binding.padecimientosEditText.text.toString().isEmpty()) {
            binding.padecimientosTextField.error = "El campo no puede estar vacio"
        } else {
            binding.padecimientosTextField.error = null
            i++
        }
        return if (i == 6) {
            return true
        } else {
            return false
        }
    }

    //Se obtenienen los datos de los campos y se mandan a la función del registro
    //Se creó la función para ahorrar espacio en el método principal
    private fun obtenerDatosRegistro() {
        muestraPantallaCarga()
        registrarPaciente(
            intent.getStringExtra("nombre").toString(),
            intent.getStringExtra("apellidoP").toString(),
            intent.getStringExtra("apellidoM").toString(),
            intent.getStringExtra("uri").toString(),
            FirebaseAuth.getInstance().uid.toString(),
            binding.edadEditText.text.toString().toInt(),
            binding.alturaEditText.text.toString().toInt(),
            binding.pesoEditText.text.toString().toInt(),
            binding.autoCompleteTextView.text.toString(),
            binding.alergiasEditText.text.toString(),
            binding.padecimientosEditText.text.toString(),
            binding.curpEditText.text.toString()
        )
    }

    private fun muestraPantallaCarga() {
        setContentView(R.layout.loading_layout)
    }

    //Función para registrar al paciente, esta manda a llamar la función que inserta los datos en la DB
    private fun registrarPaciente(
        nombre: String,
        apellidoP: String,
        apellidoM: String,
        uri: String,
        usuarioCuidador: String,
        edad: Int,
        altura: Int,
        peso: Int,
        sexo: String,
        alergias: String,
        padecimientos: String,
        curp: String
    ) {

        val nombrec = hashMapOf(
            "nombres" to nombre,
            "apellidoP" to apellidoP,
            "apellidoM" to apellidoM
        )
        val paciente = hashMapOf(
            "nombrec" to nombrec,
            "usuarioCuidador" to usuarioCuidador,
            "edad" to edad,
            "altura" to altura,
            "peso" to peso,
            "sexo" to sexo,
            "alergias" to alergias,
            "padecimientos" to padecimientos,
            "curp" to curp
        )
        insertaDB(paciente, curp, uri)

    }

    //Función para registrar los datos en la base de datos remota
    private fun insertaDB(paciente: Map<String, Any>, curp: String, uri: String) {
        db.collection("users")
            .document(FirebaseAuth.getInstance().uid.toString() + curp)
            .set(paciente).addOnSuccessListener {
                val file = Uri.parse(uri)
                val imgsRef =
                    storageRef.child("images/" + FirebaseAuth.getInstance().uid.toString() + curp)
                val uploadTask = imgsRef.putFile(file)

                uploadTask.addOnFailureListener {
                    Toast.makeText(this, "No se pudo registrar en el sistema", Toast.LENGTH_LONG)
                        .show()
                }.addOnSuccessListener {
                    val intent = Intent(this, VincularActivity::class.java)
                    intent.putExtra("documento", FirebaseAuth.getInstance().uid.toString() + curp)
                    startActivity(intent)
                    finishAffinity()
                }

            }.addOnFailureListener {
                Toast.makeText(this, "No se pudo registrar en el sistema", Toast.LENGTH_LONG).show()

            }

    }
}