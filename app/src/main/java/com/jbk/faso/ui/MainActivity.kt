package com.jbk.faso.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.jbk.alerte.coupure.faso.R
import com.jbk.alerte.coupure.faso.databinding.ActivityMainBinding
import com.jbk.alerte.coupure.faso.models.Signalement
import com.jbk.alerte.coupure.faso.models.TypeSignalement
import com.jbk.alerte.coupure.faso.ui.SignalementAdapter
import com.jbk.faso.models.Communique

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: SignalementAdapter

    private val user = Firebase.auth.currentUser
    private val listeSignalements = mutableListOf<Signalement>()

    private lateinit var communiqueAdapter: CommuniqueAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Initialisations
        configurerPermissions()
        configurerRecyclerView()
        configurerNavigation()
        ecouterLesAlertes()

        // 2. Firebase Messaging
        FirebaseMessaging.getInstance().subscribeToTopic("toutes_les_alertes")

        // 3. Clics
        binding.fabSignaler.setOnClickListener { afficherDialogueSignalement() }

        // CORRECTION : Lier le bouton de publication officielle
        binding.btnPublierOfficiel.setOnClickListener {
            afficherDialogueCommuniqueOfficiel()
        }

        configurerRecherche()

        communiqueAdapter = CommuniqueAdapter(emptyList())

        // Vérification du rôle Admin au démarrage
        verifierStatutAdmin()
    }

    private fun verifierStatutAdmin() {
        val user = Firebase.auth.currentUser
        // On affiche le bouton uniquement si l'email correspond à l'admin SONABEL
        if (user != null && user.email == "admin@sonabel.bf") {
            binding.btnPublierOfficiel.visibility = View.VISIBLE
        } else {
            binding.btnPublierOfficiel.visibility = View.GONE
        }
    }

    private fun configurerNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            // 1. Reset visuel par défaut
            binding.rvSignalements.visibility = View.GONE
            binding.pieChart.visibility = View.GONE
            binding.searchView.visibility = View.GONE
            binding.fabSignaler.hide()
            binding.btnPublierOfficiel.visibility = View.GONE

            when (item.itemId) {
                R.id.nav_alertes -> {
                    binding.rvSignalements.visibility = View.VISIBLE
                    binding.searchView.visibility = View.VISIBLE
                    // On remet l'adapter des signalements citoyens
                    binding.rvSignalements.adapter = adapter
                    binding.fabSignaler.show()
                    true
                }
                R.id.nav_stats -> {
                    binding.pieChart.visibility = View.VISIBLE
                    mettreAJourGraphique()
                    true
                }
                R.id.nav_sonabel -> {
                    binding.rvSignalements.visibility = View.VISIBLE

                    // On active l'écoute des communiqués (l'adapter est changé à l'intérieur)
                    ecouterLesCommuniquesOfficiels()

                    // Vérification Admin pour afficher le bouton de publication
                    val currentUser = Firebase.auth.currentUser
                    if (currentUser != null && currentUser.email == "admin@sonabel.bf") {
                        binding.btnPublierOfficiel.visibility = View.VISIBLE
                    }
                    true
                }
                R.id.nav_conseils -> {
                    // Ici tu pourras ajouter un layout de conseils plus tard
                    Toast.makeText(this, "Conseils d'économie d'énergie", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    private fun afficherDialogueSignalement() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_ajouter_signalement, null)

        val btnValider = view.findViewById<Button>(R.id.btnValider)
        val etZone = view.findViewById<EditText>(R.id.etZone)
        val rgType = view.findViewById<RadioGroup>(R.id.rgType)

        btnValider.setOnClickListener {
            val quartier = etZone.text.toString().trim()
            if (quartier.isNotEmpty()) {
                val type = if (rgType.checkedRadioButtonId == R.id.rbCoupure)
                    TypeSignalement.COUPURE else TypeSignalement.RETOUR

                val nouveau = Signalement(0, "", quartier, type, System.currentTimeMillis())
                envoyerVersFirebase(nouveau)
                dialog.dismiss()
            } else {
                etZone.error = "Quartier requis"
            }
        }
        dialog.setContentView(view)
        dialog.show()
    }

    private fun afficherDialogueCommuniqueOfficiel() {
        val dialog = BottomSheetDialog(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_communique_sonabel, null)

        val btnPublier = dialogView.findViewById<Button>(R.id.btnPublierOfficiel)
        val etTitre = dialogView.findViewById<EditText>(R.id.etTitreCommunique)
        val etMessage = dialogView.findViewById<EditText>(R.id.etMessageCommunique)

        btnPublier.setOnClickListener {
            val titre = etTitre.text.toString()
            val msg = etMessage.text.toString()

            if (titre.isNotEmpty() && msg.isNotEmpty()) {
                val data = hashMapOf(
                    "titre" to titre,
                    "message" to msg,
                    "timestamp" to System.currentTimeMillis(),
                    "auteur" to "SONABEL"
                )
                Firebase.firestore.collection("communiques_officiels").add(data)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Communiqué publié avec succès !", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
            }
        }
        dialog.setContentView(dialogView)
        dialog.show()
    }

    // --- AUTRES MÉTHODES ---

    private fun envoyerVersFirebase(signalement: Signalement) {
        val data = hashMapOf(
            "zone" to signalement.zone,
            "type" to signalement.type.name,
            "timestamp" to signalement.timestamp
        )
        Firebase.firestore.collection("alertes").add(data)
    }

    private fun mettreAJourGraphique() {
        if (listeSignalements.isEmpty()) return
        val stats = listeSignalements.groupBy { it.zone }.mapValues { it.value.size }
        val entries = stats.map { PieEntry(it.value.toFloat(), it.key) }
        val dataSet = PieDataSet(entries, "").apply {
            colors = ColorTemplate.COLORFUL_COLORS.toList()
            valueTextColor = Color.WHITE
            valueTextSize = 12f
        }
        binding.pieChart.data = PieData(dataSet)
        binding.pieChart.invalidate()
    }

    private fun ecouterLesAlertes() {
        Firebase.firestore.collection("alertes")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val nouvelles = snapshot.map { doc ->
                        Signalement(0, doc.id, doc.getString("zone") ?: "",
                            TypeSignalement.valueOf(doc.getString("type") ?: "COUPURE"),
                            doc.getLong("timestamp") ?: 0L)
                    }
                    listeSignalements.clear()
                    listeSignalements.addAll(nouvelles)
                    adapter.soumettreListe(listeSignalements)
                }
            }
    }

    private fun configurerRecyclerView() {
        adapter = SignalementAdapter()
        binding.rvSignalements.layoutManager = LinearLayoutManager(this)
        binding.rvSignalements.adapter = adapter
    }

    private fun configurerRecherche() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                val filtre = listeSignalements.filter { it.zone.contains(newText ?: "", true) }
                adapter.soumettreListe(filtre)
                return true
            }
        })
    }

    private fun configurerPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }

    private fun ecouterLesCommuniquesOfficiels() {
        // On change l'adapter du RecyclerView pour celui des communiqués
        binding.rvSignalements.adapter = communiqueAdapter

        binding.rvSignalements.layoutManager = LinearLayoutManager(this)

        Firebase.firestore.collection("communiques_officiels")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val liste = snapshot.map { doc ->
                        Communique(
                            doc.id,
                            doc.getString("titre") ?: "",
                            doc.getString("message") ?: "",
                            doc.getLong("timestamp") ?: 0L
                        )
                    }
                    communiqueAdapter.mettreAJour(liste)
                }
            }
    }
}