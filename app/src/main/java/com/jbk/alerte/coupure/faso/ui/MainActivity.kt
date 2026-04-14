package com.jbk.alerte.coupure.faso.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.jbk.alerte.coupure.faso.R
import com.jbk.alerte.coupure.faso.databinding.ActivityMainBinding
import com.bumptech.glide.Glide

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Initialiser le ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val currentUser = auth.currentUser

        // 2. Logique de redirection
        if (currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // On ferme la Main si pas connecté
            return
        }

        // 3. Si l'utilisateur est connecté, on configure l'UI
        configurerMenuParRole()
        chargerInfosHeader(currentUser)
    }

    private fun configurerMenuParRole() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                val role = document.getString("role") ?: "USER"

                // Vérifier que navigationView existe dans activity_main.xml
                val menu = binding.navigationView.menu

                // On affiche le groupe admin seulement si le rôle est ADMIN
                menu.setGroupVisible(R.id.group_admin, role == "ADMIN")
            }
            .addOnFailureListener {
                // Optionnel : gérer l'erreur de lecture Firestore
            }
    }

    private fun chargerInfosHeader(user: FirebaseUser) {
        // On récupère la vue du header du NavigationView
        val headerView = binding.navigationView.getHeaderView(0)

        // On utilise les IDs correspondants à ton nav_header.xml
        val tvNom = headerView.findViewById<TextView>(R.id.tvNavName)
        val tvEmail = headerView.findViewById<TextView>(R.id.tvNavEmail)
        val ivPhoto = headerView.findViewById<ImageView>(R.id.ivUserPhoto)

        // Email direct depuis Auth
        tvEmail.text = user.email

        // Infos complémentaires depuis Firestore
        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    tvNom.text = doc.getString("nom") ?: "Utilisateur"

                    val photoUrl = doc.getString("photoUrl")
                    if (!photoUrl.isNullOrEmpty()) {
                        // Utilisation de Glide (déjà importé à la ligne 11)
                        Glide.with(this@MainActivity)
                            .load(photoUrl)
                            .placeholder(R.drawable.ic_person)
                            .circleCrop() // Optionnel : pour que l'image soit ronde dans le menu
                            .into(ivPhoto)
                    }
                }
            }
    }
}