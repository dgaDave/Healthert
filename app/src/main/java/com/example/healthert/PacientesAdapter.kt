package com.example.healthert
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class PacientesAdapter(private val context:Context, private val uid:String,private val pacientes: List<com.example.healthert.classes.Paciente>,private val fragment: Fragment) :RecyclerView.Adapter<PacientesAdapter.ViewHolder>(){
    private var storageRef = Firebase.storage.reference
    private var db = Firebase.database.reference.child("medicionTr")

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var nombre: TextView
        var imageView:ImageView
        var pulsoCardiaco:TextView
        init {
            pulsoCardiaco = itemView.findViewById(R.id.ritmoCardiaco)
            nombre = itemView.findViewById(R.id.nombrePacienteTextView)
            imageView = itemView.findViewById(R.id.imageView)
        }
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val nombrec = pacientes[i].nombrec
        val nombres = nombrec["nombres"]
        val apellidoP = nombrec ["apellidoP"]
        val apellidoM = nombrec["apellidoM"]
        val curp = pacientes[i].curp

        db.child("$uid${pacientes[i].curp}").child("bpm").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value!=null) {
                    viewHolder.pulsoCardiaco.text="BPM: ${snapshot.value}"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "No se pudo", Toast.LENGTH_LONG).show()
            }

        })
        val userRef = storageRef.child("images/$uid$curp")
        Glide.with(fragment).load(userRef).into(viewHolder.imageView)
        viewHolder.nombre.text = "$nombres $apellidoP $apellidoM"
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val v = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.card_paciente_layout, viewGroup, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
    return pacientes.size
    }


}