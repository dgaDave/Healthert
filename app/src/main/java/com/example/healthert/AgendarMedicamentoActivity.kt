package com.example.healthert

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.example.healthert.databinding.ActivityAgendarMedicamentoBinding
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

private lateinit var binding: ActivityAgendarMedicamentoBinding
private lateinit var nombreMedicamentoEditText: EditText
private lateinit var cantidadEditText: EditText
private lateinit var numeroDiasEditText: EditText
private lateinit var numeroHorasEditText: EditText
private lateinit var diaInicioEditText: EditText
private lateinit var horaInicioEditText: EditText
private lateinit var datePicker: DatePickerDialog
private lateinit var timePicker: TimePickerDialog
private lateinit var agendarBoton: Button
private lateinit var ref: String
private val db = Firebase.firestore

class AgendarMedicamentoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgendarMedicamentoBinding.inflate(layoutInflater)
        nombreMedicamentoEditText = binding.nombreMedicamentoEditText
        cantidadEditText = binding.cantidadEditText
        numeroDiasEditText = binding.numeroDiasEditText
        numeroHorasEditText = binding.numeroHorasEditText
        diaInicioEditText = binding.diaInicioEditText
        horaInicioEditText = binding.horaInicioEditText
        agendarBoton = binding.agendarBoton
        ref = intent.getStringExtra("paciente").toString()

        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val currentHour = calendar.get(Calendar.HOUR)
        val currentMinute = calendar.get(Calendar.MINUTE)

        //Date picker (Fecha)
        datePicker = DatePickerDialog(this, { _, year, monthOfYear, dayOfMonth ->
            // Lógica a realizar cuando se selecciona una fecha
            // Aquí puedes usar las variables year, monthOfYear y dayOfMonth para obtener la fecha seleccionada
            val selectedDate = Calendar.getInstance()
            selectedDate.set(Calendar.YEAR, year)
            selectedDate.set(Calendar.MONTH, monthOfYear)
            selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            // Realiza la lógica adicional que desees con la fecha seleccionada
            // ...

            // Actualiza el texto del EditText con la fecha seleccionada
            val formattedDate = formatDate(selectedDate) // Función auxiliar para formatear la fecha
            diaInicioEditText.setText(formattedDate)
        }, currentYear, currentMonth, currentDay)
        datePicker.datePicker.minDate = calendar.timeInMillis

        timePicker = TimePickerDialog(
            this,
            TimePickerDialog.OnTimeSetListener { _, selectedHour, selectedMinute ->
                val selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                horaInicioEditText.setText(selectedTime)
            },
            currentHour,
            currentMinute,
            false
        )

        horaInicioEditText.setOnClickListener {
            timePicker.show()
        }

        diaInicioEditText.setOnClickListener {
            datePicker.show()
        }

        agendarBoton.setOnClickListener {
            if (validar()) {
                val nombre = nombreMedicamentoEditText.text.toString()
                val cantidad = cantidadEditText.text.toString().toInt()
                val numDias = numeroDiasEditText.text.toString().toInt()
                val numHoras = numeroHorasEditText.text.toString().toInt()
                val diaInicio = diaInicioEditText.text.toString()
                val horaInicio = horaInicioEditText.text.toString()
                val momentoInicio =
                    SimpleDateFormat("dd/MM/yyyy HH:mm").parse(("$diaInicio $horaInicio")).time
                val momentoFinal = momentoInicio + 86400000 * numDias
                var ini = momentoInicio

                val tratamiento = mapOf(
                    "nombreMedicamento" to nombre,
                    "cantidad" to cantidad,
                    "numHoras" to numHoras,
                    "fechaIni" to Timestamp(Date(momentoInicio)),
                    "fechaFinal" to Timestamp(Date(momentoFinal)),
                    "paciente" to ref
                )

                db.collection("tratamientos").add(tratamiento)

                while (ini < momentoFinal) {

                    val nDare = Date(ini)
                    val timestamp = Timestamp(nDare)

                    val data = mapOf(
                        "nombreMedicamento" to nombre,
                        "cantidad" to cantidad,
                        "timestamp" to timestamp,
                        "fechaLong" to ini,
                        "paciente" to ref
                    )

                    db.collection("medicamentos").add(data).addOnSuccessListener {
                        Log.e("subir", "Se subio correctamente")
                    }

                    ini += 3600000 * numHoras
                }
                startActivity(Intent(this,MainActivity2::class.java))
                finishAffinity()

            }
        }

        setContentView(binding.root)
    }

    private fun validar(): Boolean {

        return if (validarCantidad()&&validarNombre()&&validarDiaInicio()&&validarHoraInicio()&&validarNumeroDias()&&validarNumeroHoras()) {
            return true
        }else{
            nombreMedicamentoEditText.addTextChangedListener {
                validarNombre()
            }
            cantidadEditText.addTextChangedListener {
                validarCantidad()
            }
            numeroDiasEditText.addTextChangedListener {
                validarNumeroDias()
            }
            numeroHorasEditText.addTextChangedListener {
                validarNumeroHoras()
            }
            diaInicioEditText.addTextChangedListener {
                validarDiaInicio()
            }
            horaInicioEditText.addTextChangedListener {
                validarHoraInicio()
            }
        return false}
    }

    private fun validarHoraInicio():Boolean{
        return if (horaInicioEditText.text.isEmpty()) {
            binding.horaInicioTextField.error = "Escribe la cantidad"
            false
        } else {
            binding.horaInicioTextField.error = null
            true
        }
    }


    private fun validarDiaInicio():Boolean{
        return if (diaInicioEditText.text.isEmpty()) {
            binding.diaInicioTextField.error = "Escribe la fecha"
            false
        } else {
            binding.diaInicioTextField.error = null
            true
        }
    }


    private fun validarNumeroDias():Boolean{
        return if (numeroDiasEditText.text.isEmpty()) {
            binding.numeroDiasTextField.error = "Introduce los dias"
            false
        } else {
            binding.numeroDiasTextField.error = null
            true
        }

    }

    private fun validarNumeroHoras():Boolean{
        return if(numeroHorasEditText.text.isEmpty()) {
            binding.numeroHorasTextField.error = "Escribe la cantidad"
            false
        } else {
            binding.numeroHorasTextField.error = null
            true
        }
    }

    private fun validarCantidad():Boolean{
        return if (cantidadEditText.text.isEmpty()) {
            binding.cantidadTextField.error = "Escribe la cantidad"
            false
        } else {
            binding.cantidadTextField.error = null
            true
        }
    }

    private fun validarNombre():Boolean{
        return if (nombreMedicamentoEditText.text.isEmpty()) {
            binding.nombreMedicamentoTextField.error = "Escribe el nombre"
            false
        } else {
            binding.nombreMedicamentoTextField.error = null
            true
        }
    }



    private fun formatDate(calendar: Calendar): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        return dateFormat.format(calendar.time)
    }
}