package com.jbk.alerte.coupure.faso.ui

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.jbk.alerte.coupure.faso.R
import com.jbk.alerte.coupure.faso.databinding.ActivityAccueilBinding
import com.jbk.alerte.coupure.faso.fragments.HomeFragment
import com.jbk.alerte.coupure.faso.fragments.MesAlertesFragment
import com.jbk.alerte.coupure.faso.fragments.ProfileFragment
import com.jbk.alerte.coupure.faso.fragments.AdminDashboardFragment

class AccueilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAccueilBinding
    private val auth = Firebase.auth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccueilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val user = auth.currentUser
        if (user == null) {
            redirigerVersLogin()
            return
        }

        setupNavigationDrawer()
        setupOnBackPressed()
        initialiserEspaceTravail(user.uid, savedInstanceState)
    }

    private fun initialiserEspaceTravail(uid: String, savedInstanceState: Bundle?) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val role = document.getString("role") ?: "USER"
                    val prenom = document.getString("prenom")

                    configurerHeader(prenom, role)

                    if (savedInstanceState == null) {
                        // Tout le monde commence sur HomeFragment
                        remplacerFragment(HomeFragment())
                        supportActionBar?.title = "Accueil"
                        binding.navigationView.setCheckedItem(R.id.nav_home)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erreur de connexion aux données", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupNavigationDrawer() {
        val toolbar = binding.mainContent.toolbar
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {

                R.id.nav_home -> {
                    remplacerFragment(HomeFragment())
                    supportActionBar?.title = "Accueil"
                }

                // ✅ CORRECTION : Maintenant le Dashboard affiche le fragment avec les stats
                R.id.nav_dashboard -> {
                    remplacerFragment(AdminDashboardFragment())
                    supportActionBar?.title = "Tableau de Bord Admin"
                }

                R.id.nav_notifications -> {
                    remplacerFragment(MesAlertesFragment())
                    supportActionBar?.title = "Mes Alertes"
                }

                R.id.nav_profile -> {
                    remplacerFragment(ProfileFragment())
                    supportActionBar?.title = "Mon Profil"
                }

                // ✅ OPTIONNEL : Si tu préfères utiliser le fragment plutôt que l'activité pour l'admin
                R.id.nav_admin_panel -> {
                    remplacerFragment(AdminDashboardFragment())
                    supportActionBar?.title = "Administration"
                }

                R.id.nav_logout -> {
                    auth.signOut()
                    redirigerVersLogin()
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun remplacerFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.fragment_container, fragment)
            .commitAllowingStateLoss()
    }

    private fun configurerHeader(prenom: String?, role: String) {
        val headerView = binding.navigationView.getHeaderView(0)
        val tvNomHeader = headerView.findViewById<TextView>(R.id.tvNavName)
        val tvEmailHeader = headerView.findViewById<TextView>(R.id.tvNavEmail)

        tvNomHeader?.text = prenom ?: "Utilisateur"
        tvEmailHeader?.text = auth.currentUser?.email ?: ""

        // Affiche l'item admin uniquement pour les admins
        val menu = binding.navigationView.menu
        menu.findItem(R.id.nav_admin_panel)?.isVisible = (role == "ADMIN")
    }

    private fun setupOnBackPressed() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    if (supportFragmentManager.backStackEntryCount > 0) {
                        supportFragmentManager.popBackStack()
                    } else {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        })
    }

    private fun redirigerVersLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}