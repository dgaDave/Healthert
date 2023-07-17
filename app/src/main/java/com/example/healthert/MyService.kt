package com.example.healthert

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MyService : Service() {
    private lateinit var sharedPreferences: SharedPreferences
    private val alertas = Firebase.firestore
    private lateinit var channel: NotificationChannel
    private lateinit var manager: NotificationManager
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        sharedPreferences = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val uid = sharedPreferences.getString("uid", "")
        //Comprobar que se puedan mandar notificaciones
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = NotificationChannel("alerta", "alerta", NotificationManager.IMPORTANCE_HIGH)
            manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)



            Log.e("uid", "$uid")
            alertas.collection("alertas").whereEqualTo("visto", false)
                .whereEqualTo("usuarioCuidador", uid).addSnapshotListener { snapshot, error ->

                    for (dc in snapshot!!.documentChanges) {
                        when (dc.type) {
                            DocumentChange.Type.ADDED -> {
                                val intent = Intent(Intent.ACTION_VIEW,"https://www.google.com/maps/place/${dc.document.data["latitud"]},${dc.document.data["longitud"]}".toUri())
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                val pendingIntent = PendingIntent.getActivity(applicationContext,10,intent,PendingIntent.FLAG_IMMUTABLE)
                                Log.e("a", "${dc.document.data}")
                                //Crear notificacion y eliminar el registro y/o cambiar el estado de visto para no generar inconsistencias.
                                val notificacion =
                                    NotificationCompat.Builder(applicationContext, "alerta")
                                        .setContentText("${dc.document.data["nombrePaciente"].toString()} en ${dc.document.data["latitud"]} ${dc.document.data["longitud"]}")
                                        .setContentTitle("¡EN PELIGRO!")
                                        .setSmallIcon(R.drawable.logo1_sf)
                                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                                        .setContentIntent(pendingIntent)
                                        .build()
                                NotificationManagerCompat.from(applicationContext).notify(1,notificacion)
                                alertas.collection("alertas").document("${dc.document.id}").update("visto",true)

                            }
                            DocumentChange.Type.MODIFIED -> Log.e("m", "${dc.document.data}")
                            DocumentChange.Type.REMOVED -> Log.e("r", "${dc.document.data}")
                        }
                    }
                }


        }

        return START_STICKY
    }

}