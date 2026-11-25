package com.example.proyectofinal.network

data class RegistroRequest(
    val usuario: String,
    val contraseña: String,
    val fecha_nacimiento: String // Formato YYYY-MM-DD
)

data class LoginRequest(
    val usuario: String,
    val contraseña: String
)

data class UsuarioData(
    val Id_usuario: Int,
    val usuario: String,
    val fecha_nacimiento: String,
    val imagen: String?
)

data class LoginResponse(
    val mensaje: String,
    val usuario: UsuarioData
)

data class GenericResponse(
    val mensaje: String,
    val imagen: String? = null
)