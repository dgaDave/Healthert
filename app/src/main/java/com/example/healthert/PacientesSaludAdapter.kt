package com.example.healthert

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.healthert.ui.home.qrActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PacientesSaludAdapter(
    private val context: Context,
    private val uid: String,
    private val pacientes: List<com.example.healthert.classes.Paciente>,
    private val fragment: Fragment,
) :
    RecyclerView.Adapter<PacientesSaludAdapter.ViewHolder>() {
    private var storageRef = Firebase.storage.reference
    private var db = Firebase.firestore
    private val handler = Handler(Looper.getMainLooper())
    private var CURRENT_ITEM = 0
    //private var db = Firebase.database.reference.child("medicionTr")


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var nombre: TextView
        var curp: TextView
        var peso: TextView
        var altura: TextView
        var edad: TextView
        var sexo: TextView
        var grupoSanguineo: TextView
        var seguro: TextView
        var padecimientos: TextView
        var tratamientosText: TextView
        var alergias: TextView
        var imageView: ImageView
        var agendarImageView: ImageView
        var reportarImageView: ImageView
        var qrImageView: ImageView
        var viewPagerTratamientos: ViewPager2


        init {
            viewPagerTratamientos = itemView.findViewById(R.id.viewPagerTratamientosAgendados)
            reportarImageView = itemView.findViewById(R.id.reportarPacienteImageView)
            agendarImageView = itemView.findViewById(R.id.agendarPacientesImageView)
            qrImageView = itemView.findViewById(R.id.qrPacientesImageView)
            nombre = itemView.findViewById(R.id.nombrePacienteTextView)
            imageView = itemView.findViewById(R.id.imageView)
            curp = itemView.findViewById(R.id.curpTextView)
            peso = itemView.findViewById(R.id.pesoTextView)
            altura = itemView.findViewById(R.id.alturaTextView)
            edad = itemView.findViewById(R.id.edadTextView)
            grupoSanguineo = itemView.findViewById(R.id.grupoSanguineoTextView)
            sexo = itemView.findViewById(R.id.sexoTextView)
            padecimientos = itemView.findViewById(R.id.padecimientosTextView)
            alergias = itemView.findViewById(R.id.alergiasTextView)
            seguro = itemView.findViewById(R.id.seguroTextView)
            tratamientosText = itemView.findViewById(R.id.tratamientosTituloText)
        }
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val nombrec = pacientes[i].nombrec
        val nombres = nombrec["nombres"]
        val apellidoP = nombrec["apellidoP"]
        val apellidoM = nombrec["apellidoM"]
        val nombrecS = "$nombres $apellidoP $apellidoM"
        val alergias = pacientes[i].alergias
        val altura = pacientes[i].altura
        val curp = pacientes[i].curp
        //Se saca el calculo de la edad
        val edad =
            ((System.currentTimeMillis() - pacientes[i].fechaNacimiento) / (365.25 * 24 * 60 * 60 * 1000)).toInt()
        val grupoSanguineo = pacientes[i].grupoSanguineo
        val padecimientos = pacientes[i].padecimientos
        val peso = pacientes[i].peso
        val seguro = pacientes[i].seguro
        val sexo = pacientes[i].sexo

        viewHolder.nombre.text = "$nombres $apellidoP $apellidoM"
        viewHolder.curp.text = curp
        viewHolder.peso.text = "$peso kg"
        viewHolder.altura.text = "$altura cm"
        viewHolder.edad.text = "$edad años"
        viewHolder.seguro.text = seguro
        viewHolder.sexo.text = sexo
        viewHolder.grupoSanguineo.text = grupoSanguineo

        val userRef = storageRef.child("images/" + "$uid$curp")
        Glide.with(fragment).load(userRef).into(viewHolder.imageView)

        val usuarioCuidador = pacientes[i].usuarioCuidador
        var adapter: TratamientosAdapter
        val tratamientos = mutableListOf<Tratamiento>()
        val alertas = mutableListOf<Alerta>()
        val historiales = mutableListOf<Historial>()
        val timestampFin = Timestamp.now()
        val calendar = Calendar.getInstance()
        calendar.time = timestampFin.toDate()
        calendar.add(Calendar.MONTH, -1)


        db.collection("tratamientos")
            .whereEqualTo("paciente", "${pacientes[i].usuarioCuidador}${pacientes[i].curp}").get()
            .addOnSuccessListener {
                for (document in it) {
                    val tratamiento = document.toObject(Tratamiento::class.java)
                    tratamientos.add(tratamiento)
                }
                adapter = TratamientosAdapter(tratamientos)
                viewHolder.viewPagerTratamientos.adapter = adapter

                if (tratamientos.isNotEmpty()) {
                    viewHolder.viewPagerTratamientos.visibility = View.VISIBLE
                }
            }


        //Ordenar con los longs ya hechos

        db.collection("alertas").whereEqualTo("usuarioCuidador", pacientes[i].usuarioCuidador)
            .get().addOnSuccessListener {
                for (document in it) {
                    val alerta = document.toObject(Alerta::class.java)
                    alertas.add(alerta)
                }
                alertas.sortBy { o -> o.fechaLong }
            }.addOnFailureListener {
                Toast.makeText(context, "Valio verga", Toast.LENGTH_SHORT).show()

            }
        db.collection("historial").whereEqualTo("paciente", "$usuarioCuidador$curp").get()
            .addOnSuccessListener {

                for (document in it) {
                    val historial = document.toObject(Historial::class.java)
                    historiales.add(historial)
                }
                historiales.sortBy { o -> o.fechaLong }
            }.addOnFailureListener {

            }

        if (alergias.isNullOrEmpty()) {
            viewHolder.alergias.text = "El paciente no tiene alergias"
        } else {
            viewHolder.alergias.text = alergias
        }
        if (padecimientos.isNullOrEmpty()) {
            viewHolder.padecimientos.text = "El paciente no tiene padecimientos"
        } else {
            viewHolder.padecimientos.text = padecimientos
        }
        if (sexo == "Prefiero no especificar") {
            viewHolder.sexo.visibility = View.GONE
        }
        if (seguro.isNullOrEmpty()) {
            viewHolder.seguro.visibility = View.GONE
        }

        viewHolder.agendarImageView.setOnClickListener {
            val agendar = Intent(context, AgendarMedicamentoActivity::class.java)
            agendar.putExtra("paciente", "$uid$curp")
            context.startActivity(agendar)
        }

        viewHolder.reportarImageView.setOnClickListener {
            crearPDF(nombrecS, tratamientos, alertas, historiales)
        }

        viewHolder.qrImageView.setOnClickListener {
            storageRef.child("fichas/$uid$curp").downloadUrl.addOnSuccessListener {
                val intent = Intent(context,qrActivity::class.java)
                intent.putExtra("link",it.toString())
                context.startActivity(intent)

                //Poner loading en otro activity

            }.addOnFailureListener {
                Toast.makeText(context,"F",Toast.LENGTH_SHORT).show()
            }
        }


        handler.postDelayed(object : Runnable {
            override fun run() {
                if (viewHolder.viewPagerTratamientos.currentItem == (viewHolder.viewPagerTratamientos.adapter!!.itemCount - 1)) {
                    CURRENT_ITEM = 0
                }
                viewHolder.viewPagerTratamientos.setCurrentItem(CURRENT_ITEM++, true)
                handler.postDelayed(this, 3000) // Cambiar de página cada 3 segundos
            }
        }, 3000)

    }

    @SuppressLint("SimpleDateFormat")
    private fun crearPDF(
        nombre: String,
        tratamientos: List<Tratamiento>,
        alertas: List<Alerta>,
        historiales: List<Historial>
    ) {
        val archivo: File?
        try {


            val carpeta = "/reportespdf"
            val path =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + carpeta
            val dir = File(path)
            if (!dir.exists()) {
                dir.mkdirs()
                Toast.makeText(context, "CARPETA CREADA", Toast.LENGTH_SHORT).show()
            }

            val nombrepdf =
                "${SimpleDateFormat("ddMMyyyyHHss").format(Timestamp.now().toDate())}.pdf"

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

            val table = PdfPTable(4)
            table.widthPercentage = 100f



            val image =
                Image.getInstance(drawableToBytes(context.getDrawable(R.drawable.logo_doc)!!))

            image.scaleAbsolute(100f, 100f)
            val imageCell = PdfPCell(image)
            imageCell.verticalAlignment=Element.ALIGN_MIDDLE
            imageCell.horizontalAlignment=Element.ALIGN_CENTER
            imageCell.border = PdfPCell.NO_BORDER
            table.addCell(imageCell)

            val tituloApp = Paragraph("Healthert", FontFactory.getFont("roboto", 40f, Font.BOLD))
            val tituloCell = PdfPCell(tituloApp)
            tituloCell.border = PdfPCell.NO_BORDER
            tituloCell.verticalAlignment = Element.ALIGN_MIDDLE
            tituloCell.colspan = 3
            table.addCell(tituloCell)
            documento.add(table)

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
                        "Durante: ${SimpleDateFormat("dd/MM/yyyy").format(tratamiento.fechaIni?.toDate()!!)} - ${
                            SimpleDateFormat(
                                "dd/MM/yyyy"
                            ).format(tratamiento.fechaFinal?.toDate()!!)
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
                val tabla = PdfPTable(2)
                tabla.addCell("Tipo de alerta")
                tabla.addCell("Fecha")


                for (alerta in alertas) {
                    tabla.addCell(alerta.tipo.replaceFirstChar { it.uppercase() })
                    tabla.addCell(SimpleDateFormat("dd/MM/yyyy HH:mm").format(alerta.timestamp?.toDate()!!))

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

                val tabla = PdfPTable(2)
                tabla.addCell("BPM")
                tabla.addCell("Fecha")


                for (historial in historiales) {
                    tabla.addCell(historial.bpm.toString())
                    tabla.addCell(SimpleDateFormat("dd/MM/yyyy HH:mm").format(historial.timestamp?.toDate()!!))

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

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        handler.removeCallbacksAndMessages(null) // Detener el handler al destruir la actividad
    }

}