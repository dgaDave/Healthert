package com.example.healthert.classes
data class Paciente(
    val nombrec: Map<String,String> = mapOf(),
    val alergias : String = "",
    val altura : Int = 0,
    val curp : String = "",
    val fechaNacimiento : Long = 0,
    val grupoSanguineo : String = "",
    val padecimientos : String = "",
    val peso : Int = 0,
    val seguro : String = "",
    val sexo : String = "",
    val usuarioCuidador : String ="",
    val codigo : String = ""
)