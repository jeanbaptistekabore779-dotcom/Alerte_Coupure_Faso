package com.jbk.alerte.coupure.faso.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
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

        // 🛡️ Anti-clignotement
        binding.drawerLayout.visibility = View.INVISIBLE
        verifierRoleEtInitialiser(user.uid, savedInstanceState)

        setupOnBackPressed()
    }

    private fun verifierRoleEtInitialiser(uid: String, savedInstanceState: Bundle?) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                val role = document.getString("role")

                if (role == "ADMIN") {
                    startActivity(Intent(this, AdminDashboardActivity::class.java))
                    finish()
                } else {
                    binding.drawerLayout.visibility = View.VISIBLE
                    chargerComposantsInterface(savedInstanceState)
                }
            }
            .addOnFailureListener {
                binding.drawerLayout.visibility = View.VISIBLE
                chargerComposantsInterface(savedInstanceState)
                Toast.makeText(this, "Erreur réseau", Toast.LENGTH_SHORT).show()
            }
    }

    private fun chargerComposantsInterface(savedInstanceState: Bundle?) {
        setupNavigationDrawer()
        configurerHeader()

        if (savedInstanceState == null) {
            // Assure-toi que HomeFragment existe bien dans ton dossier ui/fragments
            remplacerFragment(HomeFragment())
            supportActionBar?.title = "Accueil"
            binding.navigationView.setCheckedItem(R.id.nav_home)
        }
    }

    private fun setupNavigationDrawer() {
        val toolbar = binding.mainContent.root.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    remplacerFragment(HomeFragment())
                    supportActionBar?.title = "Accueil"
                }
                R.id.nav_dashboard -> {
                    lancerDashboardDynamique()
                }
                // --- AJOUTE CETTE PARTIE POUR LES NOTIFICATIONS ---
                R.id.nav_notifications -> {
                    // Si tu as créé un NotificationsFragment, décommente la ligne d'après :
                    // remplacerFragment(NotificationsFragment())
                    // supportActionBar?.title = "Mes Alertes"

                    // En attendant, on peut juste faire un petit message :
                    Toast.makeText(this, "Affichage des alertes de votre zone", Toast.LENGTH_SHORT).show()
                }
                // ---------------------------------------------------
                R.id.nav_profile -> {
                    remplacerFragment(ProfileFragment())
                    supportActionBar?.title = "Mon Profil"
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

    private fun lancerDashboardDynamique() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get().addOnSuccessListener { doc ->
            val role = doc.getString("role") ?: "USER"
            val intent = if (role == "ADMIN") {
                Intent(this, AdminDashboardActivity::class.java)
            } else {
                Intent(this, UserDashboardActivity::class.java)
            }
            startActivity(intent)
        }
    }

    private fun remplacerFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null) // ✅ AJOUT : Permet de revenir en arrière avec le bouton du téléphone
            .commit()
    }

    private fun configurerHeader() {
        val headerView = binding.navigationView.getHeaderView(0)
        val tvNomHeader = headerView?.findViewById<TextView>(R.id.tvNomUser)
        val user = auth.currentUser

        if (user != null && tvNomHeader != null) {
            db.collection("users").document(user.uid).get().addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val nom = doc.getString("prenom") ?: user.email?.substringBefore("@")
                    tvNomHeader.text = nom?.replaceFirstChar { it.uppercase() }

                    val role = doc.getString("role")
                    // Cacher le menu admin s'il existe dans le drawer_menu.xml
                    binding.navigationView.menu.findItem(R.id.nav_admin_panel)?.isVisible = (role == "ADMIN")
                }
            }
        }
    }

    private fun redirigerVersLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun setupOnBackPressed() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else if (supportFragmentManager.backStackEntryCount > 0) {
                    // ✅ AJOUT : Si on est dans un fragment, on pop le fragment au lieu de quitter
                    supportFragmentManager.popBackStack()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }
}