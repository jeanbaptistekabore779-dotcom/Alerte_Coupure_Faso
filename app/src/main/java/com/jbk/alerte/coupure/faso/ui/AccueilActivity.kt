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

class AccueilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAccueilBinding
    private val auth = Firebase.auth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🛡️ ÉTAPE 1 : Vérification de la session
        val user = auth.currentUser
        if (user == null) {
            redirigerVersLogin()
            return
        }

        // 🛡️ ÉTAPE 2 : Vérification du rôle (Admin ou User)
        // Cela évite qu'un admin reste coincé sur l'interface citoyenne
        verifierRoleEtRediriger(user.uid)

        binding = ActivityAccueilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigationDrawer()
        configurerHeader()

        if (savedInstanceState == null) {
            remplacerFragment(HomeFragment())
            supportActionBar?.title = "Accueil"
        }

        setupOnBackPressed()
    }

    private fun verifierRoleEtRediriger(uid: String) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                val role = document.getString("role")
                if (role == "ADMIN") {
                    // Si c'est l'admin, on l'envoie vers son dashboard spécial
                    val intent = Intent(this, AdminDashboardActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erreur de vérification du profil", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupNavigationDrawer() {
        // Correction : On récupère la toolbar via le binding du contenu inclus
        val toolbar = binding.mainContent.root.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
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

    private fun remplacerFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun configurerHeader() {
        // 1. On récupère le NavigationView
        val navView = binding.navigationView

        // 2. On récupère la vue du Header (l'index 0 est le premier header)
        val headerView = navView.getHeaderView(0)

        // 3. On cherche le TextView à l'INTÉRIEUR du headerView
        val tvNomHeader = headerView?.findViewById<TextView>(R.id.tvNomUser)

        val user = auth.currentUser
        if (user != null && tvNomHeader != null) { // On vérifie que tvNomHeader n'est pas NULL
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val nom = doc.getString("prenom") ?: user.email?.substringBefore("@")
                        // ✅ On utilise le "?" pour être sûr de ne pas planter
                        tvNomHeader.text = nom?.replaceFirstChar { it.uppercase() }
                    }
                }
                .addOnFailureListener {
                    tvNomHeader.text = user.email?.substringBefore("@")
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
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }
}