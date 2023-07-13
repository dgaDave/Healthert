package com.example.healthert

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.healthert.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var signup: TextView
    private lateinit var fgPass: TextView
    private lateinit var logIn: Button
    private lateinit var emailEdit: EditText
    private lateinit var passwordEdit: EditText
    private lateinit var binding: ActivityLoginBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        signup = binding.registrarseTextView
        logIn = binding.iniciarBoton
        emailEdit = binding.emailEditText
        passwordEdit = binding.passwordEditText
        fgPass= binding.resetearPassTextView

        //Listener de LogIn
        logIn.setOnClickListener {
            val email = emailEdit.text.toString()
            val password = passwordEdit.text.toString()
            if (validarLogIn(email,password)) {
                logIn(email, password)
            } else {
                Toast.makeText(this, "Completa los campos", Toast.LENGTH_SHORT).show()
            }
        }
        //Listener que manda a resetear la contraseña
        fgPass.setOnClickListener {
            val email = emailEdit.text.toString().replace(" ", "")
            openResetPW(email)

        }

        //Listener de Registro
        signup.setOnClickListener {
            val intent = Intent(this, SingupActivity::class.java)
            startActivity(intent)
        }
        setContentView(binding.root)
    }

    //Funcion para validar el Login
    private fun validarLogIn(email:String,password: String):Boolean{
        return email.replace(" ", "").isNotEmpty() && password.isNotEmpty()
    }

    //Funcion de LogIn
    private fun logIn(email: String, password: String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this, "Loggeado", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity2::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Email y/o contraseña incorrecto", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    //Empezar directamente si encuentra usuario
    override fun onStart() {
        super.onStart()
        if (FirebaseAuth.getInstance().currentUser != null) {
            startActivity(Intent(this, MainActivity2::class.java))
            finish()
        }
    }
    //Funcion para mandar a resetear contraseña--se encarga de mandar el correo al activity sí el usuario lo escribio
    // en el EditText
    private fun openResetPW(email: String){
        val intent = Intent(this, ResetearPassActivity::class.java)
        if(email.isNotEmpty()){
            intent.putExtra("email", email)
            startActivity(intent)
        }else{
            startActivity(intent)
        }

    }


}