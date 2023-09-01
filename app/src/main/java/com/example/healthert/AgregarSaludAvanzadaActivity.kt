package com.example.healthert

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import com.example.healthert.databinding.ActivityAgregarSaludAvanzadaBinding
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
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


class AgregarSaludAvanzadaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAgregarSaludAvanzadaBinding
    private lateinit var seguroRadioButton: RadioButton
    private lateinit var alergiasRadioButton: RadioButton
    private lateinit var padecimientosRadioButton: RadioButton
    private lateinit var numeroSeguroTextInputLayout: TextInputLayout
    private lateinit var alergiasTextInputLayout: TextInputLayout
    private lateinit var padecimientosTextInputLayout: TextInputLayout
    private lateinit var numeroSeguroEditText: EditText
    private lateinit var alergiasEditText: EditText
    private lateinit var padecimientosEditText: EditText
    private lateinit var registrarButton: Button
    private val db = Firebase.firestore
    private var storageRef = Firebase.storage.reference
    private val uid = FirebaseAuth.getInstance().uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgregarSaludAvanzadaBinding.inflate(layoutInflater)
        seguroRadioButton = binding.seguroRadioButton
        alergiasRadioButton = binding.alergiasRadioButton
        padecimientosRadioButton = binding.padecimientosRadioButton

        numeroSeguroTextInputLayout = binding.numeroSeguroTextField
        alergiasTextInputLayout = binding.alergiasTextField
        padecimientosTextInputLayout = binding.padecimientosTextField

        numeroSeguroEditText = binding.numeroSeguroEditText
        alergiasEditText = binding.alergiasEditText
        padecimientosEditText = binding.padecimientosEditText

        registrarButton = binding.registrarButton

        seguroRadioButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                numeroSeguroTextInputLayout.visibility = View.VISIBLE
            } else {
                numeroSeguroTextInputLayout.visibility = View.GONE
                numeroSeguroEditText.text = null
            }
        }

        alergiasRadioButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                alergiasTextInputLayout.visibility = View.VISIBLE
            } else {
                alergiasTextInputLayout.visibility = View.GONE
                numeroSeguroEditText.text = null
            }
        }

        padecimientosRadioButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                padecimientosTextInputLayout.visibility = View.VISIBLE
            } else {
                padecimientosTextInputLayout.visibility = View.GONE
                numeroSeguroEditText.text = null
            }
        }

        if (intent.getBooleanExtra("estaModificando", false)) {
            recuperarDatos()
        }

        registrarButton.setOnClickListener {
            setContentView(R.layout.loading_layout)
            registrarUsuario(
                intent.getStringExtra("nombre").toString(),
                intent.getStringExtra("apellidoP").toString(),
                intent.getStringExtra("apellidoM").toString(),
                intent.getStringExtra("uri").toString(),
                intent.getStringExtra("sexo").toString(),
                intent.getStringExtra("grupoSanguineo").toString(),
                intent.getStringExtra("curp").toString(),
                convertirFechaALong(intent.getStringExtra("fechaNacimiento").toString()),
                intent.getStringExtra("altura").toString().toInt(),
                intent.getStringExtra("peso").toString().toInt(),
                numeroSeguroEditText.text.toString(),
                alergiasEditText.text.toString(),
                padecimientosEditText.text.toString()
            )
        }

        setContentView(binding.root)
    }

    private fun convertirFechaALong(fecha: String): Long {
        val format = SimpleDateFormat("dd/MM/yyyy")
        return format.parse(fecha).time
    }

    @SuppressLint("SimpleDateFormat")
    private fun crearPDF(
        paciente: MutableMap<String, Any>,
        cuidador: Map<String, *>, uri:String
    ) {
        val archivo: File?
        try {


            val carpeta = "/reportespdf"
            val path =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + carpeta
            val dir = File(path)
            if (!dir.exists()) {
                dir.mkdirs()
                Toast.makeText(this, "CARPETA CREADA", Toast.LENGTH_SHORT).show()
            }

            archivo = File.createTempFile("tempdf", "pdf")
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
                Image.getInstance(drawableToBytes(this.getDrawable(R.drawable.logo_doc)!!))

            image.scaleAbsolute(100f, 100f)
            val imageCell = PdfPCell(image)
            imageCell.verticalAlignment = Element.ALIGN_MIDDLE
            imageCell.horizontalAlignment = Element.ALIGN_CENTER
            imageCell.border = PdfPCell.NO_BORDER
            table.addCell(imageCell)

            val tituloApp = Paragraph("Healthert", FontFactory.getFont("roboto", 50f, Font.BOLD))
            val tituloCell = PdfPCell(tituloApp)
            tituloCell.border = PdfPCell.NO_BORDER
            tituloCell.verticalAlignment = Element.ALIGN_MIDDLE
            tituloCell.colspan = 3
            table.addCell(tituloCell)
            documento.add(table)

            //Anadir imagen de la persona en el futuro.

            var titulo = Paragraph(
                "Nombre del paciente:",
                FontFactory.getFont("arial", 16f, Font.BOLD)
            )
            documento.add(titulo)
            var nombrec = paciente["nombrec"] as Map<String, String>
            var contenido = Paragraph(
                "${nombrec["nombres"]} ${nombrec["apellidoP"]} ${nombrec["apellidoM"]}",
                FontFactory.getFont("arial", 16f)
            )
            documento.add(contenido)

            titulo = Paragraph(
                "CURP:",
                FontFactory.getFont("arial", 16f, Font.BOLD)
            )
            documento.add(titulo)
            contenido = Paragraph("${paciente["curp"]}", FontFactory.getFont("arial", 16f))
            documento.add(contenido)

            if (paciente["seguro"] != null) {
                titulo = Paragraph(
                    "Seguro:",
                    FontFactory.getFont("arial", 16f, Font.BOLD)
                )
                documento.add(titulo)
                contenido = Paragraph("${paciente["seguro"]}", FontFactory.getFont("arial", 16f))
                documento.add(contenido)
            }

            titulo = Paragraph(
                "Informacion medica:",
                FontFactory.getFont("arial", 16f, Font.BOLD)
            )
            documento.add(titulo)

            val table2 = PdfPTable(2)
            table2.widthPercentage = 100f

            val altura = paciente["altura"]
            Log.e("sss", altura.toString())
            var oracion = "Altura: " + paciente["altura"].toString() + " cm."
            contenido = Paragraph(oracion, FontFactory.getFont("arial", 16f))
            var contenidoPCell = PdfPCell(contenido)
            contenidoPCell.border = PdfPCell.NO_BORDER
            table2.addCell(contenidoPCell)

            oracion = "Peso: ${paciente["peso"].toString()} kg."
            contenido = Paragraph(oracion, FontFactory.getFont("arial", 16f))
            contenidoPCell = PdfPCell(contenido)
            contenidoPCell.border = PdfPCell.NO_BORDER
            table2.addCell(contenidoPCell)

            val edad = ((System.currentTimeMillis() - paciente["fechaNacimiento"].toString()
                .toLong()) / (365.25 * 24 * 60 * 60 * 1000)).toInt()
            oracion = "Edad: $edad anos."
            contenido = Paragraph(oracion, FontFactory.getFont("arial", 16f))
            contenidoPCell = PdfPCell(contenido)
            contenidoPCell.border = PdfPCell.NO_BORDER
            table2.addCell(contenidoPCell)

            contenido = Paragraph("Sexo: ${paciente["sexo"]}.", FontFactory.getFont("arial", 16f))
            contenidoPCell = PdfPCell(contenido)
            contenidoPCell.border = PdfPCell.NO_BORDER
            table2.addCell(contenidoPCell)

            contenido = Paragraph("Grupo sanguineo: ${paciente["grupoSanguineo"]}.", FontFactory.getFont("arial", 16f))
            contenidoPCell = PdfPCell(contenido)
            contenidoPCell.border = PdfPCell.NO_BORDER
            contenidoPCell.colspan = 2
            table2.addCell(contenidoPCell)

            documento.add(table2)

            titulo = Paragraph(
                "Alergias:",
                FontFactory.getFont("arial", 16f, Font.BOLD)
            )
            documento.add(titulo)

            contenido = Paragraph(
                "${paciente["alergias"]}",
                FontFactory.getFont("arial", 16f)
            )
            documento.add(contenido)

            titulo = Paragraph(
                "Padecimientos:",
                FontFactory.getFont("arial", 16f, Font.BOLD)
            )
            documento.add(titulo)

            contenido = Paragraph(
                "${paciente["padecimientos"]}",
                FontFactory.getFont("arial", 16f)
            )
            documento.add(contenido)

            titulo = Paragraph(
                "Contacto de emergencia:",
                FontFactory.getFont("arial", 16f, Font.BOLD)
            )
            documento.add(titulo)

            nombrec = cuidador["nombrec"] as Map<String, String>
            contenido = Paragraph(
                "Nombre: ${nombrec["nombres"]} ${nombrec["apellidoP"]} ${nombrec["apellidoM"]}",
                FontFactory.getFont("arial", 16f)
            )
            documento.add(contenido)

            val domicilio = cuidador["domicilio"] as Map<String, String>

            contenido = Paragraph(
                "Domicilio: ${domicilio["calle"]} ${domicilio["colonia"]}, ${domicilio["codigoPostal"]}, ${domicilio["municipio"]}, ${domicilio["estado"]}",
                FontFactory.getFont("arial", 16f)
            )
            documento.add(contenido)

            contenido = Paragraph(
                "Numero: ${cuidador["telefono"]}",
                FontFactory.getFont("arial", 16f)
            )
            documento.add(contenido)

            contenido = Paragraph(
                "Email: ${cuidador["email"]}",
                FontFactory.getFont("arial", 16f)
            )
            documento.add(contenido)

            documento.close()
            val uploadTask = storageRef.child("/fichas/$uid${paciente["curp"]}")
            uploadTask.putFile(Uri.fromFile(archivo)).addOnSuccessListener {
                Log.e("ssssss", "Se subio")
            }.addOnFailureListener {
                Log.e("ssssss", "no se subio")
            }
            archivo.delete()

            Toast.makeText(
                this,
                "Se ha subido la ficha medica",
                Toast.LENGTH_SHORT
            ).show()

        } catch (e: DocumentException) {
            Toast.makeText(this, "No se pudo generar el reporte", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        } catch (e: FileNotFoundException) {
            Toast.makeText(this, "No se pudo generar el reporte", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }


    }

    private fun drawableToBytes(drawable: Drawable): ByteArray {
        val bitmap = (drawable as BitmapDrawable).bitmap
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    private fun registrarUsuario(
        nombre: String,
        apellidoP: String,
        apellidoM: String,
        uri: String,
        sexo: String,
        grupoSanguineo: String,
        curp: String,
        fechaNacimiento: Long,
        altura: Int,
        peso: Int,
        seguro: String,
        alergias: String,
        padecimientos: String
    ) {
        val nombrec = mapOf(
            "nombres" to nombre,
            "apellidoP" to apellidoP,
            "apellidoM" to apellidoM
        )

        val paciente = mutableMapOf(
            "nombrec" to nombrec,
            "sexo" to sexo,
            "grupoSanguineo" to grupoSanguineo,
            "curp" to curp,
            "fechaNacimiento" to fechaNacimiento,
            "altura" to altura,
            "peso" to peso,
            "usuarioCuidador" to uid.toString()
        )

        if (!seguro.isNullOrEmpty()) paciente["seguro"] = seguro
        if (!alergias.isNullOrEmpty()) paciente["alergias"] = alergias
        if (!padecimientos.isNullOrEmpty()) paciente["padecimientos"] = padecimientos

        db.collection("users").document("$uid").get().addOnSuccessListener {
            crearPDF(paciente, it.data as Map<String, *>,uri)
        }.addOnFailureListener {
            Toast.makeText(
                this,
                "No se pudo recuperar la informacion del cuidador",
                Toast.LENGTH_SHORT
            ).show()
        }

        //Se sube la informacion
        db.collection("users").document(uid.toString() + curp).set(paciente).addOnSuccessListener {

            if (uri == "noModifica") {
                val intent = Intent(this, VincularActivity::class.java)
                val codigo =
                    uid!!.substring(0..2) + curp.substring(curp.length - 3..curp.length - 1)
                intent.putExtra("codigo", codigo)
                intent.putExtra("documento", uid + curp)
                startActivity(intent)
                finishAffinity()
            } else {

                //Se sube la foto
                val file = Uri.parse(uri)
                val imgsRef =
                    storageRef.child("images/" + FirebaseAuth.getInstance().uid.toString() + curp)
                val uploadTask = imgsRef.putFile(file)

                uploadTask
                    .addOnSuccessListener {
                        //Mandamos a la actividad de vinculacion
                        val intent = Intent(this, VincularActivity::class.java)
                        val codigo =
                            uid!!.substring(0..2) + curp.substring(curp.length - 3..curp.length - 1)
                        intent.putExtra("codigo", codigo)
                        intent.putExtra("documento", uid + curp)
                        startActivity(intent)
                        finishAffinity()
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            this,
                            "No se pudo registrar la foto en el sistema",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
            }

        }.addOnFailureListener {
            Toast.makeText(this, "No se pudo registrar los datos del paciente", Toast.LENGTH_SHORT)
                .show()
        }

    }

    private fun recuperarDatos() {
        db.collection("users").document(uid + intent.getStringExtra("curp")).get()
            .addOnSuccessListener {
                if (it["seguro"] != null) {
                    seguroRadioButton.isChecked = true
                    numeroSeguroEditText.setText(it["seguro"].toString())
                }
                if (it["alergias"] != null) {
                    alergiasRadioButton.isChecked = true
                    alergiasEditText.setText(it["alergias"].toString())
                }
                if (it["padecimientos"] != null) {
                    padecimientosRadioButton.isChecked = true
                    padecimientosEditText.setText(it["padecimientos"].toString())
                }
            }
    }


}