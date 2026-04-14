package com.jbk.alerte.coupure.faso.ui

import android.Manifest
import com.google.firebase.Timestamp
import com.jbk.alerte.coupure.faso.models.Alerte
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.jbk.alerte.coupure.faso.databinding.ActivityAddAlerteBinding

import com.jbk.alerte.coupure.faso.models.TypeSignalement

import java.util.UUID


import com.jbk.alerte.coupure.faso.data.local.AppDatabase
import com.jbk.alerte.coupure.faso.models.Signalement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
class AddAlerteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddAlerteBinding
    private val db = FirebaseFirestore.getInstance()
    private val storage = Firebase.storage.reference
    private val fusedLocationClient by lazy { LocationServices.getFusedLocationProviderClient(this) }

    private var imageUri: Uri? = null

    // 1. Déclarer le sélecteur d'image (Photo Picker Android 13+)
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            imageUri = uri
            binding.ivApercu.visibility = View.VISIBLE
            binding.ivApercu.setImageURI(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddAlerteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {
        // Configuration du menu déroulant
        val typesCoupure = arrayOf("COUPURE", "RETOUR")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, typesCoupure)
        binding.autoCompleteType.setAdapter(adapter)

        // Bouton pour choisir une photo
        binding.btnAjouterPhoto.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.btnPublier.setOnClickListener {
            val quartier = binding.etQuartier.text.toString().trim()
            val typeStr = binding.autoCompleteType.text.toString()

            if (quartier.isNotEmpty() && typeStr.isNotEmpty()) {
                val typeEnum = TypeSignalement.valueOf(typeStr)
                demarrerProcessusPublication(quartier, typeEnum)
            } else {
                Toast.makeText(this, "Veuillez remplir les champs", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnAnnuler.setOnClickListener { finish() }
    }

    private fun demarrerProcessusPublication(quartier: String, type: TypeSignalement) {
        binding.btnPublier.isEnabled = false
        Toast.makeText(this, "Publication en cours...", Toast.LENGTH_SHORT).show()

        // Vérification des permissions avant d'appeler le client de localisation
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Si pas de permission, on publie avec les coordonnées de Ouagadougou par défaut
            uploaderImageSiExiste(quartier, type, 12.37, -1.53)
            return
        }

        fusedLocationClient.lastLocation.addOnCompleteListener { task ->
            // On récupère la position si le task a réussi, sinon null
            val location = if (task.isSuccessful) task.result else null
            val lat = location?.latitude ?: 12.37
            val lng = location?.longitude ?: -1.53

            uploaderImageSiExiste(quartier, type, lat, lng)
        }
    }

    private fun uploaderImageSiExiste(quartier: String, type: TypeSignalement, lat: Double, lng: Double) {
        if (imageUri == null) {
            enregistrerAlerte(quartier, type, lat, lng, null)
            return
        }

        // Upload vers Firebase Storage
        val path = "signalements/${UUID.randomUUID()}.jpg"
        val imageRef = storage.child(path)

        imageRef.putFile(imageUri!!)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { url ->
                    enregistrerAlerte(quartier, type, lat, lng, url.toString())
                }
            }
            .addOnFailureListener {
                enregistrerAlerte(quartier, type, lat, lng, null) // On publie quand même sans image
            }
    }

    private fun enregistrerAlerte(quartier: String, type: TypeSignalement, lat: Double, lng: Double, imageUrl: String?) {
        val user = Firebase.auth.currentUser ?: return

        // ✅ Récupère la photo de profil depuis Firestore
        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { doc ->
                val photoProfilUrl = doc.getString("photoUrl")
                    ?.takeIf { it.isNotEmpty() }

                val nouvelleAlerte = Alerte(
                    id = "",
                    quartier = quartier,
                    type = type.name,
                    ville = "Ouagadougou",
                    status = "EN COURS",
                    timestamp = Timestamp.now(),
                    auteurEmail = user.email ?: "",
                    auteurPhotoUrl = photoProfilUrl // ✅ Photo Firestore
                )

                db.collection("alertes").add(nouvelleAlerte)
                    .addOnSuccessListener { docRef ->
                        docRef.update("id", docRef.id)

                        // ✅ Sauvegarde locale dans Room avec le modèle Signalement existant
                        val signalement = Signalement(
                            idFirestore = docRef.id,
                            zone = quartier,
                            type = type, // TypeSignalement enum directement
                            status = "EN COURS",
                            timestamp = System.currentTimeMillis(),
                            latitude = lat,
                            longitude = lng,
                            userId = user.email ?: "",
                            imageUrl = imageUrl
                        )
                        CoroutineScope(Dispatchers.IO).launch {
                            AppDatabase.getDatabase(applicationContext).signalementDao().insert(signalement)
                        }

                        if (type == TypeSignalement.RETOUR) {
                            resoudreCoupureCorrespondante(quartier)
                        }

                        Toast.makeText(this, "Alerte publiée !", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        binding.btnPublier.isEnabled = true
                        Toast.makeText(this, "Erreur lors de la publication", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                binding.btnPublier.isEnabled = true
                Toast.makeText(this, "Erreur de récupération du profil", Toast.LENGTH_SHORT).show()
            }
    }

    private fun resoudreCoupureCorrespondante(quartier: String) {
        db.collection("alertes")
            .whereEqualTo("quartier", quartier)
            .whereEqualTo("type", "COUPURE")
            .whereEqualTo("status", "EN COURS")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshots ->
                val doc = snapshots.documents.firstOrNull()
                if (doc != null) {
                    db.collection("alertes").document(doc.id)
                        .update("status", "RÉSOLU")
                        .addOnSuccessListener {
                            android.util.Log.d("ALERTE", "Coupure résolue : ${doc.id}")
                        }
                }
            }
    }
}