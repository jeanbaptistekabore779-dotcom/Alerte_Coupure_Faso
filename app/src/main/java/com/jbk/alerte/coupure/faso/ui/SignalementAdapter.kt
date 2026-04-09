package com.jbk.alerte.coupure.faso.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jbk.alerte.coupure.faso.R
import com.jbk.alerte.coupure.faso.models.Signalement
import com.jbk.alerte.coupure.faso.models.TypeSignalement
import java.text.SimpleDateFormat
import java.util.*

class SignalementAdapter : RecyclerView.Adapter<SignalementAdapter.SignalementViewHolder>() {

    private var listeSignalements = mutableListOf<Signalement>()
    private var listeFiltree = mutableListOf<Signalement>()

    class SignalementViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgStatus: ImageView = view.findViewById(R.id.imgStatus)
        val txtZone: TextView = view.findViewById(R.id.txtZone)
        val txtType: TextView = view.findViewById(R.id.txtType)
        val txtDate: TextView = view.findViewById(R.id.txtDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SignalementViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_signalement, parent, false)
        return SignalementViewHolder(view)
    }

    override fun onBindViewHolder(holder: SignalementViewHolder, position: Int) {
        val signalement = listeFiltree[position]

        holder.txtZone.text = signalement.zone

        // Formatage de la date (ex: 14:30)
        val sdf = SimpleDateFormat("HH:mm", Locale.FRANCE)
        holder.txtDate.text = sdf.format(Date(signalement.timestamp))

        // Logique visuelle selon le type (Coupure ou Retour)
        if (signalement.type == TypeSignalement.COUPURE) {
            holder.txtType.text = "Coupure d'électricité"
            holder.txtType.setTextColor(Color.RED)
            holder.imgStatus.setImageResource(android.R.drawable.ic_dialog_alert)
            holder.imgStatus.setColorFilter(Color.RED)
        } else {
            holder.txtType.text = "Retour du courant"
            holder.txtType.setTextColor(Color.parseColor("#2E7D32")) // Vert foncé
            holder.imgStatus.setImageResource(android.R.drawable.checkbox_on_background)
            holder.imgStatus.setColorFilter(Color.parseColor("#2E7D32"))
        }
    }

    override fun getItemCount() = listeFiltree.size

    fun soumettreListe(nouvelleListe: List<Signalement>) {
        listeSignalements = nouvelleListe.toMutableList()
        listeFiltree = nouvelleListe.toMutableList()
        notifyDataSetChanged()
    }

    // Fonction de recherche (SearchView)
    fun filtrer(query: String) {
        listeFiltree = if (query.isEmpty()) {
            listeSignalements
        } else {
            listeSignalements.filter {
                it.zone.contains(query, ignoreCase = true)
            }.toMutableList()
        }
        notifyDataSetChanged()
    }
}