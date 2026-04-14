package com.jbk.alerte.coupure.faso.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.tabs.TabLayout
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.jbk.alerte.coupure.faso.databinding.FragmentAdminDashboardBinding
import com.jbk.alerte.coupure.faso.databinding.DialogCommuniqueSonabelBinding
import com.jbk.alerte.coupure.faso.models.RapportVille
import com.jbk.alerte.coupure.faso.adapters.RapportVilleAdapter
import java.util.Locale
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.data.Entry




class AdminDashboardFragment : Fragment() {

    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private lateinit var rapportAdapter: RapportVilleAdapter
    private val labelsVilles = ArrayList<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configuration RecyclerView
        rapportAdapter = RapportVilleAdapter(mutableListOf())
        binding.rvAdminContent.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAdminContent.adapter = rapportAdapter

        ecouterGlobalStats()

        binding.adminTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> genererRapportGlobal()
                    1 -> chargerDonneesCitoyens()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
        binding.barChartVilles.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                // e?.x est maintenant reconnu car 'e' est bien une 'Entry'
                val index = e?.x?.toInt() ?: return

                if (index < labelsVilles.size) {
                    val villeSelectionnee = labelsVilles[index]
                    Toast.makeText(requireContext(), "Détails pour $villeSelectionnee", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onNothingSelected() {}
        })

        binding.btnPostCommunique.setOnClickListener { ouvrirDialoguePublication() }

        genererRapportGlobal()
    }

    private fun ecouterGlobalStats() {
        // Compter uniquement les alertes de type COUPURE
        db.collection("alertes")
            .whereEqualTo("type", "COUPURE")
            .addSnapshotListener { snap, _ ->
                binding.tvTotalSignalements.text =
                    String.format(Locale.FRANCE, "%02d", snap?.size() ?: 0)
            }

        db.collection("users").addSnapshotListener { snap, _ ->
            binding.tvTotalUsers.text =
                String.format(Locale.FRANCE, "%02d", snap?.size() ?: 0)
        }
    }

    private fun genererRapportGlobal() {
        db.collection("alertes").addSnapshotListener { snapshots, e ->
            if (e != null) {
                Log.e("ADMIN_DASHBOARD", "Erreur Firestore: ${e.message}")
                return@addSnapshotListener
            }

            val docs = snapshots?.documents ?: emptyList()
            val statsParVille = docs.groupBy { it.getString("ville") ?: "Inconnue" }

            val listeRapports = statsParVille.map { (nomVille, documentsVille) ->
                val total = documentsVille.size
                val enCours = documentsVille.count {
                    it.getString("status")?.uppercase() == "EN COURS"
                }
                val resolues = documentsVille.count {
                    it.getString("status")?.uppercase() == "RÉSOLU" ||
                            it.getString("status")?.uppercase() == "RESOLU"
                }

                RapportVille(
                    nomVille = nomVille,
                    totalAlertes = total,
                    enCours = enCours,
                    resolues = resolues
                )
            }

            val listeTriee = listeRapports.sortedByDescending { it.enCours }

            rapportAdapter.updateList(listeTriee)
            afficherHistogramme(listeTriee)
        }
    }

    private fun afficherHistogramme(listeRapports: List<RapportVille>) {
        val entries = ArrayList<BarEntry>()
        labelsVilles.clear() // On vide la liste globale déclarée en haut de la classe

        listeRapports.take(5).forEachIndexed { index, rapport ->
            entries.add(BarEntry(index.toFloat(), rapport.enCours.toFloat()))
            labelsVilles.add(rapport.nomVille)
        }

        val dataSet = BarDataSet(entries, "État des coupures")
        configurerCouleursGraphique(dataSet)

        // Formater l'affichage au-dessus des barres : "Nombre (Probabilité%)"
        dataSet.valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
            override fun getBarLabel(barEntry: BarEntry?): String {
                val nbCoupures = barEntry?.y ?: 0f
                val proba = (nbCoupures / 50f) * 100
                return "${nbCoupures.toInt()} c. (${proba.toInt()}%)"
            }
        }

        val barData = BarData(dataSet)
        barData.barWidth = 0.6f
        barData.setValueTextSize(10f)
        barData.setValueTextColor(Color.DKGRAY)

        binding.barChartVilles.apply {
            data = barData
            description.isEnabled = false
            setDrawValueAboveBar(true)
            animateY(1000)

            xAxis.apply {
                // On utilise labelsVilles qui est maintenant synchronisé avec les entries
                valueFormatter = IndexAxisValueFormatter(labelsVilles)
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
                labelRotationAngle = -30f
            }

            axisLeft.axisMinimum = 0f
            axisLeft.axisMaximum = 55f // Laisse de la place pour le texte (seuil à 50)
            axisRight.isEnabled = false
            invalidate()
        }
    }

    private fun configurerCouleursGraphique(dataSet: BarDataSet) {
        val couleurs = ArrayList<Int>()

        for (entry in dataSet.values) {
            val nbCoupures = entry.y
            val pourcentage = (nbCoupures / 50f) * 100 // Probabilité basée sur un seuil de 50

            // On décide de la couleur en croisant les deux critères
            when {
                // Si on a plus de 10 coupures OU plus de 20% de risque
                nbCoupures >= 10 || pourcentage >= 20f -> couleurs.add(Color.RED)

                // Si on a entre 5 et 10 coupures OU entre 10% et 20% de risque
                nbCoupures >= 5 || pourcentage >= 10f -> couleurs.add(Color.YELLOW)

                else -> couleurs.add(Color.GREEN)
            }
        }
        dataSet.colors = couleurs
    }

    private fun ouvrirDialoguePublication() {
        val dialogBinding = DialogCommuniqueSonabelBinding.inflate(layoutInflater)
        val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root).create()

        dialogBinding.btnPublierOfficiel.setOnClickListener {
            val titre = dialogBinding.etTitreCommunique.text.toString().trim()
            val message = dialogBinding.etMessageCommunique.text.toString().trim()

            if (titre.isEmpty() || message.isEmpty()) {
                Toast.makeText(context, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            dialogBinding.btnPublierOfficiel.isEnabled = false
            val communique = hashMapOf(
                "titre" to titre, "message" to message,
                "date" to Timestamp.now(), "auteur" to "Administration SONABEL"
            )

            db.collection("communiques").add(communique).addOnSuccessListener {
                Toast.makeText(context, "Publié !", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }.addOnFailureListener { dialogBinding.btnPublierOfficiel.isEnabled = true }
        }
        dialog.show()
    }

    private fun chargerDonneesCitoyens() {
        Toast.makeText(context, "Section citoyens en développement", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}