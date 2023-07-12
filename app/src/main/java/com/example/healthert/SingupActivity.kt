package com.example.healthert

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.util.PatternsCompat
import androidx.core.widget.addTextChangedListener
import com.example.healthert.databinding.ActivitySingupBinding
import java.util.regex.Pattern


class SingupActivity : AppCompatActivity() {
    private lateinit var emailEdit: EditText
    private lateinit var passwordEdit: EditText
    private lateinit var registrarBoton: Button
    private lateinit var binding: ActivitySingupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingupBinding.inflate(layoutInflater)

        emailEdit = binding.emailEditText
        passwordEdit = binding.passwordEditText
        registrarBoton = binding.registrarBoton


        //Mandar informacion a la siguiente pantalla
        registrarBoton.setOnClickListener {
            val email = emailEdit.text.toString().replace(" ", "")
            val password = passwordEdit.text.toString()
            if (validarEnConjunto()) {
                val intent = Intent(this, AgregarInfoPersonaActivity::class.java)
                intent.putExtra("email", email)
                intent.putExtra("password", password)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Complete los campos", Toast.LENGTH_SHORT).show()
            }

        }

        setContentView(binding.root)
    }

    //Esta funcion hace que se validen en conjunto y añade los efectos para que salga en rojo si es que no cumple con los requisitos
    private fun validarEnConjunto():Boolean{
       return if (validatePassword() and  validateEmail()){
            true
        }else{
            binding.emailEditText.addTextChangedListener {
                validateEmail()
            }
           binding.passwordEditText.addTextChangedListener {
               validatePassword()
           }
           false
       }
    }

    //Valida el email
    private fun validateEmail(): Boolean {
        val email = binding.emailTextField.editText?.text.toString()
        return if (email.isEmpty()) {
            binding.emailTextField.error = "El campo no puede estar vacio"
            false
        } else if (!PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailTextField.error = "Elige un correo electronico valido"
            false
        } else {
            binding.emailTextField.error = null
            true
        }
    }

    //Valida el password
    private fun validatePassword(): Boolean {
        val password = binding.passwordTextField.editText?.text.toString()
        val passwordRegex = Pattern.compile(
            "^" + "(?=.*[0-9])" +"(?=.*[a-z])" +"(?=.*[A-Z])" +"(?=.*\\W)" +"(?=\\S+$)" + ".{8,}" +"$"
        )
        return if (password.isEmpty()) {
            binding.passwordTextField.error = "El campo no puede estar vacio"
            false
        } else if (!passwordRegex.matcher(password).matches()) {
            if(!Pattern.compile("^" + ".{8,}" +"$"
                ).matcher(password).matches()){
                binding.passwordTextField.error = "Debe tener al menos 8 caracteres"
            }else if(!Pattern.compile("^" + "(?=.*[a-z])"+ ".{8,}" +"$"
                ).matcher(password).matches()){
                binding.passwordTextField.error = "Debe tener al menos 1 minuscula"
            }else if(!Pattern.compile("^"  +"(?=.*[A-Z])" +".{8,}" +"$"
                ).matcher(password).matches()){
                binding.passwordTextField.error = "Debe tener al menos 1 mayuscula"
            }else if(!Pattern.compile("^" + "(?=.*[0-9])" + ".{8,}" +"$"
                ).matcher(password).matches()){
                binding.passwordTextField.error = "Debe tener al menos 1 digito"
            }else if(!Pattern.compile("^" +"(?=.*\\W)"+ ".{8,}" +"$"
                ).matcher(password).matches()){
                binding.passwordTextField.error = "Debe tener al menos 1 caracter especial"
            }else if(!Pattern.compile("^"  +"(?=\\S+$)" + ".{8,}" +"$"
                ).matcher(password).matches()){
                binding.passwordTextField.error = "No debe contener espacios"
            }
            false
        } else {
            binding.passwordTextField.error = null
            true
        }
    }

}


