package com.jbk.alerte.coupure.faso.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.jbk.alerte.coupure.faso.R
import com.jbk.alerte.coupure.faso.adapters.AlerteAdapter
import com.jbk.alerte.coupure.faso.adapters.CommuniqueAdapter
import com.jbk.alerte.coupure.faso.models.Alerte
import com.jbk.alerte.coupure.faso.models.Communique
import com.jbk.alerte.coupure.faso.ui.AddAlerteActivity
import java.util.Locale

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var alerteAdapter: AlerteAdapter
    private lateinit var communiqueAdapter: CommuniqueAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = Firebase.auth
    private val listeAlertes = mutableListOf<Alerte>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvSignalements = view.findViewById<RecyclerView>(R.id.rvSignalements)
        val rvCommuniques = view.findViewById<RecyclerView>(R.id.rvCommuniqueOfficiel)
        val tvSalut = view.findViewById<TextView>(R.id.tvSalut)
        val tvCountCoupures = view.findViewById<TextView>(R.id.tvCountCoupures)
        val tvCountRetablies = view.findViewById<TextView>(R.id.tvCountRetablies)
        val fabAdd = view.findViewById<FloatingActionButton>(R.id.fabSignaler)
        val searchView = view.findViewById<SearchView>(R.id.searchView)
        val btnVoirTout = view.findViewById<ImageButton>(R.id.btnVoirToutCommuniques)

        alerteAdapter = AlerteAdapter(listeTotale = listeAlertes, isAdmin = false)
        rvSignalements.layoutManager = LinearLayoutManager(requireContext())
        rvSignalements.adapter = alerteAdapter
        rvSignalements.isNestedScrollingEnabled = false

        communiqueAdapter = CommuniqueAdapter(emptyList())
        rvCommuniques.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvCommuniques.adapter = communiqueAdapter

        btnVoirTout.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragment_container, CommuniqueFragment()) // VERIFIE TON ID ICI
                .addToBackStack(null)
                .commit()
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                alerteAdapter.filter.filter(newText)
                return true
            }
        })

        fabAdd.setOnClickListener {
            startActivity(Intent(requireContext(), AddAlerteActivity::class.java))
        }

        ecouterStatistiques(tvCountCoupures, tvCountRetablies)
        chargerNomUtilisateur(tvSalut)
        chargerAlertesTempsReel()
        chargerCommuniquesTempsReel()
    }

    private fun ecouterStatistiques(tvCoupures: TextView, tvRetablies: TextView) {
        db.collection("alertes").addSnapshotListener { snapshots, _ ->
            snapshots?.let { docs ->
                val active = docs.count { doc ->
                    val t = doc.getString("type")?.uppercase(Locale.ROOT) ?: ""
                    val s = doc.getString("status")?.uppercase(Locale.ROOT) ?: ""
                    (t == "COUPURE" || t == "PANNE") && s == "EN COURS"
                }
                val resolved = docs.count { doc ->
                    val t = doc.getString("type")?.uppercase(Locale.ROOT) ?: ""
                    val s = doc.getString("status")?.uppercase(Locale.ROOT) ?: ""
                    t == "RETOUR" || s == "RESOLU" || s == "RÉSOLU"
                }
                tvCoupures.text = String.format(Locale.FRANCE, "%02d", active)
                tvRetablies.text = String.format(Locale.FRANCE, "%02d", resolved)
            }
        }
    }

    private fun chargerAlertesTempsReel() {
        db.collection("alertes").orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) return@addSnapshotListener
                val list = snapshots?.documents?.mapNotNull { doc ->
                    try {
                        val ts = when (val raw = doc.get("timestamp")) {
                            is Timestamp -> raw
                            is Long -> Timestamp(raw / 1000, 0)
                            else -> null
                        }
                        Alerte(
                            id = doc.id,
                            quartier = doc.getString("quartier") ?: doc.getString("zone") ?: "",
                            type = doc.getString("type") ?: "",
                            ville = doc.getString("ville") ?: "Ouagadougou",
                            status = doc.getString("status") ?: "EN COURS",
                            timestamp = ts,
                            auteurEmail = doc.getString("auteurEmail") ?: "",
                            auteurPhotoUrl = doc.getString("auteurPhotoUrl")
                        )
                    } catch (e: Exception) { null }
                } ?: emptyList()
                alerteAdapter.updateData(list)
            }
    }

    private fun chargerNomUtilisateur(textView: TextView) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get().addOnSuccessListener { doc ->
            val prenom = doc.getString("prenom") ?: getString(R.string.default_citoyen)
            textView.text = getString(R.string.salut_format, prenom)
        }
    }

    private fun chargerCommuniquesTempsReel() {
        db.collection("communiques")
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(1) // On n'en garde qu'un seul pour l'aperçu à l'accueil
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    android.util.Log.e("FIRESTORE", "Erreur : ${error.message}")
                    return@addSnapshotListener
                }

                val list = snapshots?.toObjects(Communique::class.java) ?: emptyList()
                communiqueAdapter.mettreAJour(list)
                android.util.Log.d("FIRESTORE", "Communiqués trouvés : ${list.size}")
            }
    }
}