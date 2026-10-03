package com.mundo.keybowl.screen

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.app
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                MainAppNavigator()
            }
        }
    }
}

@Composable
fun MainAppNavigator() {
    val auth = Firebase.auth
    var currentUser by remember { mutableStateOf(auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = com.google.firebase.auth.FirebaseAuth.AuthStateListener { firebaseAuth ->
            currentUser = firebaseAuth.currentUser
        }
        auth.addAuthStateListener(listener)
        onDispose {
            auth.removeAuthStateListener(listener)
        }
    }

    if (currentUser == null) {
        AuthScreen()
    } else {
        RealtimeCounterScreen(onLogout = { auth.signOut() })
    }
}

@Composable
fun AuthScreen() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    val auth = Firebase.auth

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Connexion requise", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mot de passe") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (email.isNotBlank() && password.isNotBlank()) {
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnFailureListener {
                            Toast.makeText(context, "Erreur: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(context, "Remplissez tous les champs", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Se connecter")
        }
    }
}

@Composable
fun RealtimeCounterScreen(onLogout: () -> Unit) {
    var variableTest by remember { mutableStateOf(0L) }
    var isAuthorized by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var showCreateUserDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val database = Firebase.database
    val userId = Firebase.auth.currentUser?.uid ?: "inconnu"

    val counterRef = database.getReference("test/compteur")
    val userAuthRef = database.getReference("test").child(userId)

    DisposableEffect(Unit) {
        val counterListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val rawValue = snapshot.value
                variableTest = when (rawValue) {
                    is Long -> rawValue
                    is Int -> rawValue.toLong()
                    is Double -> rawValue.toLong()
                    is Map<*, *> -> {
                        val subVal = rawValue["valeur"] ?: rawValue["test"] ?: rawValue.values.firstOrNull()
                        when (subVal) {
                            is Long -> subVal
                            is Int -> subVal.toLong()
                            is Double -> subVal.toLong()
                            is String -> subVal.toLongOrNull() ?: 0L
                            else -> 0L
                        }
                    }
                    else -> 0L
                }
                isLoading = false
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        val authListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                isAuthorized = snapshot.getValue(Boolean::class.java) ?: false
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        counterRef.addValueEventListener(counterListener)
        userAuthRef.addValueEventListener(authListener)

        onDispose {
            counterRef.removeEventListener(counterListener)
            userAuthRef.removeEventListener(authListener)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Text(text = "Valeur Realtime DB : $variableTest", fontSize = 20.sp)
            Spacer(modifier = Modifier.height(24.dp))

            // Affichage de l'ID et du panneau Admin UNIQUEMENT si isAuthorized == true
            if (isAuthorized) {
                Text(
                    text = "ID Admin : $userId",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Bouton pour ouvrir la popup de création de compte
                Button(onClick = { showCreateUserDialog = true }) {
                    Text("Créer un nouvel utilisateur")
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            Button(onClick = {
                counterRef.setValue(variableTest + 1)
            }) {
                Text(text = "Incrémenter", fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))

            TextButton(onClick = onLogout) {
                Text("Se déconnecter", color = MaterialTheme.colorScheme.error)
            }
        }
    }

    // --- POPUP DE CRÉATION DE COMPTE (ADMIN) ---
    if (showCreateUserDialog) {
        var newEmail by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }
        var isAdminUser by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showCreateUserDialog = false },
            title = { Text("Créer un utilisateur") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newEmail,
                        onValueChange = { newEmail = it },
                        label = { Text("Email du nouvel utilisateur") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Mot de passe") },
                        visualTransformation = PasswordVisualTransformation()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = isAdminUser,
                            onCheckedChange = { isAdminUser = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Définir comme Admin (true)")
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newEmail.isNotBlank() && newPassword.isNotBlank()) {
                        // Création via une app secondaire pour ne pas déconnecter l'admin actuel
                        val secondaryAppName = "AdminSecondaryApp"
                        val secondaryApp = try {
                            FirebaseApp.getInstance(secondaryAppName)
                        } catch (e: Exception) {
                            FirebaseApp.initializeApp(context, Firebase.app.options, secondaryAppName)
                        }

                        val secondaryAuth = com.google.firebase.auth.FirebaseAuth.getInstance(secondaryApp)
                        secondaryAuth.createUserWithEmailAndPassword(newEmail, newPassword)
                            .addOnSuccessListener { authResult ->
                                val createdUid = authResult.user?.uid
                                if (createdUid != null) {
                                    // Enregistre le statut true/false dans le nœud "test"
                                    database.getReference("test").child(createdUid).setValue(isAdminUser)
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "Utilisateur créé avec succès !", Toast.LENGTH_SHORT).show()
                                            showCreateUserDialog = false
                                        }
                                }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Erreur : ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        Toast.makeText(context, "Remplissez tous les champs", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Créer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateUserDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}