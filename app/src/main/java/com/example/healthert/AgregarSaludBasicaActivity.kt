package com.example.healthert

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.example.healthert.databinding.ActivityAgregarSaludBasicaBinding
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class AgregarSaludBasicaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAgregarSaludBasicaBinding
    private lateinit var continuarButton: Button
    private lateinit var curpTextInputLayout: TextInputLayout
    private lateinit var fechaNacimientoTextInputLayout: TextInputLayout
    private lateinit var alturaTextInputLayout: TextInputLayout
    private lateinit var pesoTextInputLayout: TextInputLayout
    private lateinit var sexoTextInputLayout: TextInputLayout
    private lateinit var sangreTextInputLayout: TextInputLayout
    private lateinit var curpEditText: EditText
    private lateinit var fechaNacimientoEditText: EditText
    private lateinit var alturaEditText: EditText
    private lateinit var pesoEditText: EditText
    private lateinit var sexoAutoCompleteTextView: AutoCompleteTextView
    private lateinit var sangreAutoCompleteTextView: AutoCompleteTextView
    private lateinit var edadTextView: TextView
    private lateinit var datePicker: DatePickerDialog
    private var db = Firebase.firestore.collection("users")
    private var uid = FirebaseAuth.getInstance().uid
    private var curp : String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgregarSaludBasicaBinding.inflate(layoutInflater)
        continuarButton = binding.continuarButton
        curpTextInputLayout = binding.curpTextInputLayout
        fechaNacimientoTextInputLayout = binding.fechaNacimientoTextInputLayout
        alturaTextInputLayout = binding.alturaTextInputLayout
        pesoTextInputLayout = binding.pesoTextInputLayout
        sexoTextInputLayout = binding.sexoTextInputLayout
        sangreTextInputLayout = binding.sangreTextInputLayout
        curpEditText = binding.curpEditText
        fechaNacimientoEditText = binding.fechaNacimientoEditText
        alturaEditText = binding.alturaEditText
        pesoEditText = binding.pesoEditText
        sexoAutoCompleteTextView = binding.sexosAutoCompleteTextView
        sangreAutoCompleteTextView = binding.sangreAutoCompleteTextView
        edadTextView = binding.edadTextView

        //Recuperamos datos si se esta modificando
        if (intent.getBooleanExtra("estaModificando",false)){
            curp = intent.getStringExtra("curp")!!
            recuperarDatos()
        }

        //Se inicializa el datepicker
        datePicker = DatePickerDialog(
            this,
            { _, year, monthOfYear, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, monthOfYear)
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                // Actualiza el texto del EditText con la fecha seleccionada
                val formattedDate =
                    formatDate(selectedDate) // Función auxiliar para formatear la fecha
                fechaNacimientoEditText.setText(formattedDate)

                val anios =
                    (Calendar.getInstance().timeInMillis - selectedDate.timeInMillis) / 31557600000
                edadTextView.text = "$anios años."

            },
            Calendar.getInstance().get(Calendar.YEAR),
            Calendar.getInstance().get(Calendar.MONTH),
            Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        )
        datePicker.datePicker.maxDate = Calendar.getInstance().timeInMillis

        iniciarSelections()

        continuarButton.setOnClickListener {
            if (validarCampos()) iniciarActivityNueva()
        }

        fechaNacimientoEditText.setOnClickListener {
            datePicker.show()
        }

        fechaNacimientoEditText.addTextChangedListener {
            if (it!!.isNotEmpty()) {
                edadTextView.visibility = View.VISIBLE
            } else {
                edadTextView.visibility = View.GONE
            }
        }

        setContentView(binding.root)
    }

    //Inicializa selections
    private fun iniciarSelections() {
        val sexos = resources.getStringArray(R.array.sexosStrings)
        var adapter = ArrayAdapter(this, R.layout.list_item, sexos)
        with(binding.sexosAutoCompleteTextView) {
            setAdapter(adapter)
        }
        val gruposSanguineos = resources.getStringArray(R.array.gruposSanguineosStrings)
        adapter = ArrayAdapter(this, R.layout.list_item, gruposSanguineos)
        with(binding.sangreAutoCompleteTextView) {
            setAdapter(adapter)
        }
    }


    //Valida que los campos no estén vacios
    private fun validarCampos(): Boolean {
        var i = 0
        if (curpEditText.text.toString()
                .isNotEmpty() && curpEditText.text.toString().length == 18
        ) {
            i++
            curpTextInputLayout.error = null
        } else {
            curpTextInputLayout.error = "El campo no puede estar vacio"
        }
        if (fechaNacimientoEditText.text.toString().isNotEmpty()) {
            i++
            fechaNacimientoTextInputLayout.error = null
        } else {
            fechaNacimientoTextInputLayout.error = "El campo no puede estar vacio"
        }
        if (alturaEditText.text.toString().isNotEmpty()) {
            i++
            alturaTextInputLayout.error = null
        } else {
            alturaTextInputLayout.error = "El campo no puede estar vacio"
        }
        if (pesoEditText.text.toString().isNotEmpty()) {
            i++
            pesoTextInputLayout.error = null
        } else {
            pesoTextInputLayout.error="El campo no puede estar vacio"
        }
        if (sexoAutoCompleteTextView.text.toString().isNotEmpty()) {
            i++
            sexoTextInputLayout.error = null
        } else {
            sexoTextInputLayout.error="El campo no puede estar vacio"
        }
        if (sangreAutoCompleteTextView.text.toString().isNotEmpty()) {
            i++
            sangreTextInputLayout.error = null
        } else {
            sangreTextInputLayout.error="El campo no puede estar vacio"
        }
        return i == 6
    }

    private fun iniciarActivityNueva() {
        val intent = Intent(this, AgregarSaludAvanzadaActivity::class.java)
        if (getIntent().getBooleanExtra("estaModificando",false)){
            intent.putExtra("estaModificando",true)
        }
        intent.putExtra("curp", curpEditText.text.toString())
        intent.putExtra("fechaNacimiento", fechaNacimientoEditText.text.toString())
        intent.putExtra("altura", alturaEditText.text.toString())
        intent.putExtra("peso", pesoEditText.text.toString())
        intent.putExtra("sexo", sexoAutoCompleteTextView.text.toString())
        intent.putExtra("grupoSanguineo", sangreAutoCompleteTextView.text.toString())
        intent.putExtra("nombre", getIntent().getStringExtra("nombre") )
        intent.putExtra("apellidoP", getIntent().getStringExtra("apellidoP"))
        intent.putExtra("apellidoM", getIntent().getStringExtra("apellidoM"))
        intent.putExtra("uri", getIntent().getStringExtra("uri"))
        startActivity(intent)
    }

    private fun recuperarDatos(){
        db.document(uid+curp).get().addOnSuccessListener {
           fechaNacimientoEditText.setText(SimpleDateFormat("dd/MM/yyyy").format(it["fechaNacimiento"]).toString())
            curpEditText.setText(it["curp"].toString())
            alturaEditText.setText(it["altura"].toString())
            pesoEditText.setText(it["peso"].toString())
            sexoAutoCompleteTextView.setText(it["sexo"].toString())
            sangreAutoCompleteTextView.setText(it["grupoSanguineo"].toString())
        }
    }

    private fun formatDate(calendar: Calendar): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }
}