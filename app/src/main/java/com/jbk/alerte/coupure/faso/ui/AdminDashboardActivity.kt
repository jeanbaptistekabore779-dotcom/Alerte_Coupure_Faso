package com.jbk.alerte.coupure.faso.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.jbk.alerte.coupure.faso.R
import com.jbk.alerte.coupure.faso.adapters.AlerteAdapter
import com.jbk.alerte.coupure.faso.adapters.UserAdapter
import com.jbk.alerte.coupure.faso.databinding.ActivityAdminDashboardBinding
import com.jbk.alerte.coupure.faso.models.Alerte
import com.jbk.alerte.coupure.faso.models.User
import com.squareup.picasso.Picasso
import java.util.Calendar

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding
    private lateinit var alerteAdapter: AlerteAdapter
    // On déclare enfin l'adapter utilisateur
    private lateinit var userAdapter: UserAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- CONFIGURATION TOOLBAR ---
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        setupRecyclerView()
        setupTabs()
        setupInteractions()

        // --- CHARGEMENT INITIAL ---
        chargerAlertes()
        chargerStatsDuJour()
        compterUtilisateurs()
        chargerProfilAdmin()
    }

    private fun setupRecyclerView() {
        binding.rvAdmin.layoutManager = LinearLayoutManager(this)

        // Initialisation de l'adapter Alertes
        alerteAdapter = AlerteAdapter(
            listeTotale = mutableListOf(),
            isAdmin = true,
            onItemClick = { alerte -> afficherDialogueStatut(alerte) },
            onItemLongClick = { alerte -> afficherDialogueSuppression(alerte) }
        )

        // Initialisation de l'adapter Utilisateurs (C'est ce qui manquait !)
        userAdapter = UserAdapter(
            users = mutableListOf(),
            onBlockClick = { user -> toggleUserBlockStatus(user) },
            onDeleteClick = { user -> afficherDialogueSuppressionUser(user) }
        )

        // Par défaut, on affiche l'adapter des alertes
        binding.rvAdmin.adapter = alerteAdapter
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        binding.rvAdmin.adapter = alerteAdapter
                        chargerAlertes()
                    }
                    1 -> {
                        binding.rvAdmin.adapter = userAdapter
                        chargerUtilisateurs()
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupInteractions() {
        binding.swipeRefresh.setOnRefreshListener {
            // On rafraîchit selon l'onglet actif
            if (binding.tabLayout.selectedTabPosition == 0) chargerAlertes()
            else chargerUtilisateurs()

            chargerStatsDuJour()
            compterUtilisateurs()
        }

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddAlerteActivity::class.java))
        }
    }

    // --- LOGIQUE ALERTES ---

    private fun chargerAlertes() {
        db.collection("alertes")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, _ ->
                binding.swipeRefresh.isRefreshing = false

                if (snapshots != null) {
                    val list = mutableListOf<Alerte>()
                    for (doc in snapshots.documents) {
                        val alerte = doc.toObject(Alerte::class.java)
                        if (alerte != null) {
                            alerte.id = doc.id
                            list.add(alerte)
                        }
                    }

                    actualiserVisibilite(list.isEmpty())
                    alerteAdapter.updateData(list)
                }
            }
    }

    // --- LOGIQUE UTILISATEURS ---

    private fun chargerUtilisateurs() {
        db.collection("users").addSnapshotListener { snapshots, _ ->
            binding.swipeRefresh.isRefreshing = false
            if (snapshots != null) {
                val list = snapshots.toObjects(User::class.java)
                for (i in list.indices) {
                    list[i].uid = snapshots.documents[i].id
                }

                actualiserVisibilite(list.isEmpty())
                userAdapter.updateData(list)
            }
        }
    }

    private fun toggleUserBlockStatus(user: User) {
        val nouveauStatut = !user.estBloque
        db.collection("users").document(user.uid)
            .update("estBloque", nouveauStatut)
            .addOnSuccessListener {
                val action = if (nouveauStatut) "bloqué" else "débloqué"
                Toast.makeText(this, "Utilisateur $action", Toast.LENGTH_SHORT).show()
            }
    }

    // --- DIALOGUES ---

    private fun afficherDialogueStatut(alerte: Alerte) {
        val options = arrayOf("EN COURS", "RÉSOLU", "MAINTENANCE")
        AlertDialog.Builder(this)
            .setTitle("Mise à jour État")
            .setItems(options) { _, which ->
                db.collection("alertes").document(alerte.id).update("status", options[which])
                    .addOnSuccessListener { Toast.makeText(this, "Statut mis à jour", Toast.LENGTH_SHORT).show() }
            }.show()
    }

    private fun afficherDialogueSuppression(alerte: Alerte) {
        AlertDialog.Builder(this)
            .setTitle("Suppression")
            .setMessage("Voulez-vous supprimer l'alerte à ${alerte.quartier} ?")
            .setPositiveButton("Supprimer") { _, _ ->
                db.collection("alertes").document(alerte.id).delete()
            }
            .setNegativeButton("Annuler", null).show()
    }

    private fun afficherDialogueSuppressionUser(user: User) {
        AlertDialog.Builder(this)
            .setTitle("Supprimer l'utilisateur")
            .setMessage("Supprimer ${user.nom} ?")
            .setPositiveButton("Supprimer") { _, _ ->
                db.collection("users").document(user.uid).delete()
            }
            .setNegativeButton("Annuler", null).show()
    }

    // --- UTILITAIRES ---

    private fun actualiserVisibilite(isEmpty: Boolean) {
        if (isEmpty) {
            binding.layoutEmpty.visibility = View.VISIBLE
            binding.rvAdmin.visibility = View.GONE
        } else {
            binding.layoutEmpty.visibility = View.GONE
            binding.rvAdmin.visibility = View.VISIBLE
        }
    }

    private fun chargerProfilAdmin() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user?.photoUrl != null) {
            Picasso.get().load(user.photoUrl).placeholder(R.drawable.ic_admin_profile).into(binding.imgAdminProfile)
        }
    }

    private fun chargerStatsDuJour() {
        val debut = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.time

        db.collection("alertes")
            .whereGreaterThanOrEqualTo("timestamp", Timestamp(debut))
            .addSnapshotListener { snapshots, _ ->
                binding.txtCountCoupures.text = (snapshots?.size() ?: 0).toString()
            }
    }

    private fun compterUtilisateurs() {
        db.collection("users").addSnapshotListener { snapshots, _ ->
            binding.txtCountUsers.text = (snapshots?.size() ?: 0).toString()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}