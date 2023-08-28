package com.example.healthert.ui.notifications

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.healthert.AgregarInfoPersonaActivity
import com.example.healthert.R
import com.example.healthert.VincularActivity
import com.example.healthert.classes.Paciente
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class AjustesPacientesAdapter(
    private val context: Context,
    private val uid: String,
    private val pacientes: List<Paciente>,
    private val activity:Activity
) : RecyclerView.Adapter<AjustesPacientesAdapter.ViewHolder>() {
    private var storageRef = Firebase.storage.reference
    private val db = Firebase.firestore

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var nombrec: TextView
        var imageView: ImageView
        var editarButton: Button
        var vincularButton: Button
        var eliminarButton: Button

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
        val codigo = pacientes[i].codigo

        val userRef = storageRef.child("images/$uid$curp")
        Glide.with(context).load(userRef).into(viewHolder.imageView)
        viewHolder.nombrec.text = "$nombres $apellidoP $apellidoM"

        //Se comprueba si existe un codigo, se asume que nunca se vinculo
        if (!codigo.isNullOrEmpty()) {
            viewHolder.vincularButton.text = "Vincular"
            viewHolder.vincularButton.setOnClickListener {
                val intent = Intent(context, VincularActivity::class.java)
                intent.putExtra("codigo", codigo)
                intent.putExtra("documento", uid + curp)
                context.startActivity(intent)
            }
        } else {
            viewHolder.vincularButton.setOnClickListener {
                //Codigo para desvincular del reloj
            }
        }

        //Evento para eliminar al paciente
        viewHolder.eliminarButton.setOnClickListener {
            MaterialAlertDialogBuilder(context).setTitle("Atencion")
                .setMessage("Estas a punto de borrar por completo a un paciente, estas seguro de esto?")
                .setPositiveButton("Eliminar") { dialog, which ->
                    borrarPaciente(curp)
                }
                .setNegativeButton("Cancelar", null).create().show()
        }
        viewHolder.editarButton.setOnClickListener {
            val intent = Intent(context, AgregarInfoPersonaActivity::class.java)
            intent.putExtra("estaModificando", true)
            intent.putExtra("esPaciente", true)
            intent.putExtra("curp", curp)
            context.startActivity(intent)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val v = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.card_ajustes_pacientes, viewGroup, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return pacientes.size
    }

    private fun borrarPaciente(curp: String) {
        db.collection("users").document(uid + curp).delete().addOnSuccessListener {
            storageRef.child("images/$uid$curp").delete().addOnSuccessListener {
                activity.finish()
            }.addOnFailureListener {

            }
        }.addOnFailureListener {

        }
    }


}