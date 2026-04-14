package com.jbk.alerte.coupure.faso.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jbk.alerte.coupure.faso.R
import com.jbk.alerte.coupure.faso.models.RapportVille
import java.util.Locale

class RapportVilleAdapter(private var liste: List<RapportVille>) :
    RecyclerView.Adapter<RapportVilleAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNomVille: TextView = view.findViewById(R.id.tvNomVille)
        val tvStats: TextView = view.findViewById(R.id.tvStatsVille)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Utilise un layout simple pour l'instant ou crée 'item_rapport_ville.xml'
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rapport_ville, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val rapport = liste[position]
        holder.tvNomVille.text = rapport.nomVille
        // Utilisation de Locale pour corriger le warning String.format
        holder.tvStats.text = String.format(Locale.FRANCE,
            "En cours: %02d | Résolues: %02d",
            rapport.enCours, rapport.resolues)
    }

    override fun getItemCount() = liste.size

    fun updateList(nouvelleListe: List<RapportVille>) {
        this.liste = nouvelleListe
        notifyDataSetChanged()
    }
}