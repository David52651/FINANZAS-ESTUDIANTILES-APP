package com.arcjas.studentapp.ui.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.arcjas.studentapp.R
import com.arcjas.studentapp.core.config.VolleyListener
import com.arcjas.studentapp.core.model.ResponseCore
import com.arcjas.studentapp.core.model.User
import com.arcjas.studentapp.core.service.AuthRequest

class RegisterActivity : AppCompatActivity() {

    private lateinit var editTextCode: EditText
    private lateinit var editTextName: EditText
    private lateinit var editTextUsuario: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var btnLogin: Button

    private lateinit var authRequest: AuthRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        editTextCode = findViewById(R.id.txtRegisterCode)
        editTextName = findViewById(R.id.txtRegisterName)
        editTextUsuario = findViewById(R.id.txtRegisterUser)
        editTextEmail = findViewById(R.id.txtRegisterEmail)
        editTextPassword = findViewById(R.id.txtRegisterPassword)
        btnRegister = findViewById(R.id.btnRegister)
        btnLogin = findViewById(R.id.btnLogin)

        authRequest = AuthRequest(this)

        btnRegister.setOnClickListener {
            val code = editTextCode.text.toString().trim()
            val name = editTextName.text.toString().trim()
            val usuario = editTextUsuario.text.toString().trim()
            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            if (code.isEmpty() || name.isEmpty() || usuario.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT)
                    .show()
            } else {
                authRequest.registerUser(
                    usuario,
                    password,
                    email,
                    code,
                    name,
                    object : VolleyListener<User> {
                        override fun onSuccess(response: ResponseCore<User>) {
                            if (response.success && response.data != null) {

                                Toast.makeText(
                                    this@RegisterActivity,
                                    "Registro exitoso",
                                    Toast.LENGTH_SHORT
                                ).show()
                                iniciarSesion(usuario, password)
                            } else {
                                Toast.makeText(
                                    this@RegisterActivity,
                                    response.message ?: "Error en el registro",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        override fun onError(error: String) {
                            Toast.makeText(
                                this@RegisterActivity,
                                "Error en el registro: $error",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
            }
        }

        btnLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun iniciarSesion(usuario: String, password: String) {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}