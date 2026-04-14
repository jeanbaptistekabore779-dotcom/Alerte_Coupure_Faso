package com.jbk.alerte.coupure.faso.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.jbk.alerte.coupure.faso.R
import com.jbk.alerte.coupure.faso.adapters.CommuniqueAdapter
import com.jbk.alerte.coupure.faso.databinding.FragmentCommuniqueBinding
import com.jbk.alerte.coupure.faso.models.Communique

class CommuniqueFragment : Fragment(R.layout.fragment_communique) {

    private var _binding: FragmentCommuniqueBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: CommuniqueAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCommuniqueBinding.bind(view)

        // 1. Initialiser l'adapter avec une liste vide
        adapter = CommuniqueAdapter(emptyList())

        // 2. Configurer le RecyclerView avec l'ID exact de ton layout
        binding.rvToutsLesCommuniques.layoutManager = LinearLayoutManager(requireContext())
        binding.rvToutsLesCommuniques.adapter = adapter

        // 3. Récupération en temps réel
        chargerCommuniques()
    }

    private fun chargerCommuniques() {
        // CORRECTION : "communiques" (selon ta console) et "date" (selon ton modèle)
        db.collection("communiques")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("FIRESTORE", "Erreur de chargement", e)
                    return@addSnapshotListener
                }

                val list = snapshots?.toObjects(Communique::class.java) ?: emptyList()

                // Mettre à jour l'UI
                adapter.mettreAJour(list)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}