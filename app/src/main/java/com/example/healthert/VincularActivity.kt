package com.example.healthert

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.healthert.databinding.ActivityVincularBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class VincularActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVincularBinding
    private var storageRef = Firebase.firestore
    private lateinit var documento: String
    private lateinit var codigo: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVincularBinding.inflate(layoutInflater)
        documento = intent.getStringExtra("documento").toString()
        codigo = documento.substring(0..2) + documento.substring((documento.length - 3) until documento.length)

        storageRef.collection("users").document(documento).update("codigo",codigo.uppercase())
        binding.codigoTextView.text = codigo.uppercase()

        val docRef = storageRef.collection("users").document(documento)
        docRef.addSnapshotListener { snapshot, _ ->
            if (snapshot?.data?.get("codigo")==null){
             startActivity(Intent(this,MainActivity2::class.java))
                finishAffinity()
            }
        }


        setContentView(binding.root)
    }
}