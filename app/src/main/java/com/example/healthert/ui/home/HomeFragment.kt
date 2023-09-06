package com.example.healthert.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.healthert.PacientesSaludAdapter
import com.example.healthert.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var pacientes = mutableListOf<com.example.healthert.classes.Paciente>()
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
//        usuarios.whereEqualTo("usuarioCuidador", uid).get(Source.CACHE).addOnSuccessListener {
//            for (document in it) {
//                val paciente = document.toObject(com.example.healthert.classes.Paciente::class.java)
//                pacientes.add(paciente)
//            }
//
//            adapter = PacientesSaludAdapter(
//                requireContext(),
//                FirebaseAuth.getInstance().uid.toString(),
//                pacientes, this
//            )
//            if (adapter.itemCount == 0) {
//                binding.logoBack.visibility = View.VISIBLE
//                viewPager.visibility = View.GONE
//            }
//            viewPager.adapter = adapter
//        }
        usuarios.whereEqualTo("usuarioCuidador", uid).get(Source.SERVER).addOnSuccessListener {
            pacientes.clear()
            for (document in it) {
                val paciente = document.toObject(com.example.healthert.classes.Paciente::class.java)
                pacientes.add(paciente)
            }

            adapter = PacientesSaludAdapter(
                requireContext(),
                FirebaseAuth.getInstance().uid.toString(),
                pacientes, this
            )
            if (adapter.itemCount == 0) {
                binding.logoBack.visibility = View.VISIBLE
                viewPager.visibility = View.GONE
            }
            viewPager.adapter = adapter
        }




        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}