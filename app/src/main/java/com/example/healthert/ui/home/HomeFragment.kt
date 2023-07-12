package com.example.healthert.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.healthert.PacientesAdapter
import com.example.healthert.PacientesSaludAdapter
import com.example.healthert.databinding.FragmentHomeBinding
import com.example.healthert.ui.dashboard.Paciente
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var pacientes = mutableListOf<Paciente>()
    private val usuarios = Firebase.firestore.collection("users")
    private lateinit var adapter: PacientesSaludAdapter
    private lateinit var viewPager: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        viewPager = binding.viewPagerPacientesSalud

        val uid = FirebaseAuth.getInstance().uid.toString()
        usuarios.whereEqualTo("usuarioCuidador",uid).get().addOnSuccessListener {
            for (document in it){
                val paciente = document.toObject(Paciente::class.java)
                pacientes.add(paciente)
            }
            adapter = PacientesSaludAdapter(
                requireContext(),
                FirebaseAuth.getInstance().uid.toString(),
                pacientes, this,requireActivity()
            )
            viewPager.adapter = adapter
        }



        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
data class Paciente(
    val nombrec: HashMap<String, String> = hashMapOf(),
    val apellidoP: String = "",
    val apellidoM: String = "",
    val usuarioCuidador: String = "",
    val edad: Int = 0,
    val altura: Int = 0,
    val peso: Int = 0,
    val sexo: String = "",
    val alergias: String = "",
    val padecimientos: String = "",
    val curp: String = ""
)