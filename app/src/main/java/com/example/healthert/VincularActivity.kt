package com.example.healthert

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.healthert.databinding.ActivityVincularBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class VincularActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVincularBinding
    private var storageRef = Firebase.firestore
    private lateinit var codigo: String
    private lateinit var documento: String
    private lateinit var agregarMasTardeButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVincularBinding.inflate(layoutInflater)
        codigo = intent.getStringExtra("codigo").toString()
        documento = intent.getStringExtra("documento").toString()
        storageRef.collection("users").document(documento).update("codigo", codigo.uppercase())
        binding.codigoTextView.text = codigo.uppercase()
        agregarMasTardeButton = binding.agregarMasTardeButton

        val docRef = storageRef.collection("users").document(documento)
        docRef.addSnapshotListener { snapshot, _ ->
            if (snapshot?.data?.get("codigo") == null) {
                startActivity(Intent(this, MainActivity2::class.java))
                finishAffinity()
            }
        }

        agregarMasTardeButton.setOnClickListener {
            startActivity(Intent(this, MainActivity2::class.java))
            finishAffinity()
        }

        setContentView(binding.root)
    }
}