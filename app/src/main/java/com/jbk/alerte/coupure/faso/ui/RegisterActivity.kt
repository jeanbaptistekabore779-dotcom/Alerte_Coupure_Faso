package com.jbk.alerte.coupure.faso.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.jbk.alerte.coupure.faso.databinding.ActivityRegisterBinding


class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private var photoUri: Uri? = null // Variable indispensable

    // 1. Déclencheur pour choisir une image
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            binding.ivProfil.setImageURI(uri)
            photoUri = uri
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Tout le code d'écoute doit être ICI dans le onCreate.
        binding.btnChoisirPhoto.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.btnValiderInscription.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val passe = binding.etPassword.text.toString().trim()
            val nom = binding.etNom.text.toString().trim()
            val prenom = binding.etPrenom.text.toString().trim()
            val ville = binding.spinnerVilles.selectedItem.toString()

            if (email.isNotEmpty() && passe.isNotEmpty() && nom.isNotEmpty() && prenom.isNotEmpty()) {
                if (passe.length >= 6) {
                    creerCompte(email, passe, ville, nom, prenom)
                } else {
                    Toast.makeText(this, "Mot de passe : 6 caractères min", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun creerCompte(email: String, passe: String, ville: String, nom: String, prenom: String) {
        Firebase.auth.createUserWithEmailAndPassword(email, passe)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: return@addOnSuccessListener

                if (photoUri != null) {
                    uploadPhoto(uid, email, ville, nom, prenom)
                } else {
                    enregistrerProfil(uid, email, ville, nom, prenom, null)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erreur Auth : ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadPhoto(uid: String, email: String, ville: String, nom: String, prenom: String) {
        val storageRef = Firebase.storage.reference.child("profiles/$uid.jpg")
        photoUri?.let { uri ->
            storageRef.putFile(uri).addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    enregistrerProfil(uid, email, ville, nom, prenom, downloadUrl.toString())
                }
            }
        }
    }

    private fun enregistrerProfil(uid: String, email: String, ville: String, nom: String, prenom: String, url: String?) {
        // Détermination automatique du rôle pour la SONABEL
        val role = if (email.endsWith("@sonabel.bf")) "ADMIN" else "USER"

        val profile = hashMapOf(
            "uid" to uid,
            "nom" to nom,
            "prenom" to prenom,
            "email" to email,
            "role" to role,
            "ville" to ville,
            "photoUrl" to url,
            "estBloque" to false, // Important pour ton système de gestion
            "timestamp" to FieldValue.serverTimestamp() // Utilise le temps serveur
        )

        // Utilisation directe de Firebase.firestore pour éviter l'erreur "db"
        Firebase.firestore.collection("users").document(uid).set(profile)
            .addOnSuccessListener {
                Toast.makeText(this, "Compte créé avec succès !", Toast.LENGTH_SHORT).show()
                redirigerSelonRole(role)
            }
            .addOnFailureListener { e: Exception ->
                Toast.makeText(this, "Erreur Firestore : ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun redirigerSelonRole(role: String) {
        val destination = if (role == "ADMIN") {
            AdminDashboardActivity::class.java
        } else {
            // Assure-toi que cette classe existe bien dans ton package ui
            UserDashboardActivity::class.java
        }
        startActivity(Intent(this, destination))
        finishAffinity()
    }

}