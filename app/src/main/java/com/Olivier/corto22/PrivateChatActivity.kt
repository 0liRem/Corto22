package com.Olivier.corto22

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.TextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api


class PrivateChatActivity : ComponentActivity() {
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        setContent {
            var chatIdSeleccionado by remember { mutableStateOf<String?>(null) }
            var usuarioSeleccionado by remember { mutableStateOf<Map<String, Any>?>(null) }
            var mostrarPinDialog by remember { mutableStateOf(false) }
            var pinIngresado by remember { mutableStateOf("") }
            var mensajes by remember { mutableStateOf<List<String>>(emptyList()) }
            var nuevoMensaje by remember { mutableStateOf("") }

            if (chatIdSeleccionado != null && !mostrarPinDialog) {
                // Si ya se ha seleccionado un chat y no se está mostrando el diálogo del PIN, cargar mensajes
                cargarMensajes(chatIdSeleccionado!!) { mensajesCargados ->
                    mensajes = mensajesCargados
                }

                PantallaChat(
                    mensajes = mensajes,
                    nuevoMensaje = nuevoMensaje,
                    onMensajeChange = { nuevoMensaje = it },
                    onEnviarMensaje = {
                        enviarMensaje(chatIdSeleccionado!!, nuevoMensaje)
                        nuevoMensaje = ""
                    }
                )
            } else if (mostrarPinDialog) {
                PedirPinDialog(
                    pinIngresado = pinIngresado,
                    onPinChange = { pinIngresado = it },
                    onPinSubmit = {
                        verificarPin(chatIdSeleccionado!!, pinIngresado) { correcto ->
                            if (correcto) {
                                mostrarPinDialog = false
                                Toast.makeText(this, "PIN correcto", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this, "PIN incorrecto", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            } else {
                ListaUsuariosScreen(
                    onUsuarioSeleccionado = { chatId, usuario ->
                        verificarChat(chatId, usuario) { existeChat ->
                            if (existeChat) {
                                // Si el chat ya existe, pedir el PIN
                                chatIdSeleccionado = chatId
                                usuarioSeleccionado = usuario
                                mostrarPinDialog = true
                            } else {
                                // Si el chat no existe, crear chat y pedir PIN
                                crearChat(chatId, usuario) {
                                    chatIdSeleccionado = chatId
                                    usuarioSeleccionado = usuario
                                    mostrarPinDialog = true
                                }
                            }
                        }
                    }
                )
            }
        }
    }

    // Función para cargar los mensajes desde Firestore
    private fun cargarMensajes(chatId: String, onMensajesCargados: (List<String>) -> Unit) {
        firestore.collection("chats").document(chatId)
            .get()
            .addOnSuccessListener { document ->
                val mensajesList = document.get("messages") as? List<String> ?: emptyList()
                onMensajesCargados(mensajesList)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar mensajes", Toast.LENGTH_SHORT).show()
            }
    }

    // Función para enviar un nuevo mensaje
    private fun enviarMensaje(chatId: String, nuevoMensaje: String) {
        firestore.collection("chats").document(chatId)
            .update("messages", FieldValue.arrayUnion(nuevoMensaje))
            .addOnSuccessListener {
                Toast.makeText(this, "Mensaje enviado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al enviar mensaje", Toast.LENGTH_SHORT).show()
            }
    }

    // Verifica si el chat ya existe en Firestore
    private fun verificarChat(chatId: String, usuario: Map<String, Any>, onResultado: (Boolean) -> Unit) {
        firestore.collection("chats").document(chatId).get()
            .addOnSuccessListener { document ->
                onResultado(document.exists())
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al verificar chat", Toast.LENGTH_SHORT).show()
                onResultado(false)
            }
    }

    // Verificar PIN para acceder al chat
    private fun verificarPin(chatId: String, pinIngresado: String, onResultado: (Boolean) -> Unit) {
        firestore.collection("chats").document(chatId).get()
            .addOnSuccessListener { document ->
                val pinGuardado = document.getString("pin")
                onResultado(pinGuardado == pinIngresado)
            }
            .addOnFailureListener {
                onResultado(false)
            }
    }

    // Crear un nuevo chat en Firestore
    private fun crearChat(chatId: String, usuario: Map<String, Any>, onChatCreado: () -> Unit) {
        val nuevoChat = mapOf(
            "user1" to auth.currentUser!!.uid,
            "user2" to usuario["uid"].toString(),
            "messages" to listOf<String>(),
            "pin" to "" // Esto se llenará cuando el usuario ingrese un PIN
        )

        firestore.collection("chats").document(chatId)
            .set(nuevoChat)
            .addOnSuccessListener {
                Toast.makeText(this, "Chat creado", Toast.LENGTH_SHORT).show()
                onChatCreado()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al crear chat", Toast.LENGTH_SHORT).show()
            }
    }

    // Composable para la pantalla del chat estilo WhatsApp
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun PantallaChat(mensajes: List<String>, nuevoMensaje: String, onMensajeChange: (String) -> Unit, onEnviarMensaje: () -> Unit) {
        Scaffold(
            topBar = { TopAppBar(title = { Text("Chat") }) },
            content = { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        items(mensajes) { mensaje ->
                            Text(text = mensaje, modifier = Modifier.padding(8.dp))
                        }
                    }
                    TextField(
                        value = nuevoMensaje,
                        onValueChange = { onMensajeChange(it) },
                        placeholder = { Text("Escribe un mensaje...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                    Button(
                        onClick = { onEnviarMensaje() },
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

    // Composable para la lista de usuarios
    @Composable
    fun ListaUsuariosScreen(onUsuarioSeleccionado: (String, Map<String, Any>) -> Unit) {
        var usuarios by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
        var loading by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            firestore.collection("users").get()
                .addOnSuccessListener { querySnapshot ->
                    usuarios = querySnapshot.documents.mapNotNull { it.data }
                    loading = false
                }
                .addOnFailureListener {
                    Toast.makeText(this@PrivateChatActivity, "Error al obtener usuarios", Toast.LENGTH_SHORT).show()
                    loading = false
                }
        }

        if (loading) {
            // Muestra indicador de carga
            Text("Cargando usuarios...", modifier = Modifier.padding(16.dp), fontSize = 18.sp)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp)
            ) {
                items(usuarios) { usuario ->
                    val chatId = generarChatId(auth.currentUser!!.uid, usuario["uid"].toString())
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable {
                                onUsuarioSeleccionado(chatId, usuario)
                            }
                    ) {
                        Text(text = usuario["name"].toString(), fontSize = 20.sp)
                    }
                }
            }
        }
    }

    // Composable para pedir el PIN
    @Composable
    fun PedirPinDialog(pinIngresado: String, onPinChange: (String) -> Unit, onPinSubmit: () -> Unit) {
        AlertDialog(
            onDismissRequest = { /* No permitir cerrar sin ingresar el PIN */ },
            title = { Text(text = "Ingresa el PIN del chat") },
            text = {
                Column {
                    Text("Por favor ingresa el PIN para acceder a este chat:")
                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        value = pinIngresado,
                        onValueChange = { onPinChange(it) },
                        placeholder = { Text("Ingrese el PIN") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = { onPinSubmit() }) {
                    Text("Verificar PIN")
                }
            },
            dismissButton = {
                Button(onClick = { /* No hacer nada */ }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Función para generar el ID del chat basado en los dos usuarios
    private fun generarChatId(user1Id: String, user2Id: String): String {
        return if (user1Id < user2Id) {
            "$user1Id-$user2Id"
        } else {
            "$user2Id-$user1Id"
        }
    }
}
