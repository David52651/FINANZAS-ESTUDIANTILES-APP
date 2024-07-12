package com.arcjas.studentapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.arcjas.studentapp.MainActivity
import com.arcjas.studentapp.R
import com.arcjas.studentapp.core.config.VolleyListener
import com.arcjas.studentapp.core.model.ResponseCore
import com.arcjas.studentapp.core.model.User
import com.arcjas.studentapp.core.service.ApiManager
import com.arcjas.studentapp.core.service.AuthRequest
import com.arcjas.studentapp.core.service.UserRequest


class LoginActivity : AppCompatActivity() {

    private lateinit var editTextUsuario: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var btnRegister: Button
    private lateinit var apiManager: ApiManager

    private lateinit var authRequest: AuthRequest
    private lateinit var userRequest: UserRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        apiManager = ApiManager.getInstance(applicationContext)
        if (isLoggedIn()) {
            navigateToLogin()
            return
        }
        editTextUsuario = findViewById(R.id.editTextTextd)
        editTextPassword = findViewById(R.id.editTextTextPassword)
        buttonLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)

        authRequest = AuthRequest(this)
        userRequest = UserRequest(this, ApiManager.getInstance(applicationContext))

        btnRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }

        buttonLogin.setOnClickListener {
            val username = editTextUsuario.text.toString()
            val password = editTextPassword.text.toString()

            if (username.isBlank() || password.isBlank()) {
                Toast.makeText(this@LoginActivity, "Por favor ingresa usuario y contraseña", Toast.LENGTH_SHORT).show()
            } else {
                authRequest.loginAuth(username, password, object : VolleyListener<User> {
                    override fun onSuccess(response: ResponseCore<User>) {
                        if (response.success && response.data != null) {
                            val token = response.data.token
                            ApiManager.getInstance(applicationContext).saveToken(token.toString())
                            ApiManager.getInstance(applicationContext).saveCurrentUser(response.data)
                            Toast.makeText(this@LoginActivity, "Login exitoso", Toast.LENGTH_SHORT).show()
                            getUserInfo()
                        } else {
                            Toast.makeText(
                                this@LoginActivity,
                                response.message ?: "Error de autenticación",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onError(error: String) {
                        Toast.makeText(
                            this@LoginActivity,
                            error,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            }
        }
    }

    private fun isLoggedIn(): Boolean {
        val token = apiManager.getToken()
        println("Token : $token")
        return !token.isNullOrEmpty()
    }

    private fun navigateToLogin() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun getUserInfo() {
        userRequest.getUserInfo(object : UserRequest.UserInfoListener {
            override fun onUserInfoSuccess(response: ResponseCore<User>) {
                if (response.success && response.data != null) {
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        response.message ?: "Error al obtener información del usuario",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            override fun onUserInfoError(error: String) {
                Toast.makeText(
                    this@LoginActivity,
                    "Error al obtener información del usuario: $error",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}