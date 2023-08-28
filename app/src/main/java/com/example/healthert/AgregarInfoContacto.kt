package com.example.healthert

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.healthert.databinding.ActivityAgregarInfoContactoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext


class AgregarInfoContacto : AppCompatActivity() {

    private lateinit var registrarBoton: Button
    private lateinit var telEditText: EditText
    private lateinit var calleEditText: EditText
    private lateinit var codigoPostalEditText: EditText
    private lateinit var coloniaEditText: EditText
    private lateinit var municipioEditText: EditText
    private lateinit var estadoEditText: EditText
    private val db = Firebase.firestore
    private var storageRef = Firebase.storage.reference
    private lateinit var binding: ActivityAgregarInfoContactoBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgregarInfoContactoBinding.inflate(layoutInflater)
        sharedPreferences = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        registrarBoton = binding.registrarBoton
        telEditText = binding.telEditText
        calleEditText = binding.calleEditText
        codigoPostalEditText = binding.codigoPostalEditText
        coloniaEditText = binding.coloniaEditText
        municipioEditText = binding.municipioEditText
        estadoEditText = binding.estadoEditText

        //Si esta modificando se pone el texto en el campo
        val estaModificando = intent.getBooleanExtra("estaModificando", false)
        if (estaModificando) {
            db.collection("users").document(FirebaseAuth.getInstance().uid.toString()).get()
                .addOnSuccessListener { document ->
                    val domicilio = document.get("domicilio") as Map<String, String>
                    telEditText.setText(document.get("telefono").toString())
                    calleEditText.setText(domicilio["calle"])
                    codigoPostalEditText.setText(domicilio["codigoPostal"])
                    coloniaEditText.setText(domicilio["colonia"])
                    municipioEditText.setText(domicilio["municipio"])
                    estadoEditText.setText(domicilio["estado"])
                }.addOnFailureListener {
                    Toast.makeText(this, "No se pudo recuperar la informacion", Toast.LENGTH_LONG)
                        .show()
                }
        }

        //evento de registrar
        registrarBoton.setOnClickListener {
            if (validarDatos()) {
                if (estaModificando) {
                    modificarUsuario(
                        intent.getStringExtra("nombre")!!,
                        intent.getStringExtra("apellidoP")!!,
                        intent.getStringExtra("apellidoM")!!,
                        intent.getStringExtra("uri")!!,
                        telEditText.text.toString()
                    )
                } else {
                    setContentView(R.layout.loading_layout)
                    registrarUsuario(
                        intent.getStringExtra("email")!!,
                        intent.getStringExtra("password")!!,
                        mapOf(
                            "nombres" to intent.getStringExtra("nombre")!!,
                            "apellidoP" to intent.getStringExtra("apellidoP")!!,
                            "apellidoM" to intent.getStringExtra("apellidoM")!!
                        ),
                        intent.getStringExtra("uri")!!,
                        telEditText.text.toString(),
                        mapOf(
                            "calle" to calleEditText.text.toString(),
                            "codigoPostal" to codigoPostalEditText.text.toString(),
                            "colonia" to coloniaEditText.text.toString(),
                            "municipio" to municipioEditText.text.toString(),
                            "estado" to estadoEditText.text.toString()
                        )
                    )
                }

            } else {
                Toast.makeText(this, "Llena bien el campo de número", Toast.LENGTH_LONG).show()

            }
        }
        setContentView(binding.root)
    }

    private fun validarDatos(): Boolean {
        var i = 0
        //Validar telefono
        if (telEditText.text.toString().isNotEmpty() && telEditText.text.length == 10) {
            i++
        } else {

        }
        //Validar calle
        if (calleEditText.text.isNotEmpty()) {
            i++
        } else {

        }
        //Validar codigo postal
        if (codigoPostalEditText.text.isNotEmpty()) {
            i++
        } else {

        }
        //Validar colonia
        if (coloniaEditText.text.isNotEmpty()) {
            i++
        } else {

        }
        //Validar municipio
        if (municipioEditText.text.isNotEmpty()) {
            i++
        } else {

        }
        //Validar estado
        if (estadoEditText.text.isNotEmpty()) {
            i++
        } else {

        }
        return i == 6
    }

    private fun registrarUsuario(
        email: String,
        password: String,
        nombrec: Map<String, String>,
        uri: String,
        telefono: String,
        domicilio: Map<String, String>
    ) {
        //Registra en firebase auth
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val personaCuidadora = mapOf(
                        "nombrec" to nombrec,
                        "email" to email,
                        "telefono" to telefono,
                        "domicilio" to domicilio
                    )

                    //Registra datos en cloud
                    db.collection("users").document(FirebaseAuth.getInstance().uid.toString())
                        .set(personaCuidadora).addOnSuccessListener {
                            val file = Uri.parse(uri)
                            val imgsRef =
                                storageRef.child("images/" + FirebaseAuth.getInstance().uid.toString())
                            val uploadTask = imgsRef.putFile(file)

                            uploadTask.addOnFailureListener {
                                Toast.makeText(this, "Valio verga", Toast.LENGTH_LONG).show()
                            }.addOnSuccessListener {
                                sharedPreferences.edit()
                                    .putString("uid", FirebaseAuth.getInstance().uid.toString())
                                    .apply()
                                startActivity(Intent(this, MainActivity2::class.java))
                                finishAffinity()
                            }
                        }.addOnFailureListener {
                            Toast.makeText(this, "Valio verga", Toast.LENGTH_LONG).show()
                        }

                } else {
                    Toast.makeText(this, "Error en el registro", Toast.LENGTH_LONG).show()
                }

            }
    }

    private fun modificarUsuario(
        nombre: String,
        apellidoP: String,
        apellidoM: String,
        uri: String,
        telefono: String
    ) {
        val nombrec = hashMapOf(
            "nombres" to nombre,
            "apellidoP" to apellidoP,
            "apellidoM" to apellidoM
        )

        val personaCuidadora = mapOf(
            "nombrec" to nombrec,
            "telefono" to telefono
        )

        //Registra datos en cloud
        db.collection("users").document(FirebaseAuth.getInstance().uid.toString())
            .update(personaCuidadora).addOnSuccessListener {

                /*Glide.get(this).clearMemory()
                mainCache(this)*/
                if (uri == "noModifica") {
                    startActivity(Intent(this, MainActivity2::class.java))
                    finishAffinity()
                } else {
                    val file = Uri.parse(uri)
                    val imgsRef =
                        storageRef.child("images/" + FirebaseAuth.getInstance().uid.toString())
                    val uploadTask = imgsRef.putFile(file)
                    uploadTask.addOnFailureListener {
                        Toast.makeText(this, "Valio verga", Toast.LENGTH_LONG).show()
                    }.addOnSuccessListener {
                        startActivity(Intent(this, MainActivity2::class.java))
                        finishAffinity()
                    }
                }
            }
    }

    fun mainCache(context: Context) = runBlocking {
        launch {
            borrarCache(context)
            Log.e("Cache", "Se ha borrado el cache")
        }
    }

    private suspend fun borrarCache(context: Context) {
        withContext(Dispatchers.IO) {
            Glide.get(context).clearDiskCache()
        }
    }


}