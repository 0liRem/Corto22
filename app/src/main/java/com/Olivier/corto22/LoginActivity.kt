package com.Olivier.corto22

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        setContent {
            LoginScreen(
                onLoginClick = { email, password ->
                    signInUser(email, password)
                },
                onSignUpClick = {
                    // Navegar a la pantalla de registro si no tiene cuenta
                    startActivity(Intent(this, SignUpActivity::class.java))
                }
            )
        }
    }

    private fun signInUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Login exitoso, redirigir al ChatListActivity o a la pantalla de chats
                    Toast.makeText(this, "Login exitoso", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, ChatListActivity::class.java))
                    finish() // Terminar LoginActivity para que no pueda regresar
                } else {
                    // Mostrar mensaje de error si falla el login
                    Toast.makeText(this, "Error en el login: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}

@Composable
fun LoginScreen(onLoginClick: (String, String) -> Unit, onSignUpClick: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Iniciar Sesión", fontSize = 32.sp, modifier = Modifier.padding(bottom = 16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Email
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Password
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                onLoginClick(email, password)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = email.isNotBlank() && password.isNotBlank()
        ) {
            Text(text = "Iniciar sesión")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botón para ir a la pantalla de registro
        Button(
            onClick = {
                onSignUpClick()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Crear cuenta")
        }
    }
}
