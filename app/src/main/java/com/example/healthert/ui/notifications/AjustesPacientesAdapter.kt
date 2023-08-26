package com.example.healthert.ui.notifications

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.healthert.R
import com.example.healthert.classes.Paciente
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class AjustesPacientesAdapter(
    private val context: Context,
    private val uid: String,
    private val pacientes: List<Paciente>
) : RecyclerView.Adapter<AjustesPacientesAdapter.ViewHolder>() {
    private var storageRef = Firebase.storage.reference

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var nombrec: TextView
        var imageView: ImageView
        var editarButton: Button
        var vincularButton: Button
        var eliminarButton : Button

        init {
            nombrec = itemView.findViewById(R.id.nombrePacienteTextView)
            imageView = itemView.findViewById(R.id.imageView)
            editarButton = itemView.findViewById(R.id.editarButton)
            vincularButton = itemView.findViewById(R.id.vincularButton)
            eliminarButton = itemView.findViewById(R.id.eliminarButton)
        }
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val nombrec = pacientes[i].nombrec
        val nombres = nombrec["nombres"]
        val apellidoP = nombrec["apellidoP"]
        val apellidoM = nombrec["apellidoM"]
        val curp = pacientes[i].curp

        val userRef = storageRef.child("images/$uid$curp")
        Glide.with(context).load(userRef).into(viewHolder.imageView)
        viewHolder.nombrec.text = "$nombres $apellidoP $apellidoM"
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val v = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.card_ajustes_pacientes, viewGroup, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return pacientes.size
    }


}