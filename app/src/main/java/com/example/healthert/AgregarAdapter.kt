package com.example.healthert

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class AgregarAdapter(private val context:Context) : RecyclerView.Adapter<AgregarAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var btnAgregar: ImageView

        init {
            btnAgregar = itemView.findViewById(R.id.agregarPacienteBoton)
        }
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        viewHolder.btnAgregar.setOnClickListener {
            val intent = Intent(context,AgregarInfoPersonaActivity::class.java)
            intent.putExtra("esPaciente",true)
            context.startActivity(intent)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val v = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.card_agregar_layout, viewGroup, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return 1
    }
}