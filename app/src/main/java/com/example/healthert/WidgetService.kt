package com.example.healthert

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.SystemClock
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.healthert.WidgetProvider.Companion.EXTRA_ITEM_POSITION
import com.example.healthert.ui.dashboard.Paciente
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class WidgetService : RemoteViewsService() {

    private val usuarios = Firebase.firestore.collection("users")
    private var db = Firebase.database.reference.child("medicionTr")
    val uid = FirebaseAuth.getInstance().uid.toString()
    private var storageRef = Firebase.storage.reference

    private var exampleData = ArrayList<String>()
    private var curpData = ArrayList<String>()
    private var bitmapLista = ArrayList<Bitmap>()
    private var pulsoPaciente= ArrayList<String>()

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return ExampleWidgetItemFactory(applicationContext, intent)

    }
    internal inner class ExampleWidgetItemFactory(private val context: Context, intent: Intent) :
        RemoteViewsFactory {
        private val appWidgetId: Int

        init {
            appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )

        }

        override fun onCreate() {
            //connect to data source
            SystemClock.sleep(1000)

        }

        override fun onDataSetChanged() {
            //Se actualizan los datos utilizando corrutinas
            runBlocking {
                // Realizar todas las consultas
                realizarConsultas()
                }
        }

        override fun onDestroy() {
            //close data source
        }

        override fun getCount(): Int {
            return exampleData.size
        }

        override fun getViewAt(position: Int): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.example_widget_item)
            if (exampleData.isNotEmpty()  && bitmapLista.isNotEmpty()) {
                views.setTextViewText(R.id.nombrePacienteTv, exampleData[position])
                views.setImageViewBitmap(R.id.imageView,bitmapLista[position])
                views.setTextViewText(R.id.ritmoCardiaco2,"BPM: ${pulsoPaciente[position]}")
                val fillIntent = Intent()
                fillIntent.putExtra(EXTRA_ITEM_POSITION, position)
                views.setOnClickFillInIntent(R.id.nombrePacienteTv, fillIntent)
            } else {
                views.setTextViewText(R.id.nombrePacienteTv, "No hay pacientes")
            }
            return views
        }

        override fun getLoadingView(): RemoteViews? {
            return null
        }

        override fun getViewTypeCount(): Int {
            return 1
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun hasStableIds(): Boolean {
            return true
        }
    }
//Función para consultar pacientes del cuidador
    private suspend fun consultaPacientes(): ArrayList<String> = withContext(Dispatchers.IO) {
        val result = usuarios.whereEqualTo("usuarioCuidador", uid).get().await()
        val data = ArrayList<String>()
        for (document in result) {
            val paciente = document.toObject(Paciente::class.java)
            val nombres = paciente.nombrec["nombres"].toString()
            val apellidoP = paciente.nombrec["apellidoP"].toString()
            val apellidoM = paciente.nombrec["apellidoM"].toString()
            data.add("$nombres $apellidoP $apellidoM")
        }
        data
    }
//Función para consultar curp de pacientes
    private suspend fun consultaCurp(): ArrayList<String> = withContext(Dispatchers.IO) {
        val result = usuarios.whereEqualTo("usuarioCuidador", uid).get().await()
        val dataCurp = ArrayList<String>()
        for (document in result) {
            val paciente = document.toObject(Paciente::class.java)
            val curp = paciente.curp
            dataCurp.add(curp)
        }

        dataCurp
    }
//Función para consultar las imagenes
   private suspend fun consultaBitmap():ArrayList<Bitmap> = withContext(Dispatchers.IO) {
        val bitmapList = ArrayList<Bitmap>()
        for (curp in curpData) {
            val userRef = storageRef.child("images/$uid$curp")
            try {
                val bytes = userRef.getBytes(Long.MAX_VALUE).await()
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                bitmapList.add(bitmap)
            } catch (e: Exception) {
               //error
            }
        }
        bitmapList
    }

///Función para consultar el ritmo cardicaco
    private suspend fun consultaPulso(): ArrayList<String> = withContext(Dispatchers.IO) {
        val pulso = ArrayList<String>()
        for (curp in curpData) {
            try {
                val snapshot = db.child("$uid$curp").child("bpm").get().await()
                if (snapshot.exists()) {
                    val bpm = snapshot.getValue().toString()
                    pulso.add(bpm)
                } else {
                    pulso.add("No se pudo medir")
                }
            } catch (error: Exception) {
                pulso.add("Error: ${error.message}")
            }
        }
        pulso
    }
//Función para inicializar todas las consultas del widget
  private suspend fun realizarConsultas() {
        curpData = consultaCurp()
        exampleData = consultaPacientes()
        bitmapLista = consultaBitmap()
        pulsoPaciente=consultaPulso()

    }




}






