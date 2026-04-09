package com.jbk.alerte.coupure.faso.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.appcompat.widget.SearchView
import android.os.Bundle
import android.util.Log
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

        // Gestion des marges système
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialisation des vues
        // Assure-toi que l'ID dans activity_user_dashboard.xml est bien searchViewDashboard
        searchView = findViewById(R.id.searchViewDashboard)
        recyclerView = findViewById(R.id.rvAlertes)

        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialisation de l'adapter
        adapter = AlerteAdapter(
            listeTotale = listeAlertes,
            isAdmin = false, // L'utilisateur ne peut pas gérer
            onItemClick = null,
            onItemLongClick = null
        )
        recyclerView.adapter = adapter

        // Configuration du filtrage (Recherche par quartier)
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                // Si c'est toujours rouge ici, passe à l'étape 2 ci-dessous
                adapter.filter.filter(newText)
                return true
            }
        })

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
                    listeAlertes.add(alerte)
                }
                adapter.notifyDataSetChanged()
            }
    }

    private fun showSignalementDialog() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_ajouter_signalement, null)

        val btnValider = view.findViewById<Button>(R.id.btnValider)
        val etZone = view.findViewById<EditText>(R.id.etZone)
        val rgType = view.findViewById<RadioGroup>(R.id.rgType)
        val btnGps = view.findViewById<ImageButton>(R.id.btnGps) // Ajoute un ImageButton dans ton XML si tu veux

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
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Champs incomplets", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.setContentView(view)
        dialog.show()
    }

    @SuppressLint("MissingPermission")
    private fun recupererLocalisation(editText: EditText) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // VÉRIFICATION DES PERMISSIONS
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                try {
                    val geocoder = Geocoder(this, Locale.getDefault())
                    // Geocoder peut retourner une liste vide, on vérifie avec addresses?.firstOrNull()
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        // On cherche le quartier (subLocality) ou la ville (locality)
                        val nomQuartier = address.subLocality ?: address.locality ?: "Quartier inconnu"

                        editText.setText(nomQuartier)
                        Toast.makeText(this, "Localisé à : $nomQuartier", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Impossible de trouver le nom du quartier", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("GPS", "Erreur Geocoder", e)
                    Toast.makeText(this, "Erreur lors de la récupération de l'adresse", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Position introuvable. Activez le GPS et réessayez.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun envoyerAlerte(quartier: String, type: String) {
        val alerteData = hashMapOf(
            "quartier" to quartier,
            "type" to type,
            "timestamp" to FieldValue.serverTimestamp(),
            "status" to "en_cours"
        )
        db.collection("alertes").add(alerteData)
    }


}