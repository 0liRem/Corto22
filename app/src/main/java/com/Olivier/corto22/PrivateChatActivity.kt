package com.Olivier.corto22
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.Alignment

class PrivateChatActivity : ComponentActivity() {

    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Obtener el UID y nombre del usuario seleccionado
        val userId = intent.getStringExtra("USER_ID")
        val userName = intent.getStringExtra("USER_NAME")

        setContent {
            PrivateChatScreen(userName ?: "Chat Privado")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivateChatScreen(userName: String) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(userName) })
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Aquí iría la lista de mensajes
                Text(
                    text = "Aquí se mostrarán los mensajes encriptados",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 20.sp
                )
                // Entrada de texto para escribir nuevos mensajes
                Spacer(modifier = Modifier.weight(1f))
                TextField(
                    value = "",
                    onValueChange = {},
                    placeholder = { Text("Escribe un mensaje") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
                Button(
                    onClick = { /* Lógica para enviar el mensaje */ },
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(8.dp)
                ) {
                    Text("Enviar")
                }
            }
        }
    )
}

