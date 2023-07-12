package com.example.healthert

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.healthert.databinding.ActivityAgregarInfoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class AgregarInfoPersonaActivity : AppCompatActivity() {
    private lateinit var nombre: EditText
    private lateinit var apellidoP: EditText
    private lateinit var apellidoM: EditText
    private lateinit var seguirB: Button
    private lateinit var imageView: ImageView
    private lateinit var uriString: String
    private lateinit var nombrec: HashMap<String, String>
    private val usuarios = Firebase.firestore.collection("users")
    private var storageRef = Firebase.storage.reference
    private var estaModificando:Boolean = false
    private lateinit var binding: ActivityAgregarInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgregarInfoBinding.inflate(layoutInflater)

        nombre = binding.nombreEditText
        apellidoP = binding.apellidoPText
        apellidoM = binding.apellidoMText
        seguirB = binding.seguirButton
        imageView = binding.imageView

        //Recuperar valor de estar modificando
        estaModificando = intent.getBooleanExtra("estaModificando",false)
        if (estaModificando){
            val userRef = storageRef.child("images/" + FirebaseAuth.getInstance().uid.toString())
            Glide.with(this).load(userRef).into(imageView)

            val docRef = usuarios.document(FirebaseAuth.getInstance().uid!!)
            docRef.get(Source.CACHE).addOnSuccessListener { document ->
                nombrec = document.data?.getValue("nombrec") as HashMap<String, String>
                nombre.setText(nombrec["nombres"])
                apellidoP.setText(nombrec["apellidoP"])
                apellidoM.setText(nombrec["apellidoM"])
            }.addOnFailureListener {
                Toast.makeText(this, "No se pudo recuperar la informacion", Toast.LENGTH_LONG).show()
            }
        }

        //pickMedia codigo
        val pickMedia =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                if (uri != null) {
                    imageView.setImageURI(uri)
                    //imageView.setImageBitmap(uriToBitmap(uri))
                    uriString = uri.toString()
                } else {
                    Toast.makeText(
                        this,
                        "Error",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        //Evento de escoger imagen
        imageView.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }


        //Seguir con el proceso y se guarda la informacion para pasarla al siguiente
        seguirB.setOnClickListener {
            if (nombre.text.toString().isNotEmpty() && apellidoP.text.toString()
                    .isNotEmpty() && apellidoM.text.toString()
                    .isNotEmpty() && uriString.isNotEmpty()
            ) {
                //Preguntar si estamos registrando un paciente
                val esPaciente = intent.getBooleanExtra("esPaciente", false)
                var intent = Intent(this, AgregarTelActivity::class.java)
                if (esPaciente) {
                    intent = Intent(this, AgregarSaludActivity::class.java)
                }
                intent.putExtra("email", getIntent().getStringExtra("email"))
                intent.putExtra("password", getIntent().getStringExtra("password"))
                intent.putExtra("nombre", formatear(nombre.text.toString()))
                intent.putExtra("apellidoP", formatear(apellidoP.text.toString()))
                intent.putExtra("apellidoM", formatear(apellidoM.text.toString()))
                intent.putExtra("uri", uriString)
                if (estaModificando){
                 intent.putExtra("estaModificando",true)
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "Complete los campos y/o agregue una foto", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        setContentView(binding.root)
    }


    //Funcion para eliminar dobles espacios y espacios al final
    private fun formatear(texto: String): String {
        // Eliminar espacios duplicados
        val textoSinDoblesEspacios = texto.replace("\\s+".toRegex(), " ")
        // Eliminar espacios al final
        // Devolver el texto modificado
        return textoSinDoblesEspacios.replace("\\s+$".toRegex(), "")
    }
}