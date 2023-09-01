package com.example.healthert.ui.notifications

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthert.classes.Paciente
import com.example.healthert.databinding.ActivityAjustesPacientesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class AjustesPacientesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAjustesPacientesBinding
    private lateinit var recyclerView: RecyclerView
    private val db = Firebase.firestore.collection("users")
    private val uid = FirebaseAuth.getInstance().uid
    private var pacientes = mutableListOf<Paciente>()
    private lateinit var ajustesPacientesAdapter: AjustesPacientesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAjustesPacientesBinding.inflate(layoutInflater)
        recyclerView = binding.recyclerView
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        db.whereEqualTo("usuarioCuidador", uid).get(Source.CACHE).addOnSuccessListener {
            for (document in it) {
                val paciente = document.toObject(Paciente::class.java)
                pacientes.add(paciente)
            }
            ajustesPacientesAdapter = AjustesPacientesAdapter(this, uid.toString(), pacientes,this)

            if (ajustesPacientesAdapter.itemCount == 0) {
                binding.logoBack.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
                Toast.makeText(this, "No tienes pacientes registrados", Toast.LENGTH_LONG).show()
            } else {
                recyclerView.adapter = ajustesPacientesAdapter
            }
        }
        db.whereEqualTo("usuarioCuidador", uid).get(Source.SERVER).addOnSuccessListener {
            pacientes.clear()
            for (document in it) {
                val paciente = document.toObject(Paciente::class.java)
                pacientes.add(paciente)
            }
            ajustesPacientesAdapter = AjustesPacientesAdapter(this, uid.toString(), pacientes,this)

            if (ajustesPacientesAdapter.itemCount == 0) {
                binding.logoBack.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
                Toast.makeText(this, "No tienes pacientes registrados", Toast.LENGTH_LONG).show()
            } else {
                recyclerView.adapter = ajustesPacientesAdapter
            }
        }
        setContentView(binding.root)
    }
}