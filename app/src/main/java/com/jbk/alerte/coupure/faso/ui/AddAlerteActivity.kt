package com.jbk.alerte.coupure.faso.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.jbk.alerte.coupure.faso.R // Import de ton propre R
import com.jbk.alerte.coupure.faso.databinding.ActivityAddAlerteBinding
import com.jbk.alerte.coupure.faso.models.Signalement
import com.jbk.alerte.coupure.faso.models.TypeSignalement

class AddAlerteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddAlerteBinding
    private val db = FirebaseFirestore.getInstance()
    private val fusedLocationClient by lazy { LocationServices.getFusedLocationProviderClient(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddAlerteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuration du menu déroulant
        val typesCoupure = arrayOf("COUPURE", "RETOUR") // Correspond à ton Enum
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, typesCoupure)
        binding.autoCompleteType.setAdapter(adapter)

        binding.btnPublier.setOnClickListener {
            val quartier = binding.etQuartier.text.toString().trim()
            val typeStr = binding.autoCompleteType.text.toString()

            if (quartier.isNotEmpty() && typeStr.isNotEmpty()) {
                val typeEnum = TypeSignalement.valueOf(typeStr)
                recupererPositionEtEnregistrer(quartier, typeEnum)
            } else {
                Toast.makeText(this, "Veuillez remplir les champs", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnAnnuler.setOnClickListener { finish() }
    }

    private fun recupererPositionEtEnregistrer(quartier: String, type: TypeSignalement) {
        // Vérifier la permission GPS
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 101)
            // Si pas de permission, on enregistre quand même avec 0.0, 0.0
            enregistrerAlerte(quartier, type, 0.0, 0.0)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            val lat = location?.latitude ?: 0.0
            val lng = location?.longitude ?: 0.0
            enregistrerAlerte(quartier, type, lat, lng)
        }
    }

    private fun enregistrerAlerte(quartier: String, type: TypeSignalement, lat: Double, lng: Double) {
        val nouvelleAlerte = Signalement(
            idFirestore = "", // Sera généré par Firebase
            zone = quartier,
            type = type,
            timestamp = System.currentTimeMillis(),
            latitude = lat,
            longitude = lng
        )

        db.collection("signalements").add(nouvelleAlerte)
            .addOnSuccessListener { docRef ->
                // Mise à jour de l'ID Firestore dans l'objet pour la cohérence
                docRef.update("idFirestore", docRef.id)
                Toast.makeText(this, "Alerte publiée avec succès !", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erreur : ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}