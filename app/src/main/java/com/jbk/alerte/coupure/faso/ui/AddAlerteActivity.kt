package com.jbk.alerte.coupure.faso.ui

import android.R
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.jbk.alerte.coupure.faso.databinding.ActivityAddAlerteBinding

class AddAlerteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddAlerteBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddAlerteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuration du menu déroulant (Exposed Dropdown Menu)
        val typesCoupure = arrayOf("Coupure d'électricité", "Coupure d'eau", "Baisse de tension", "Maintenance")
        val adapter = ArrayAdapter(this, R.layout.simple_list_item_1, typesCoupure)
        binding.autoCompleteType.setAdapter(adapter)

        // Bouton Publier
        binding.btnPublier.setOnClickListener {
            val quartier = binding.etQuartier.text.toString().trim()
            val type = binding.autoCompleteType.text.toString()
            val description = binding.etDescription.text.toString().trim()

            if (quartier.isNotEmpty()) {
                enregistrerAlerte(quartier, type, description)
            } else {
                binding.etQuartier.error = "Veuillez saisir un quartier"
            }
        }

        // Bouton Annuler
        binding.btnAnnuler.setOnClickListener {
            finish()
        }
    }

    private fun enregistrerAlerte(quartier: String, type: String, description: String) {
        val currentUser = Firebase.auth.currentUser
        val nouvelleAlerte = hashMapOf(
            "quartier" to quartier,
            "type" to type,
            "description" to description,
            "timestamp" to Timestamp.now(),
            "status" to "EN COURS",
            "auteurEmail" to (currentUser?.email ?: "Anonyme")
        )

        db.collection("alertes").add(nouvelleAlerte)
            .addOnSuccessListener {
                Toast.makeText(this, "Alerte publiée !", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erreur : ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}