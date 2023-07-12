package com.example.healthert.ui.notifications

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.healthert.AgregarInfoPersonaActivity
import com.example.healthert.LoginActivity
import com.example.healthert.databinding.FragmentNotificationsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private lateinit var salirCardView: CardView
    private lateinit var nombreUsuarioTextView: TextView
    private lateinit var imgView: ImageView
    private lateinit var ajustesUsuarioCardView: CardView

    private lateinit var nombrec: HashMap<String, String>
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
        salirCardView = binding.salirCardview
        ajustesUsuarioCardView = binding.ajustesUsuarioCardView

        //Boton de ajustes del usuario
        ajustesUsuarioCardView.setOnClickListener {
            val intent = Intent(activity,AgregarInfoPersonaActivity::class.java)
            intent.putExtra("estaModificando",true)
            startActivity(intent)
        }







        //Recuperar foto del usuario y guardar en cache
        var userRef = storageRef.child("images/" + FirebaseAuth.getInstance().uid.toString())
        Glide.with(this).load(userRef).into(imgView)

        //Recuperar nombre del usuario
        val docRef = usuarios.document(FirebaseAuth.getInstance().uid!!)
        docRef.get().addOnSuccessListener { document ->
            nombrec = document.data?.getValue("nombrec") as HashMap<String, String>
            nombreUsuarioTextView.text =
                nombrec["nombres"] + " " + nombrec["apellidoP"] + " " + nombrec["apellidoM"]
        }.addOnFailureListener {
            Toast.makeText(context, "No se pudo recuperar la informacion", Toast.LENGTH_LONG)
        }

        //Evento para salir de la cuenta
        salirCardView.setOnClickListener {
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