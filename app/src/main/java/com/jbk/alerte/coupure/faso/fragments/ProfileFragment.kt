package com.jbk.alerte.coupure.faso.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.jbk.alerte.coupure.faso.R
import com.jbk.alerte.coupure.faso.ui.LoginActivity

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Liaison des vues avec les NOUVEAUX IDs
        val ivPic = view.findViewById<ShapeableImageView>(R.id.ivProfilePic)
        val txtName = view.findViewById<MaterialTextView>(R.id.txtProfileName)
        val txtEmail = view.findViewById<MaterialTextView>(R.id.txtProfileEmail)
        val txtVille = view.findViewById<MaterialTextView>(R.id.txtProfileVille)
        val chipRole = view.findViewById<Chip>(R.id.chipRole)
        val btnLogout = view.findViewById<MaterialButton>(R.id.btnLogout)

        // 2. Récupération des données Firestore
        val uid = Firebase.auth.currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val prenom = doc.getString("prenom") ?: ""
                    val nom = doc.getString("nom") ?: ""
                    txtName.text = "$prenom $nom".trim()
                    txtEmail.text = doc.getString("email") ?: ""
                    txtVille.text = "Ville : ${doc.getString("ville") ?: "Non définie"}"

                    // Gestion du rôle (Admin ou Citoyen)
                    val role = doc.getString("role") ?: "USER"
                    if (role == "ADMIN") {
                        chipRole.text = "ADMINISTRATEUR"
                        chipRole.setChipBackgroundColorResource(R.color.purple_500)
                        chipRole.setTextColor(resources.getColor(android.R.color.white, null))
                    } else {
                        chipRole.text = "CITOYEN"
                    }

                    // Chargement de la vraie photo avec Glide
                    val photoUrl = doc.getString("photoUrl")
                    if (!photoUrl.isNullOrEmpty()) {
                        Glide.with(this).load(photoUrl).into(ivPic)
                    }
                }
            }

        // 3. Action du bouton Déconnexion
        btnLogout.setOnClickListener {
            Firebase.auth.signOut()
            Toast.makeText(context, "Déconnexion réussie", Toast.LENGTH_SHORT).show()

            // Redirection vers Login et fermeture de toutes les autres activités
            val intent = Intent(context, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}