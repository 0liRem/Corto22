package com.Olivier.corto22

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// Data class para un Usuario
data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = ""
)

class ChatListActivity : ComponentActivity() {

    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Cargar usuarios desde Firestore y mostrarlos
            val users = remember { mutableStateListOf<User>() }
            LaunchedEffect(Unit) {
                val usersList = getUsersFromFirestore()
                users.addAll(usersList)
            }
            UserListScreen(users) { selectedUser ->
                // Cuando se hace clic en un usuario, redirigir al chat privado
                val intent = Intent(this, PrivateChatActivity::class.java)
                intent.putExtra("USER_ID", selectedUser.uid)
                intent.putExtra("USER_NAME", selectedUser.name)
                startActivity(intent)
            }
        }
    }

    // Función para obtener los usuarios de Firestore
    private suspend fun getUsersFromFirestore(): List<User> {
        return try {
            val result = firestore.collection("users").get().await()
            result.documents.map { doc ->
                User(
                    uid = doc.id,
                    name = doc.getString("name") ?: "",
                    email = doc.getString("email") ?: ""
                )
            }
        } catch (e: Exception) {
            emptyList() // En caso de error
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserListScreen(users: List<User>, onUserClick: (User) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Lista de Chats") })
        },
        content = { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(users) { user ->
                    UserListItem(user, onUserClick)
                }
            }
        }
    )
}

@Composable
fun UserListItem(user: User, onUserClick: (User) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onUserClick(user) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = user.name)
            Text(text = user.email)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UserListScreenPreview() {
    val dummyUsers = listOf(
        User(uid = "1", name = "Juan Pérez", email = "juan@example.com"),
        User(uid = "2", name = "Maria Gómez", email = "maria@example.com")
    )
    UserListScreen(dummyUsers) {}
}
