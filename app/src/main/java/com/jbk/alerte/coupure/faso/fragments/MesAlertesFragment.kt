package com.jbk.alerte.coupure.faso.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.jbk.alerte.coupure.faso.R
import com.jbk.alerte.coupure.faso.adapters.AlerteAdapter
import com.jbk.alerte.coupure.faso.models.Alerte

class MesAlertesFragment : Fragment(R.layout.fragment_mes_alertes) {

    private lateinit var adapter: AlerteAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.rvMesAlertes)
        adapter = AlerteAdapter(listeTotale = emptyList(), isAdmin = false)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.isNestedScrollingEnabled = true
        rv.adapter = adapter

        chargerAlertes()
    }

    private fun chargerAlertes() {
        db.collection("alertes")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null) return@addSnapshotListener

                val liste = snapshots.documents.mapNotNull { doc ->
                    try {
                        val timestamp: Timestamp? = when (val raw = doc.get("timestamp")) {
                            is Timestamp -> raw
                            is Long -> Timestamp(raw / 1000, ((raw % 1000) * 1_000_000).toInt())
                            is Double -> Timestamp(raw.toLong() / 1000, 0)
                            else -> null
                        }
                        Alerte(
                            id = doc.id,
                            quartier = doc.getString("quartier") ?: doc.getString("zone") ?: "",
                            type = doc.getString("type") ?: "",
                            ville = doc.getString("ville") ?: "Ouagadougou",
                            status = doc.getString("status") ?: "EN COURS",
                            timestamp = timestamp,
                            auteurEmail = doc.getString("auteurEmail") ?: "",
                            auteurPhotoUrl = doc.getString("auteurPhotoUrl")
                        )
                    } catch (e: Exception) { null }
                }

                // ✅ Toutes les alertes s'affichent
                adapter.updateData(liste)

                view?.findViewById<View>(R.id.tvAucuneAlerte)?.visibility =
                    if (liste.isEmpty()) View.VISIBLE else View.GONE
            }
    }
}