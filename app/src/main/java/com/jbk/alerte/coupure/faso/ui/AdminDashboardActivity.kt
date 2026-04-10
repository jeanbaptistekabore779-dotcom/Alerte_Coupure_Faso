package com.jbk.alerte.coupure.faso.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.jbk.alerte.coupure.faso.R
import com.jbk.alerte.coupure.faso.adapters.AlerteAdapter
import com.jbk.alerte.coupure.faso.adapters.UserAdapter
import com.jbk.alerte.coupure.faso.databinding.ActivityAdminDashboardBinding
import com.jbk.alerte.coupure.faso.models.Alerte
import com.jbk.alerte.coupure.faso.models.User

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding
    private lateinit var alerteAdapter: AlerteAdapter
    private lateinit var userAdapter: UserAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuration Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Tableau de Bord Admin"

        // Configuration des onglets
        binding.tabLayout.removeAllTabs()
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Alertes"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Citoyens"))

        setupRecyclerView()
        setupTabs()
        setupInteractions()

        chargerStatsEtProbabilites()
        chargerProfilAdmin()
    }

    private fun setupRecyclerView() {
        binding.rvAdmin.layoutManager = LinearLayoutManager(this)

        // Adapter pour les Alertes
        alerteAdapter = AlerteAdapter(
            listeTotale = mutableListOf(),
            isAdmin = true,
            onItemClick = { alerte -> afficherDialogueStatut(alerte) },
            onItemLongClick = { alerte -> afficherDialogueSuppression(alerte) }
        )

        // Adapter pour les Utilisateurs (Citoyens)
        userAdapter = UserAdapter(
            users = mutableListOf(),
            onBlockClick = { user -> toggleUserBlockStatus(user) },
            onDeleteClick = { user -> afficherDialogueSuppressionUser(user) }
        )

        binding.rvAdmin.adapter = alerteAdapter
        chargerAlertes()
    }

    private fun chargerStatsEtProbabilites() {
        // Stats Alertes
        db.collection("alertes")
            .whereEqualTo("status", "EN COURS")
            .addSnapshotListener { snapshots, _ ->
                val count = snapshots?.size() ?: 0
                binding.txtCountCoupures.text = count.toString()
            }

        // Stats Citoyens
        db.collection("users")
            .addSnapshotListener { snapshots, _ ->
                val count = snapshots?.size() ?: 0
                binding.txtCountUsers.text = count.toString()
            }
    }

    private fun chargerAlertes() {
        binding.swipeRefresh.isRefreshing = true
        db.collection("alertes")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, _ ->
                binding.swipeRefresh.isRefreshing = false
                if (snapshots != null) {
                    val list = snapshots.documents.mapNotNull { doc ->
                        doc.toObject(Alerte::class.java)?.apply { id = doc.id }
                    }
                    actualiserVisibilite(list.isEmpty())
                    alerteAdapter.updateData(list)
                }
            }
    }

    private fun chargerUtilisateurs() {
        binding.swipeRefresh.isRefreshing = true
        db.collection("users")
            .addSnapshotListener { snapshots, _ ->
                binding.swipeRefresh.isRefreshing = false
                if (snapshots != null) {
                    val list = snapshots.documents.mapNotNull { doc ->
                        doc.toObject(User::class.java)?.apply { uid = doc.id }
                    }
                    actualiserVisibilite(list.isEmpty())
                    userAdapter.updateData(list)
                }
            }
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
            if (binding.tabLayout.selectedTabPosition == 0) chargerAlertes() else chargerUtilisateurs()
            chargerStatsEtProbabilites()
        }

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddAlerteActivity::class.java))
        }

        // Action de déconnexion si tu as un bouton
        binding.imgAdminProfile.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Déconnexion")
                .setMessage("Voulez-vous quitter la session admin ?")
                .setPositiveButton("Oui") { _, _ ->
                    auth.signOut()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("Non", null).show()
        }
    }

    // --- DIALOGUES ---
    private fun afficherDialogueStatut(alerte: Alerte) {
        val options = arrayOf("EN COURS", "RÉSOLU", "MAINTENANCE")
        AlertDialog.Builder(this)
            .setTitle("Changer le statut")
            .setItems(options) { _, which ->
                db.collection("alertes").document(alerte.id).update("status", options[which])
            }.show()
    }

    private fun toggleUserBlockStatus(user: User) {
        val nouveauStatut = !user.estBloque
        db.collection("users").document(user.uid).update("estBloque", nouveauStatut)
            .addOnSuccessListener {
                val msg = if (nouveauStatut) "Citoyen bloqué" else "Citoyen débloqué"
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
    }

    private fun afficherDialogueSuppressionUser(user: User) {
        AlertDialog.Builder(this)
            .setTitle("Supprimer")
            .setMessage("Supprimer définitivement ${user.nom} ?")
            .setPositiveButton("Oui") { _, _ ->
                db.collection("users").document(user.uid).delete()
            }
            .setNegativeButton("Non", null).show()
    }

    private fun afficherDialogueSuppression(alerte: Alerte) {
        AlertDialog.Builder(this)
            .setTitle("Supprimer l'alerte")
            .setPositiveButton("Oui") { _, _ ->
                db.collection("alertes").document(alerte.id).delete()
            }
            .setNegativeButton("Non", null).show()
    }

    private fun actualiserVisibilite(isEmpty: Boolean) {
        binding.layoutEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvAdmin.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun chargerProfilAdmin() {
        val user = auth.currentUser ?: return
        db.collection("users").document(user.uid).get().addOnSuccessListener { doc ->
            val url = doc.getString("photoUrl")
            if (!url.isNullOrEmpty()) Glide.with(this).load(url).circleCrop().into(binding.imgAdminProfile)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}