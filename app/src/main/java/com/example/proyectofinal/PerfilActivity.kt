package com.example.proyectofinal

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.example.proyectofinal.network.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class PerfilActivity : AppCompatActivity() {

    private var idUsuario: Int = -1
    private lateinit var ivFotoPerfil: ImageView
    private var fotoCamaraUri: Uri? = null

    // Launcher para Galería
    private val seleccionarGaleriaLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            ivFotoPerfil.setImageURI(it)
            subirImagenAlServidor(it)
        }
    }

    // Launcher para Cámara
    private val tomarFotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        if (exito && fotoCamaraUri != null) {
            ivFotoPerfil.setImageURI(fotoCamaraUri)
            subirImagenAlServidor(fotoCamaraUri!!)
        }
    }

    // Permiso de Cámara
    private val permisoCamaraLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) abrirCamara()
        else Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        ivFotoPerfil = findViewById(R.id.ivFotoPerfil)
        val tvUsuario = findViewById<TextView>(R.id.tvPerfilUsuario)
        val tvFecha = findViewById<TextView>(R.id.tvPerfilFecha)
        val btnTomarFoto = findViewById<Button>(R.id.btnTomarFoto)
        val btnGaleria = findViewById<Button>(R.id.btnGaleria)
        val btnCerrarSesion = findViewById<Button>(R.id.btnCerrarSesion)

        // Recibir datos del Login
        idUsuario = intent.getIntExtra("ID_USUARIO", -1)
        val nombreUsuario = intent.getStringExtra("USUARIO") ?: ""
        val fechaNac = intent.getStringExtra("FECHA")?.take(10) ?: ""
        val rutaImagen = intent.getStringExtra("IMAGEN")

        tvUsuario.text = "Usuario: $nombreUsuario"
        tvFecha.text = "Fecha de nacimiento: $fechaNac"

        // Cargar imagen desde EC2 si ya existe
        if (!rutaImagen.isNullOrEmpty()) {
            val urlCompleta = RetrofitClient.BASE_URL + rutaImagen.removePrefix("/")
            Glide.with(this).load(urlCompleta).into(ivFotoPerfil)
        }

        btnTomarFoto.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                abrirCamara()
            } else {
                permisoCamaraLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        btnGaleria.setOnClickListener {
            seleccionarGaleriaLauncher.launch("image/*")
        }

        btnCerrarSesion.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun abrirCamara() {
        val archivoTemporal = File.createTempFile("foto_perfil_", ".jpg", cacheDir)
        fotoCamaraUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", archivoTemporal)
        tomarFotoLauncher.launch(fotoCamaraUri!!)
    }

    private fun subirImagenAlServidor(uri: Uri) {
        // Convertir Uri a File temporal para enviarlo con Retrofit
        val inputStream = contentResolver.openInputStream(uri) ?: return
        val archivo = File(cacheDir, "upload_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(archivo)
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()

        val requestFile = archivo.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("imagen", archivo.name, requestFile)

        Toast.makeText(this, "Subiendo imagen...", Toast.LENGTH_SHORT).show()

        RetrofitClient.instance.subirImagen(idUsuario, body).enqueue(object : Callback<GenericResponse> {
            override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@PerfilActivity, "Foto actualizada en el servidor", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@PerfilActivity, "Error al guardar en servidor", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                Toast.makeText(this@PerfilActivity, "Error de subida: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}