package com.jbk.alerte.coupure.faso.ui

import android.app.ProgressDialog
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
    private var photoUri: Uri? = null

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
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Création du compte en cours...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        Firebase.auth.createUserWithEmailAndPassword(email, passe)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: return@addOnSuccessListener

                if (photoUri != null) {
                    uploadPhoto(uid, email, ville, nom, prenom, progressDialog)
                } else {
                    enregistrerProfil(uid, email, ville, nom, prenom, null, progressDialog)
                }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss() // ✅ Fermer si erreur
                Toast.makeText(this, "Erreur Auth : ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadPhoto(
        uid: String,
        email: String,
        ville: String,
        nom: String,
        prenom: String,
        progressDialog: ProgressDialog
    ) {
        val storageRef = Firebase.storage.reference.child("profiles/$uid.jpg")
        photoUri?.let { uri ->
            storageRef.putFile(uri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        enregistrerProfil(uid, email, ville, nom, prenom, downloadUrl.toString(), progressDialog)
                    }
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss() // ✅ Fermer si l'image ne monte pas
                    Toast.makeText(this, "Erreur Image : ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun enregistrerProfil(
        uid: String,
        email: String,
        ville: String,
        nom: String,
        prenom: String,
        url: String?,
        progressDialog: ProgressDialog
    ) {
        val role = if (email.endsWith("@sonabel.bf")) "ADMIN" else "USER"
        val finalPhotoUrl = url ?: ""

        val profile = hashMapOf(
            "uid" to uid,
            "nom" to nom,
            "prenom" to prenom,
            "email" to email,
            "role" to role,
            "ville" to ville,
            "photoUrl" to finalPhotoUrl,
            "estBloque" to false,
            "timestamp" to FieldValue.serverTimestamp()
        )

        Firebase.firestore.collection("users").document(uid).set(profile)
            .addOnSuccessListener {
                progressDialog.dismiss() // ✅ Fermer enfin le dialogue !
                Toast.makeText(this, "Compte créé avec succès !", Toast.LENGTH_SHORT).show()
                redirigerSelonRole(role)
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss() // ✅ Fermer si Firestore échoue
                Toast.makeText(this, "Erreur Firestore : ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun redirigerSelonRole(role: String) {
        val destination = if (role == "ADMIN") {
            AdminDashboardActivity::class.java
        } else {
            // Remplace par ta classe d'accueil utilisateur (ex: AccueilActivity)
            UserDashboardActivity::class.java
        }
        startActivity(Intent(this, destination))
        finishAffinity()
    }
}