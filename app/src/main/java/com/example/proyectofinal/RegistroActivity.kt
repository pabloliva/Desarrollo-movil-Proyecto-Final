package com.example.proyectofinal

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.proyectofinal.network.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class RegistroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        val etUsuario = findViewById<EditText>(R.id.etRegUsuario)
        val etPassword = findViewById<EditText>(R.id.etRegPassword)
        val etFecha = findViewById<EditText>(R.id.etRegFecha)
        val btnRegistrar = findViewById<Button>(R.id.btnRegistrar)

        // Abrir calendario al tocar el campo fecha
        etFecha.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                val fechaFormateada = String.format("%04d-%02d-%02d", year, month + 1, day)
                etFecha.setText(fechaFormateada)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnRegistrar.setOnClickListener {
            val user = etUsuario.text.toString().trim()
            val pass = etPassword.text.toString().trim()
            val fecha = etFecha.text.toString().trim()

            if (user.isEmpty() || pass.isEmpty() || fecha.isEmpty()) {
                Toast.makeText(this, "Llena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            RetrofitClient.instance.registrarUsuario(RegistroRequest(user, pass, fecha))
                .enqueue(object : Callback<GenericResponse> {
                    override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@RegistroActivity, "Usuario registrado con éxito", Toast.LENGTH_SHORT).show()
                            finish() // Regresa al Login
                        } else {
                            Toast.makeText(this@RegistroActivity, "Error: El usuario ya existe o datos inválidos", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                        Toast.makeText(this@RegistroActivity, "Error de conexión: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                })
        }
    }
}