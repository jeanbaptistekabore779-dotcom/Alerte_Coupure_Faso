package com.jbk.alerte.coupure.faso.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.jbk.alerte.coupure.faso.adapters.SignalementAdapter
import com.jbk.alerte.coupure.faso.databinding.ActivityAccueilBinding
import com.jbk.alerte.coupure.faso.models.Signalement
import com.jbk.alerte.coupure.faso.models.TypeSignalement

class AccueilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAccueilBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = Firebase.auth
    private val listeSignalements = mutableListOf<Signalement>()
    private lateinit var adapter: SignalementAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. TOUJOURS Initialiser le ViewBinding en premier
        binding = ActivityAccueilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. Vérifier l'utilisateur et afficher le nom
        configurerHeader()

        // 3. Configuration du RecyclerView
        adapter = SignalementAdapter(listeSignalements)
        binding.rvSignalements.layoutManager = LinearLayoutManager(this)
        binding.rvSignalements.adapter = adapter

        // 4. Bouton Ajouter
        binding.fabSignaler.setOnClickListener {
            startActivity(Intent(this, AddAlerteActivity::class.java))
        }

        // 5. Écouter les données
        ecouterSignalementsEnTempsReel()
    }

    private fun configurerHeader() {
        val user = auth.currentUser
        if (user != null) {
            val nomAafficher = if (!user.displayName.isNullOrEmpty()) {
                user.displayName
            } else {
                user.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
            }
            binding.tvSalut.text = "Salut $nomAafficher"
        } else {
            // Si pas d'utilisateur, on déconnecte pour sécurité
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun ecouterSignalementsEnTempsReel() {
        db.collection("signalements")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Toast.makeText(this, "Erreur de chargement", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    listeSignalements.clear()
                    for (document in snapshots.documents) {
                        try {
                            val typeString = document.getString("type") ?: "COUPURE"
                            val typeEnum = TypeSignalement.valueOf(typeString)

                            val signalement = Signalement(
                                idLocal = 0,
                                idFirestore = document.id,
                                zone = document.getString("zone") ?: "Zone inconnue",
                                type = typeEnum,
                                timestamp = document.getLong("timestamp") ?: System.currentTimeMillis()
                            )
                            listeSignalements.add(signalement)
                        } catch (e: Exception) {
                            // Évite de faire planter l'app si un document est mal formaté
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }
}