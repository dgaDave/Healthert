package com.example.healthert

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat

class TratamientosAdapter(private val medicamentosAgendados : List<PacientesSaludAdapter.Tratamiento>):RecyclerView.Adapter<TratamientosAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val nombreMedicamento : TextView
        val indicaciones : TextView
        val fechaInicio : TextView
        val fechaFinal : TextView

        init {
            nombreMedicamento = itemView.findViewById(R.id.nombreMedicamentoTextView)
            indicaciones = itemView.findViewById(R.id.indicacionesTextView)
            fechaInicio = itemView.findViewById(R.id.fechaInicio)
            fechaFinal = itemView.findViewById(R.id.fechaFinal)
        }
    }



    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        i: Int
    ): TratamientosAdapter.ViewHolder {
        val v = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.card_tratamiento_paciente_layout, viewGroup, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(viewHolder: TratamientosAdapter.ViewHolder, i: Int) {
        val nombreMedicamento = medicamentosAgendados[i].nombreMedicamento
        val indicaciones = "${medicamentosAgendados[i].cantidad} cada ${medicamentosAgendados[i].numHoras} horas"
        val fechasInicio = "Inicio: ${SimpleDateFormat("dd/MM/yyyy").format(medicamentosAgendados[i].fechaIni?.toDate()!!)}"
        val fechaFinal = "Fin: ${SimpleDateFormat("dd/MM/yyyy").format(medicamentosAgendados[i].fechaFinal?.toDate()!!)}"

        viewHolder.nombreMedicamento.text = nombreMedicamento
        viewHolder.indicaciones.text = indicaciones
        viewHolder.fechaInicio.text = fechasInicio
        viewHolder.fechaFinal.text = fechaFinal
    }

    override fun getItemCount(): Int {
        return medicamentosAgendados.size
    }

}