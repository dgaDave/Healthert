package com.example.healthert.ui.notifications

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.healthert.AgregarInfoPersonaActivity
import com.example.healthert.LoginActivity
import com.example.healthert.databinding.FragmentNotificationsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private lateinit var cerrarSesionButton: Button
    private lateinit var nombreUsuarioTextView: TextView
    private lateinit var imgView: ImageView
    private lateinit var ajustesUsuarioImageView: ImageView
    private lateinit var ajustesPacienteCardView: CardView
    private lateinit var informacionPlanCardView: CardView
    private lateinit var ayudaCardView: CardView
    private lateinit var acercaCardView: CardView
    private lateinit var nombrec: Map<String, String>
    private var storageRef = Firebase.storage.reference
    private val usuarios = Firebase.firestore.collection("users")
    private lateinit var sharedPreferences: SharedPreferences

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        sharedPreferences = requireActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE)
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        //Bindear
        nombreUsuarioTextView = binding.nombreUsuarioTextview
        imgView = binding.imageView
        cerrarSesionButton = binding.cerrarSesionButton
        ajustesUsuarioImageView = binding.ajustesUsuarioImageView
        ajustesPacienteCardView = binding.ajustesPacienteCardView
        informacionPlanCardView = binding.informacionPlanCardView
        ayudaCardView = binding.ayudaCardView
        acercaCardView = binding.acercaCardView

        //Recuperar foto del usuario y guardar en cache
        val userRef = storageRef.child("images/" + FirebaseAuth.getInstance().uid.toString())
        Glide.with(this).load(userRef).into(imgView)

        //Recuperar nombre del usuario
        val docRef = usuarios.document(FirebaseAuth.getInstance().uid!!)
        docRef.get().addOnSuccessListener { document ->
            nombrec = document.data?.getValue("nombrec") as HashMap<String, String>
            nombreUsuarioTextView.text = "Hola " + nombrec["nombres"] + "!"
        }.addOnFailureListener {
            Toast.makeText(context, "No se pudo recuperar la informacion", Toast.LENGTH_LONG).show()
        }

        //Boton de ajustes del usuario
        ajustesUsuarioImageView.setOnClickListener {
            val intent = Intent(activity, AgregarInfoPersonaActivity::class.java)
            intent.putExtra("estaModificando", true)
            startActivity(intent)
        }

        //Boton de ajustes del paciente
        ajustesPacienteCardView.setOnClickListener {
            startActivity(Intent(requireContext(), AjustesPacientesActivity::class.java))
        }

        informacionPlanCardView.setOnClickListener {
            //Activity informacion del plan
        }

        ayudaCardView.setOnClickListener {
            //Activity para ayudar
        }

        acercaCardView.setOnClickListener {
            //Activity para contactarnos
        }

        //Evento para salir de la cuenta
        cerrarSesionButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            sharedPreferences.edit().remove("uid").apply()
            startActivity(Intent(context, LoginActivity::class.java))
            activity?.finish()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}