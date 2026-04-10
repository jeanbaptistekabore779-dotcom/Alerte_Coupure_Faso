package com.jbk.alerte.coupure.faso.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.jbk.alerte.coupure.faso.R

class ProfileFragment : Fragment() {

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // On gonfle le layout XML du fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val auth = Firebase.auth
        val user = auth.currentUser

        // Liaison des vues
        val tvNom = view.findViewById<TextView>(R.id.txtProfileName)
        val tvEmail = view.findViewById<TextView>(R.id.txtProfileEmail)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)

        // Affichage des infos
        tvNom.text = user?.displayName ?: "Jean Baptiste Kaboré"
        tvEmail.text = user?.email ?: "Etudiant UJKZ"

        // Gestion de la déconnexion
        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        return view
    }
}