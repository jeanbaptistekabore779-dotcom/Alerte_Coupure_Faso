package com.jbk.alerte.coupure.faso.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.jbk.alerte.coupure.faso.R
import com.jbk.alerte.coupure.faso.adapters.AlerteAdapter
import com.jbk.alerte.coupure.faso.models.Alerte

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var adapter: AlerteAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = Firebase.auth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Initialisation du RecyclerView
        val rv = view.findViewById<RecyclerView>(R.id.rvSignalements)
        val tvSalut = view.findViewById<TextView>(R.id.tvSalut)

        Toast.makeText(requireContext(), "HomeFragment chargé !", Toast.LENGTH_SHORT).show()
        adapter = AlerteAdapter(mutableListOf(), isAdmin = false)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        // 2. Message de bienvenue dynamique
        chargerNomUtilisateur(tvSalut)

        // 3. Écoute des alertes en temps réel (SnapshotListener)
        chargerAlertesTempsReel()
    }

    private fun chargerNomUtilisateur(textView: TextView) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val prenom = doc.getString("prenom") ?: "Citoyen"
                textView.text = "Salut, $prenom !"
            }
            .addOnFailureListener {
                textView.text = "Salut !"
            }
    }

    private fun chargerAlertesTempsReel() {
        db.collection("alertes")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val list = snapshots.documents.mapNotNull { doc ->
                        val alerte = doc.toObject(Alerte::class.java)
                        alerte?.id = doc.id // On récupère l'ID du document Firestore
                        alerte
                    }
                    adapter.updateData(list)
                }
            }
    }
}