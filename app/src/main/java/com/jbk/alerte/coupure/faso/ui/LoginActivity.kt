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

        // 1. Initialiser le Binding correctement
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. Vérifier si l'utilisateur est déjà connecté (Auto-Login)
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Si connecté, on vérifie le rôle et on redirige
            verifierRoleEtRediriger(currentUser.uid)
            // On ne continue pas le reste de l'initialisation du login
            return
        }

        // 3. Animation du logo
        binding.imgLogo.animate()
            .scaleX(1.1f)
            .scaleY(1.1f)
            .setDuration(1200)
            .start()

        // 4. Gestion des clics
        binding.btnConnexion.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val passe = binding.etPassword.text.toString().trim()

            if (email.isNotEmpty() && passe.isNotEmpty()) {
                seConnecter(email, passe)
            } else {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnCreerCompte.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun seConnecter(email: String, passe: String) {
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
                    val estBloque = document.getBoolean("estBloque") ?: false

                    if (estBloque) {
                        Toast.makeText(this, "Ce compte est suspendu.", Toast.LENGTH_LONG).show()
                        auth.signOut()
                    } else {
                        // TOUT LE MONDE va vers AccueilActivity
                        val intent = Intent(this, AccueilActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                } else {
                    startActivity(Intent(this, AccueilActivity::class.java))
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erreur réseau : ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}