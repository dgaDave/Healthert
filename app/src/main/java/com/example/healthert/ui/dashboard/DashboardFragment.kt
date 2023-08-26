package com.example.healthert.ui.dashboard

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ConcatAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.healthert.AgregarAdapter
import com.example.healthert.PacientesAdapter
import com.example.healthert.R
import com.example.healthert.databinding.FragmentDashboardBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewPager2: ViewPager2
    private lateinit var adapter1: PacientesAdapter
    private lateinit var adapter2: AgregarAdapter
    private var pacientes = mutableListOf<com.example.healthert.classes.Paciente>()
    private var idsPacientes: Array<String> = arrayOf()
    private val usuarios = Firebase.firestore.collection("users")
    private val markerList: MutableList<Marker> = mutableListOf()
    private var estaSiendoVisto: MutableMap<String, Boolean> = mutableMapOf()
    private var animado: Boolean = false

    //Referencia de base de datos para la medicion en tiempo real
    private var db = Firebase.database.reference.child("medicionTr")

    private val callback = OnMapReadyCallback { googleMap ->

        //Valor de nuestra uid
        val uid = FirebaseAuth.getInstance().uid.toString()

        //recuperamos a los usuarios, los metemos en un arreglo de clase pacientes y tambien agarramos sus ids
        usuarios.whereEqualTo("usuarioCuidador", uid).get()
            .addOnSuccessListener { result ->
                for (document in result) {

                    val paciente = document.toObject(com.example.healthert.classes.Paciente::class.java)
                    pacientes.add(paciente)
                    idsPacientes += uid + paciente.curp
                }
                adapter1 = PacientesAdapter(
                    requireContext(),
                    FirebaseAuth.getInstance().uid.toString(),
                    pacientes, this
                )
                adapter2 = AgregarAdapter(requireContext())
                val adapter = ConcatAdapter(adapter1, adapter2)
                viewPager2.adapter = adapter

                for (id in idsPacientes) {
                    estaSiendoVisto[id] = false

                    db.child(id).child("coordenadas")
                        .addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                //ruta donde esta leyendo el dispositivo
                                val dispositivo =
                                    snapshot.ref.parent.toString().substringAfter("medicionTr/")
                                //Verificamos que los valores existan
                                if (snapshot.child("latitud").value != null && snapshot.child("longitud")
                                        .value != null
                                ) {
                                    val posicion = LatLng(
                                        snapshot.child("latitud").value as Double,
                                        snapshot.child("longitud").value as Double
                                    )
                                    val colorMarcador : Float

                                    //Configuracion del marcador
                                    val isDarkTheme = isDarkTheme(requireContext())
                                    colorMarcador = if (isDarkTheme) {
                                        BitmapDescriptorFactory.HUE_MAGENTA
                                    } else {
                                        BitmapDescriptorFactory.HUE_GREEN
                                    }
                                    //Inicializamos el marcador, si existe uno ya anterior, lo eliminamos
                                    val marcador =
                                        MarkerOptions().position(posicion).draggable(false).icon(
                                            BitmapDescriptorFactory.defaultMarker(colorMarcador)
                                        )

                                    for (marker in markerList) {
                                        if (marker.tag == dispositivo) {
                                            marker.remove() // Elimina el marcador del mapa
                                            markerList.remove(marker) // Elimina el marcador de la lista
                                            break
                                        }
                                    }
                                    val marker = googleMap.addMarker(marcador)
                                    marker!!.tag = dispositivo
                                    //inicializamos si se esta observandolo como false y animamos y observamossolo la primera vez
                                    if (!animado) {
                                        val cameraPosition =
                                            CameraPosition.builder().target(marker.position)
                                                .zoom(17f).build()
                                        googleMap.animateCamera(
                                            CameraUpdateFactory.newCameraPosition(
                                                cameraPosition
                                            )
                                        )
                                        estaSiendoVisto[dispositivo] = true
                                        animado = true
                                    }
                                    markerList.add(marker)

                                    //evento para animar si es que esta siendo observado
                                    if (estaSiendoVisto[dispositivo]!!) {
                                        val cameraPosition =
                                            CameraPosition.builder().target(marker.position)
                                                .zoom(17f).build()
                                        googleMap.animateCamera(
                                            CameraUpdateFactory.newCameraPosition(
                                                cameraPosition
                                            )
                                        )
                                    }

                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(context, "No se pudo", Toast.LENGTH_LONG).show()
                            }
                        })
                }
            }.addOnFailureListener {
                Toast.makeText(
                    context,
                    "No se pudo",
                    Toast.LENGTH_SHORT
                ).show()
            }


        //Evento al cambiar las paginas
        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                //Movemos la camara al marcador asociado con el paciente
                if (position < pacientes.size) {
                    for (marker in markerList) {
                        if (marker.tag == pacientes[position].usuarioCuidador + pacientes[position].curp) {
                            val cameraPosition =
                                CameraPosition.builder().target(marker.position).zoom(17f).build()
                            googleMap.animateCamera(
                                CameraUpdateFactory.newCameraPosition(
                                    cameraPosition
                                )
                            )
                            //Iniicializamos todos los estados de estar viendo en falso, y luego solo ponemos el seleccionado como verdadero
                            estaSiendoVisto.replaceAll { _, _ -> false }
                            estaSiendoVisto[pacientes[position].usuarioCuidador + pacientes[position].curp] =
                                true
                            break
                        }
                    }

                }
            }
        })

    }
    //inicializar el mapa


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        pacientes.clear()
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        //viewPager y datos
        //Registrar a los pacientes
        viewPager2 = binding.viewPager2

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?

        mapFragment?.getMapAsync(callback)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}

fun isDarkTheme(context: Context): Boolean {
    val currentNightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return currentNightMode == Configuration.UI_MODE_NIGHT_YES
}

