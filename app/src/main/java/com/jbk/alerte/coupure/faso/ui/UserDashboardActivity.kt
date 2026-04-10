package com.jbk.alerte.coupure.faso.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.appcompat.widget.SearchView
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.jbk.alerte.coupure.faso.R
import com.jbk.alerte.coupure.faso.adapters.AlerteAdapter
import com.jbk.alerte.coupure.faso.models.Alerte
import java.util.Locale

class UserDashboardActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var adapter: AlerteAdapter
    private val listeAlertes = mutableListOf<Alerte>()
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_dashboard)

        // Gestion des marges système (EdgeToEdge)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialisation des vues
        searchView = findViewById(R.id.searchViewDashboard)
        recyclerView = findViewById(R.id.rvAlertes)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialisation de l'adapter
        adapter = AlerteAdapter(
            listeTotale = listeAlertes,
            isAdmin = false,
            onItemClick = null,
            onItemLongClick = null
        )
        recyclerView.adapter = adapter

        // Recherche
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return true
            }
        })

        // Bouton pour ouvrir la BottomSheet
        findViewById<FloatingActionButton>(R.id.btnOpenDialog).setOnClickListener {
            showSignalementDialog()
        }

        chargerAlertes()
    }

    private fun chargerAlertes() {
        db.collection("alertes")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener

                listeAlertes.clear()
                snapshots?.forEach { doc ->
                    val alerte = doc.toObject(Alerte::class.java)
                    // ✅ CORRECTION 1 : On récupère l'ID du document Firestore !
                    alerte?.id = doc.id
                    if (alerte != null) {
                        listeAlertes.add(alerte)
                    }
                }
                // Utiliser updateData est mieux pour le filtre de recherche
                adapter.updateData(listeAlertes)
            }
    }

    private fun showSignalementDialog() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_ajouter_signalement, null)

        val btnValider = view.findViewById<Button>(R.id.btnValider)
        val etZone = view.findViewById<EditText>(R.id.etZone)
        val rgType = view.findViewById<RadioGroup>(R.id.rgType)
        val btnGps = view.findViewById<ImageButton>(R.id.btnGps)

        // LOGIQUE GPS
        btnGps?.setOnClickListener {
            recupererLocalisation(etZone)
        }

        btnValider.setOnClickListener {
            val quartier = etZone.text.toString().trim()
            val selectedId = rgType.checkedRadioButtonId

            if (quartier.isNotEmpty() && selectedId != -1) {
                val radioButton = view.findViewById<RadioButton>(selectedId)
                envoyerAlerte(quartier, radioButton.text.toString())

                // Fermer le clavier avant de fermer la dialog
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)

                dialog.dismiss()
            } else {
                Toast.makeText(this, "Veuillez remplir le quartier et le type", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.setContentView(view)
        dialog.show()
    }

    @SuppressLint("MissingPermission")
    private fun recupererLocalisation(editText: EditText) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                try {
                    val geocoder = Geocoder(this, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        // Priorité au quartier, sinon à la ville
                        val nomQuartier = address.subLocality ?: address.locality ?: "Zone inconnue"

                        editText.setText(nomQuartier)
                        Toast.makeText(this, "📍 Localisé à : $nomQuartier", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Adresse introuvable sur la carte", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("GPS", "Erreur Geocoder", e)
                    Toast.makeText(this, "Erreur de géolocalisation", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "GPS en cours de calibration, réessayez...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun envoyerAlerte(quartier: String, type: String) {
        // ✅ CORRECTION 2 : Harmonisation des majuscules pour l'Admin
        val statutPropre = "EN COURS"

        val alerteData = hashMapOf(
            "quartier" to quartier,
            "type" to type,
            "timestamp" to FieldValue.serverTimestamp(),
            "status" to statutPropre, // Avant c'était "en_cours"
            "auteurEmail" to (Firebase.auth.currentUser?.email ?: "Anonyme")
        )

        db.collection("alertes").add(alerteData)
            .addOnSuccessListener {
                // ✅ CORRECTION 3 : Confirmation visuelle pour l'utilisateur
                Toast.makeText(this, "✅ Alerte signalée avec succès !", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "❌ Erreur : ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}