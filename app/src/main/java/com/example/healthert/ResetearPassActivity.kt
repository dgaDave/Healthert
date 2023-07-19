package com.example.healthert

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.healthert.databinding.ActivityResetearPassBinding
import com.google.firebase.auth.FirebaseAuth

class ResetearPassActivity : AppCompatActivity() {
    private lateinit var resetButton : Button
    private lateinit var emailEdit : EditText
    private lateinit var binding: ActivityResetearPassBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resetear_pass)

        val emailPuesto:String?= intent.getStringExtra("email")
        initElements()
        if(emailPuesto!=null) emailEdit.setText(emailPuesto)

       //listener para enviar correo y resetear contraseña
        resetButton.setOnClickListener {
            forgotPassWD()
        }
        setContentView(binding.root)
    }

    //Funcion para resetear contraseña
    private fun forgotPassWD() {
        val email = emailEdit.text.toString().replace(" ", "")
        if (email.isNotEmpty()){
            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this, "Correo enviado para cambiar la contraseña a : $email", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finishAffinity()
                    }else{
                        Toast.makeText(this, "No se encontró el usuario con este correo", Toast.LENGTH_SHORT).show()
                    }
                }
        }else  Toast.makeText(this, "Ingresa un correo", Toast.LENGTH_SHORT).show()
    }
    //Funcion para inicializar elementos a usar
    private fun initElements(){
        binding = ActivityResetearPassBinding.inflate(layoutInflater)
        resetButton = binding.enviarCorreo
        emailEdit = binding.emailEditTextsend
    }
}