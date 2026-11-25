package com.example.proyectofinal.network

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @POST("api/registro")
    fun registrarUsuario(@Body request: RegistroRequest): Call<GenericResponse>

    @POST("api/login")
    fun loginUsuario(@Body request: LoginRequest): Call<LoginResponse>

    @Multipart
    @POST("api/usuario/{id}/imagen")
    fun subirImagen(
        @Path("id") idUsuario: Int,
        @Part imagen: MultipartBody.Part
    ): Call<GenericResponse>
}