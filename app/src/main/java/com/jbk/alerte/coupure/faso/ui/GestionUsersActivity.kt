package com.jbk.alerte.coupure.faso.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.jbk.alerte.coupure.faso.R
import com.jbk.alerte.coupure.faso.adapters.UserAdapter
import com.jbk.alerte.coupure.faso.models.User

class GestionUsersActivity : AppCompatActivity() {

    private lateinit var rvUsers: RecyclerView
    private val db = Firebase.firestore
    private val listeUsers = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_gestion_users)

        // Gestion des marges système
        val mainView = findViewById<View>(R.id.main)
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        rvUsers = findViewById(R.id.rvUsers)
        rvUsers.layoutManager = LinearLayoutManager(this)

        chargerUtilisateurs()
    }

    private fun chargerUtilisateurs() {
        db.collection("users")
            .whereEqualTo("role", "USER")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(this, "Erreur : ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                listeUsers.clear()
                snapshots?.forEach { doc ->
                    val user = doc.toObject(User::class.java)
                    user.uid = doc.id
                    listeUsers.add(user)
                }

                // Utilisation de l'adapter avec les 3 paramètres requis
                val adapter = UserAdapter(
                    users = listeUsers,
                    onBlockClick = { user -> bloquerUser(user) },
                    onDeleteClick = { user ->
                        Toast.makeText(this, "Action sur ${user.nom}", Toast.LENGTH_SHORT).show()
                    }
                )
                rvUsers.adapter = adapter
            }
    }

    private fun bloquerUser(user: User) {
        val nouveauStatut = !user.estBloque

        db.collection("users").document(user.uid)
            .update("estBloque", nouveauStatut)
            .addOnSuccessListener {
                val message = if (nouveauStatut) "Utilisateur bloqué" else "Utilisateur débloqué"
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erreur : ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}