package com.example.healthert

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.healthert.databinding.ActivityAgregarTelBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage


class AgregarTelActivity : AppCompatActivity() {

    private lateinit var registrarBoton: Button
    private lateinit var numeroText: EditText
    private val db = Firebase.firestore
    private var storageRef = Firebase.storage.reference
    private lateinit var binding: ActivityAgregarTelBinding
    private lateinit var sharedPreferences:SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgregarTelBinding.inflate(layoutInflater)
        sharedPreferences = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        registrarBoton = binding.registrarBoton
        numeroText = binding.telEditText

        //Si esta modificando se pone el texto en el campo
        val estaModificando = intent.getBooleanExtra("estaModificando", false)
        if (estaModificando) {
            db.collection("users").document(FirebaseAuth.getInstance().uid.toString()).get()
                .addOnSuccessListener { document ->
                    numeroText.setText(document.get("telefono").toString())
                }.addOnFailureListener {
                    Toast.makeText(this, "No se pudo recuperar la informacion", Toast.LENGTH_LONG).show()
                }
        }

        //evento de registrar
        registrarBoton.setOnClickListener {
            if (numeroText.text.toString()
                    .isNotEmpty() && numeroText.text.toString().length == 10
            ) {
                if (estaModificando){
                    modificarUsuario(intent.getStringExtra("nombre")!!,
                        intent.getStringExtra("apellidoP")!!,
                        intent.getStringExtra("apellidoM")!!,
                        intent.getStringExtra("uri")!!,
                        numeroText.text.toString())
                }else{
                    setContentView(R.layout.loading_layout)
                    registrarUsuario(
                        intent.getStringExtra("email")!!,
                        intent.getStringExtra("password")!!,
                        intent.getStringExtra("nombre")!!,
                        intent.getStringExtra("apellidoP")!!,
                        intent.getStringExtra("apellidoM")!!,
                        intent.getStringExtra("uri")!!,
                        numeroText.text.toString()
                    )
                }

            } else {
                Toast.makeText(this, "Llena bien el campo de número", Toast.LENGTH_LONG).show()

            }
        }
        setContentView(binding.root)
    }


    private fun registrarUsuario(
        email: String,
        password: String,
        nombre: String,
        apellidoP: String,
        apellidoM: String,
        uri: String,
        telefono: String
    ) {
        //Registra en firebase auth
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val nombrec = hashMapOf(
                        "nombres" to nombre,
                        "apellidoP" to apellidoP,
                        "apellidoM" to apellidoM
                    )

                    val personaCuidadora = hashMapOf(
                        "nombrec" to nombrec,
                        "email" to email,
                        "telefono" to telefono
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
                                sharedPreferences.edit().putString("uid",FirebaseAuth.getInstance().uid.toString()).apply()
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
        db.collection("users").document(FirebaseAuth.getInstance().uid.toString()).update(personaCuidadora).addOnSuccessListener {
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