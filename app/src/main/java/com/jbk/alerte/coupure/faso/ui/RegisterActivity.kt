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

        binding.btnChoisirPhoto.setOnClickListener { pickImage.launch("image/*") }

        binding.btnValiderInscription.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val passe = binding.etPassword.text.toString().trim()
            val nom = binding.etNom.text.toString().trim()
            val prenom = binding.etPrenom.text.toString().trim()

            // Vérifie que le spinner n'est pas vide
            val ville = binding.spinnerVilles.selectedItem?.toString() ?: "Ouagadougou"

            if (email.isNotEmpty() && validerChamps(email, passe, nom, prenom)) {
                creerCompte(email, passe, ville, nom, prenom)
            }
        }
    }

    private fun validerChamps(email: String, passe: String, nom: String, prenom: String): Boolean {
        if (nom.isEmpty() || prenom.isEmpty()) {
            Toast.makeText(this, "Nom et prénom requis", Toast.LENGTH_SHORT).show()
            return false
        }
        if (passe.length < 6) {
            Toast.makeText(this, "Mot de passe trop court (6 min)", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun creerCompte(email: String, passe: String, ville: String, nom: String, prenom: String) {
        val progressDialog = ProgressDialog(this).apply {
            setMessage("Création du compte...")
            setCancelable(false)
            show()
        }

        Firebase.auth.createUserWithEmailAndPassword(email, passe)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: return@addOnSuccessListener

                // Si l'image est sélectionnée, on upload, sinon profil direct
                if (photoUri != null) {
                    uploadPhoto(uid, email, ville, nom, prenom, progressDialog)
                } else {
                    enregistrerProfil(uid, email, ville, nom, prenom, null, progressDialog)
                }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Erreur : ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    private fun uploadPhoto(uid: String, email: String, ville: String, nom: String, prenom: String, pd: ProgressDialog) {
        val storageRef = Firebase.storage.reference.child("profiles/$uid.jpg")

        photoUri?.let { uri ->
            storageRef.putFile(uri)
                .continueWithTask { task ->
                    if (!task.isSuccessful) task.exception?.let { throw it }
                    storageRef.downloadUrl
                }
                .addOnSuccessListener { downloadUrl ->
                    enregistrerProfil(uid, email, ville, nom, prenom, downloadUrl.toString(), pd)
                }
                .addOnFailureListener {
                    pd.dismiss()
                    Toast.makeText(this, "Erreur upload image", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun enregistrerProfil(uid: String, email: String, ville: String, nom: String, prenom: String, url: String?, pd: ProgressDialog) {
        // Logique SONABEL = ADMIN
        val role = if (email.lowercase().endsWith("@sonabel.bf")) "ADMIN" else "USER"

        val profile = hashMapOf(
            "uid" to uid,
            "nom" to nom,
            "prenom" to prenom,
            "email" to email,
            "role" to role,
            "ville" to ville,
            "photoUrl" to (url ?: null),
            "estBloque" to false,
            "timestamp" to FieldValue.serverTimestamp()
        )

        Firebase.firestore.collection("users").document(uid).set(profile)
            .addOnSuccessListener {
                pd.dismiss()
                Toast.makeText(this, "Bienvenue $prenom !", Toast.LENGTH_SHORT).show()

                // REDIRECTION VERS L'ACTIVITÉ UNIQUE (AccueilActivity)
                val intent = Intent(this, AccueilActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                pd.dismiss()
                Toast.makeText(this, "Erreur base de données", Toast.LENGTH_SHORT).show()
            }
    }
}