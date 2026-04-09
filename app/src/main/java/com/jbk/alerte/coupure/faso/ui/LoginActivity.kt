package com.jbk.alerte.coupure.faso.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.jbk.alerte.coupure.faso.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Effet de zoom fluide au lancement
        binding.imgLogo.animate()
            .scaleX(1.1f) // Un zoom de 10% suffit souvent pour rester élégant
            .scaleY(1.1f)
            .setDuration(1200)
            .start()

        // 1. Connexion
        binding.btnConnexion.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val passe = binding.etPassword.text.toString().trim()

            if (email.isNotEmpty() && passe.isNotEmpty()) {
                seConnecter(email, passe)
            } else {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
            }
        }

        // 2. Inscription
        binding.btnCreerCompte.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun seConnecter(email: String, passe: String) {
        // Ajout d'un petit message de patience
        Toast.makeText(this, "Vérification en cours...", Toast.LENGTH_SHORT).show()

        auth.signInWithEmailAndPassword(email, passe)
            .addOnSuccessListener { authResult ->
                authResult.user?.let { verifierRoleEtRediriger(it.uid) }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Échec : ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun verifierRoleEtRediriger(uid: String) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val role = document.getString("role") ?: "USER"
                    val estBloque = document.getBoolean("estBloque") ?: false

                    if (estBloque) {
                        Toast.makeText(this, "Ce compte est suspendu.", Toast.LENGTH_LONG).show()
                        auth.signOut()
                    } else {
                        val destination = if (role == "ADMIN") AdminDashboardActivity::class.java
                        else UserDashboardActivity::class.java

                        startActivity(Intent(this, destination))
                        finish()
                    }
                } else {
                    Toast.makeText(this, "Profil utilisateur introuvable.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erreur réseau : ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}