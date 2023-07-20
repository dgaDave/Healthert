package com.example.healthert

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.healthert.databinding.ActivityAgregarSaludBasicaBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage


class AgregarSaludBasicaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAgregarSaludBasicaBinding
    private lateinit var continuarButton: Button
    private lateinit var curpEditText: EditText
    private lateinit var fechaNacimientoEditText: EditText
    private lateinit var alturaEditText: EditText
    private lateinit var pesoEditText: EditText
    private lateinit var sexoAutoCompleteTextView: AutoCompleteTextView
    private lateinit var sangreAutoCompleteTextView: AutoCompleteTextView
    private val db = Firebase.firestore
    private var storageRef = Firebase.storage.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgregarSaludBasicaBinding.inflate(layoutInflater)
        continuarButton = binding.continuarButton
        curpEditText = binding.curpEditText
        fechaNacimientoEditText = binding.fechaNacimientoEditText
        alturaEditText = binding.alturaEditText
        pesoEditText = binding.pesoEditText
        sexoAutoCompleteTextView = binding.sexosAutoCompleteTextView
        sangreAutoCompleteTextView = binding.sangreAutoCompleteTextView
        iniciarSelections()

        continuarButton.setOnClickListener {
            if (validarCampos()) iniciarActivityNueva()
        }
        setContentView(binding.root)
    }

    //Inicializa selections
    private fun iniciarSelections() {
        val sexos = resources.getStringArray(R.array.sexosStrings)
        var adapter = ArrayAdapter(this, R.layout.list_item, sexos)
        with(binding.sexosAutoCompleteTextView) {
            setAdapter(adapter)
        }
        val gruposSanguineos = resources.getStringArray(R.array.gruposSanguineosStrings)
        adapter = ArrayAdapter(this, R.layout.list_item, gruposSanguineos)
        with(binding.sangreAutoCompleteTextView) {
            setAdapter(adapter)
        }
    }


    //Valida que los campos no estén vacios
    private fun validarCampos(): Boolean {
        var i = 0
        if (curpEditText.text.toString().isNotEmpty() && curpEditText.text.toString().length==18){
            i++
        }else{

        }
        if (fechaNacimientoEditText.text.toString().isNotEmpty()){
            i++
        }else{

        }
        if (alturaEditText.text.toString().isNotEmpty()){
            i++
        }else{

        }
        if (pesoEditText.text.toString().isNotEmpty()){
            i++
        }else{

        }
        if (sexoAutoCompleteTextView.text.toString().isNotEmpty()){
            i++
        }else{

        }
        if (sangreAutoCompleteTextView.text.toString().isNotEmpty()){
            i++
        }else{

        }
        return i == 6
    }

    private fun iniciarActivityNueva(){
        val intent = Intent(this,AgregarSaludAvanzadaActivity::class.java)
        intent.putExtra("curp",curpEditText.text.toString())
        intent.putExtra("fechaNacimiento",fechaNacimientoEditText.text.toString())
        intent.putExtra("altura",alturaEditText.text.toString())
        intent.putExtra("peso",pesoEditText.text.toString())
        intent.putExtra("sexo",sexoAutoCompleteTextView.text.toString())
        intent.putExtra("grupoSanguineo",sangreAutoCompleteTextView.text.toString())
        startActivity(intent)
    }

    //Se obtenienen los datos de los campos y se mandan a la función del registro
    //Se creó la función para ahorrar espacio en el método principal
    /*private fun obtenerDatosRegistro() {
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

    }*/
}