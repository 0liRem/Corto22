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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.KeyboardType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        setContent {
            SignUpScreen(
                onSignUpClick = { name, email, password ->
                    signUpUser(name, email, password)
                }
            )
        }
    }

    private fun signUpUser(name: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Mostrar mensaje de éxito cuando la cuenta se crea en Firebase
                    Toast.makeText(this, "Registro exitoso en Firebase", Toast.LENGTH_SHORT).show()

                    val user = auth.currentUser
                    user?.let {
                        // Crear el mapa de datos para Firestore
                        val userData = mapOf(
                            "uid" to user.uid,
                            "name" to name,
                            "mail" to email  // Cambié de "email" a "mail" para que coincida con Firestore
                        )

                        // Guardar los datos en Firestore
                        firestore.collection("users")
                            .document(user.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Datos guardados correctamente en Firestore", Toast.LENGTH_SHORT).show()

                                // Redirigir a LoginActivity después del éxito en Firestore
                                startActivity(Intent(this, LoginActivity::class.java))
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Error guardando usuario en Firestore", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    // Mostrar error si falla el registro
                    Toast.makeText(this, "Error en el registro: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}

@Composable
fun SignUpScreen(onSignUpClick: (String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Crear Cuenta", fontSize = 32.sp, modifier = Modifier.padding(bottom = 16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre completo") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

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
                onSignUpClick(name, email, password)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = name.isNotBlank() && email.isNotBlank() && password.isNotBlank()
        ) {
            Text(text = "Registrarse")
        }
    }
}
