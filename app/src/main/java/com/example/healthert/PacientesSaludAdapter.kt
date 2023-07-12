package com.example.healthert

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.healthert.ui.dashboard.Paciente
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import kotlinx.coroutines.delay
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar

class PacientesSaludAdapter(
    private val context: Context,
    private val uid: String,
    private val pacientes: List<Paciente>,
    private val fragment: Fragment,
    private val activity : Activity
) :
    RecyclerView.Adapter<PacientesSaludAdapter.ViewHolder>() {
    private var storageRef = Firebase.storage.reference
    private var db = Firebase.firestore

    //private var db = Firebase.database.reference.child("medicionTr")


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var nombre: TextView
        var imageView: ImageView
        var floatingActionButtonAgendar: FloatingActionButton
        var floatingActionButtonReportar: FloatingActionButton
        var floatingActionButtonEditar: FloatingActionButton
        var floatingActionButtonAtras: FloatingActionButton
        var floatingActionButtonSiguiente: FloatingActionButton
        var curp: TextView
        var peso: TextView
        var altura: TextView
        var edad: TextView
        var padecimientos: TextView
        var tratamientosText: TextView
        var alergias: TextView
        var viewPagerTratamientos: ViewPager2


        init {
            viewPagerTratamientos = itemView.findViewById(R.id.viewPagerTratamientosAgendados)
            floatingActionButtonReportar = itemView.findViewById(R.id.floatingActionButtonReportar)
            floatingActionButtonAgendar = itemView.findViewById(R.id.floatingActionButtonAgendar)
            floatingActionButtonEditar = itemView.findViewById(R.id.floatingActionButtonEditar)
            floatingActionButtonAtras = itemView.findViewById(R.id.floatingActionButtonAtras)
            floatingActionButtonSiguiente =
                itemView.findViewById(R.id.floatingActionButtonSiguiente)
            nombre = itemView.findViewById(R.id.nombrePacienteTextView)
            imageView = itemView.findViewById(R.id.imageView)
            curp = itemView.findViewById(R.id.curpTextView)
            peso = itemView.findViewById(R.id.pesoTextView)
            altura = itemView.findViewById(R.id.alturaTextView)
            edad = itemView.findViewById(R.id.edadTextView)
            padecimientos = itemView.findViewById(R.id.padecimientosTextView)
            alergias = itemView.findViewById(R.id.alergiasTextView)
            tratamientosText = itemView.findViewById(R.id.tratamientosTextView)
        }
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val nombrec = pacientes[i].nombrec as HashMap<String, String>
        val nombres = nombrec["nombres"]
        val apellidoP = nombrec["apellidoP"]
        val apellidoM = nombrec["apellidoM"]
        val nombrecS = "$nombres $apellidoP $apellidoM"
        val curp = pacientes[i].curp
        val usuarioCuidador = pacientes[i].usuarioCuidador
        val peso = pacientes[i].peso
        val altura = pacientes[i].altura
        val edad = pacientes[i].edad
        val padecimientos = pacientes[i].padecimientos
        val alergias = pacientes[i].alergias
        var adapter: TratamientosAdapter
        var tratamientos = mutableListOf<Tratamiento>()
        var alertas = mutableListOf<Alerta>()
        var historiales = mutableListOf<Historial>()
        val timestampFin = Timestamp.now()
        val calendar = Calendar.getInstance()
        calendar.time = timestampFin.toDate()
        calendar.add(Calendar.MONTH, -1)
        val timestampIni = Timestamp(calendar.time)


        db.collection("tratamientos")
            .whereEqualTo("paciente", "${pacientes[i].usuarioCuidador}${pacientes[i].curp}").get()
            .addOnSuccessListener {
                for (document in it) {
                    val tratamiento = document.toObject(Tratamiento::class.java)
                    tratamientos.add(tratamiento)
                }
                adapter = TratamientosAdapter(tratamientos)
                viewHolder.viewPagerTratamientos.adapter = adapter

                if (tratamientos.isNullOrEmpty()) {
                    viewHolder.floatingActionButtonAtras.hide()
                    viewHolder.floatingActionButtonSiguiente.hide()
                    viewHolder.viewPagerTratamientos.visibility = View.GONE
                    viewHolder.tratamientosText.text = "No tienes tratamientos"
                }
            }


        //Ordenar con los longs ya hechos

        db.collection("alertas").whereEqualTo("usuarioCuidador", "${pacientes[i].usuarioCuidador}")
            .get().addOnSuccessListener {
            for (document in it) {
                val alerta = document.toObject(Alerta::class.java)
                alertas.add(alerta)
            }
                alertas.sortBy { o-> o.fechaLong }
        }.addOnFailureListener {
            Toast.makeText(context, "Valio verga", Toast.LENGTH_SHORT).show()

        }
        db.collection("historial").whereEqualTo("paciente", "$usuarioCuidador$curp").get()
            .addOnSuccessListener {

                for (document in it) {
                    val historial = document.toObject(Historial::class.java)
                    historiales.add(historial)
                }
                historiales.sortBy {o->  o.fechaLong }
            }.addOnFailureListener {

        }


        var userRef = storageRef.child("images/" + "$uid$curp")
        Glide.with(fragment).load(userRef).into(viewHolder.imageView)
        viewHolder.nombre.text = "$nombres $apellidoP $apellidoM"
        viewHolder.curp.text = "$curp"
        viewHolder.peso.text = "$peso kg"
        viewHolder.altura.text = "$altura cm"
        viewHolder.edad.text = "$edad años"
        viewHolder.padecimientos.text = "$padecimientos"
        viewHolder.alergias.text = "$alergias"



        viewHolder.floatingActionButtonAgendar.setOnClickListener {
            var agendar = Intent(context, AgendarMedicamentoActivity::class.java)
            agendar.putExtra("paciente", "$uid$curp")
            context.startActivity(agendar)
        }
        viewHolder.floatingActionButtonEditar.setOnClickListener {
            //Codigo Editar
        }
        viewHolder.floatingActionButtonReportar.setOnClickListener {
            crearPDF(nombrecS, tratamientos, alertas, historiales)
        }
        viewHolder.floatingActionButtonAtras.setOnClickListener {
            viewHolder.viewPagerTratamientos.currentItem =
                viewHolder.viewPagerTratamientos.currentItem - 1
        }
        viewHolder.floatingActionButtonSiguiente.setOnClickListener {
            viewHolder.viewPagerTratamientos.currentItem =
                viewHolder.viewPagerTratamientos.currentItem + 1
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun crearPDF(
        nombre: String,
        tratamientos: List<Tratamiento>,
        alertas: List<Alerta>,
        historiales: List<Historial>
    ) {
        var archivo: File? = null
        try {


            val carpeta = "/reportespdf"
            val path =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + carpeta
            val dir = File(path)
            if (!dir.exists()) {
                dir.mkdirs()
                Toast.makeText(context, "CARPETA CREADA", Toast.LENGTH_SHORT).show()
            }

            val nombrepdf = "${SimpleDateFormat("ddMMyyyyHHss").format(Timestamp.now().toDate())}.pdf"

             archivo = File(
                dir,
                nombrepdf
            )
            val fos = FileOutputStream(archivo)
            val documento = Document()
            PdfWriter.getInstance(documento, fos)
            documento.open()

            val tituloFecha = Paragraph(
                "${SimpleDateFormat("dd/MM/yyyy").format(Timestamp.now().toDate())}\n",
                FontFactory.getFont("roboto", 14f)
            )
            tituloFecha.alignment = Element.ALIGN_RIGHT
            documento.add(tituloFecha)


            val image = Image.getInstance(drawableToBytes(context.getDrawable(R.drawable.logo_doc)!!))

            image.scaleToFit(75f,75f)
            image.alignment = Element.ALIGN_CENTER

            documento.add(image)

            val tituloApp = Paragraph(
                "\nHealthert\n\n",
                FontFactory.getFont("roboto", 22f, Font.BOLD)
            )
            tituloApp.alignment = Element.ALIGN_CENTER
            documento.add(tituloApp)

            val titulo = Paragraph(
                "Reporte del paciente:\n",
                FontFactory.getFont("arial", 14f, Font.BOLD)
            )
            documento.add(titulo)

            val tituloPaciente =
                Paragraph("$nombre \n\n", FontFactory.getFont("arial", 14f))
            documento.add(tituloPaciente)
            if (tratamientos.isNotEmpty()) {

                val tituloTratamientos = Paragraph(
                    "Tratamientos agendados:\n\n",
                    FontFactory.getFont("arial", 16f, Font.BOLD)
                )
                documento.add(tituloTratamientos)


                for (tratamiento in tratamientos) {
                    val nombreMedT = Paragraph(
                        "${tratamiento.nombreMedicamento}.\n",
                        FontFactory.getFont("arial", 14f, Font.BOLD)
                    )
                    documento.add(nombreMedT)
                    val cantidadT = Paragraph(
                        "${tratamiento.cantidad} dosis cada ${tratamiento.numHoras} horas.\n",
                        FontFactory.getFont("arial", 12f)
                    )
                    documento.add(cantidadT)
                    val fechasT = Paragraph(
                        "Durante: ${SimpleDateFormat("dd/MM/yyyy").format(tratamiento.fechaIni?.toDate())} - ${
                            SimpleDateFormat(
                                "dd/MM/yyyy"
                            ).format(tratamiento.fechaFinal?.toDate())
                        }\n",
                        FontFactory.getFont("arial", 12f)
                    )
                    documento.add(fechasT)
                }
                documento.add(Paragraph("\n\n"))
            } else {
                val tituloTratamientos = Paragraph(
                    "No cuenta con tratamientos agendados.\n\n",
                    FontFactory.getFont("arial", 16f, Font.BOLD)
                )
                documento.add(tituloTratamientos)
            }
            if (alertas.isNotEmpty()) {
                val tituloAlertas = Paragraph(
                    "Alertas en el ultimo mes:\n\n",
                    FontFactory.getFont("arial", 16f, Font.BOLD)
                )
                documento.add(tituloAlertas)
                var tabla = PdfPTable(2)
                tabla.addCell("Tipo de alerta")
                tabla.addCell("Fecha")


                for (alerta in alertas) {
                    tabla.addCell(alerta.tipo.replaceFirstChar { it.uppercase() })
                    tabla.addCell("${SimpleDateFormat("dd/MM/yyyy HH:mm").format(alerta.timestamp?.toDate())}")

                }
                documento.add(tabla)
            } else {
                val tituloAlertas = Paragraph(
                    "No tuvo alertas en el ultimo mes.\n\n",
                    FontFactory.getFont("arial", 16f, Font.BOLD)
                )
                documento.add(tituloAlertas)
            }
            if (historiales.isNotEmpty()) {
                val tituloMediciones = Paragraph(
                    "Mediciones en el ultimo mes:\n\n",
                    FontFactory.getFont("arial", 16f, Font.BOLD)
                )
                documento.add(tituloMediciones)

                var tabla = PdfPTable(2)
                tabla.addCell("BPM")
                tabla.addCell("Fecha")


                for (historial in historiales) {
                    tabla.addCell(historial.bpm.toString())
                    tabla.addCell("${SimpleDateFormat("dd/MM/yyyy HH:mm").format(historial.timestamp?.toDate())}")

                }
                documento.add(tabla)

            } else {
                val tituloMediciones = Paragraph(
                    "No tuvo mediciones en el ultimo mes.\n\n",
                    FontFactory.getFont("arial", 16f, Font.BOLD)
                )
                documento.add(tituloMediciones)
            }


            documento.close()
            Toast.makeText(context, "El archivo ha sido generado con exito", Toast.LENGTH_SHORT)
                .show()


        } catch (e: DocumentException) {
            Toast.makeText(context, "No se pudo generar el reporte", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        } catch (e: FileNotFoundException) {
            Toast.makeText(context, "No se pudo generar el reporte", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }



    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val v = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.card_salud_paciente_layout, viewGroup, false)
        return ViewHolder(v)
    }

    private fun drawableToBytes(drawable: Drawable): ByteArray {
        val bitmap = (drawable as BitmapDrawable).bitmap
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }
    override fun getItemCount(): Int {
        return pacientes.size
    }

    data class Tratamiento(
        val nombreMedicamento: String = "",
        val cantidad: Int = 0,
        val numHoras: Int = 0,
        val fechaIni: Timestamp? = null,
        val fechaFinal: Timestamp? = null,
        val paciente: String = ""
    )

    data class Alerta(
        val nombrePaciente: String = "",
        val visto: Boolean = false,
        val timestamp: Timestamp? = null,
        val usuarioCuidador: String = "",
        val paciente: String = "",
        val tipo: String = "",
        val fechaLong: Long = 0
    )

    data class Historial(
        val bpm: Int = 0,
        val timestamp: Timestamp? = null,
        val paciente: String = "",
        val fechaLong: Long = 0
    )

}