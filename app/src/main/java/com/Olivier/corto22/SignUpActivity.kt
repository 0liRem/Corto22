package com.Olivier.corto22

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
                    // Usuario creado correctamente
                    val user = auth.currentUser
                    user?.let {
                        // Guardar los datos del usuario en Firestore
                        val userData = mapOf(
                            "uid" to user.uid,
                            "name" to name,
                            "email" to email
                        )

                        firestore.collection("users")
                            .document(user.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()

                                // Redirigir a LoginActivity
                                val intent = Intent(this, ChatListActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                                finish()  // Cerrar SignUpActivity y volver a LoginActivity
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error guardando usuario en Firestore", Toast.LENGTH_SHORT).show()

                            }
                    }
                } else {
                    // Mostrar error si falla el registro
                    Toast.makeText(this, "Error en el registro: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, ChatListActivity::class.java)
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
