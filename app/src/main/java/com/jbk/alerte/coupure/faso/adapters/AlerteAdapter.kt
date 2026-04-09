package com.jbk.alerte.coupure.faso.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.jbk.alerte.coupure.faso.R
import com.jbk.alerte.coupure.faso.models.Alerte
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Adapter pour gérer l'affichage des alertes de coupures.
 * Supporte le filtrage, les rôles Admin et l'optimisation DiffUtil.
 */
class AlerteAdapter(
    private var listeTotale: List<Alerte>,
    private val isAdmin: Boolean,
    private val onItemClick: ((Alerte) -> Unit)? = null,
    private val onItemLongClick: ((Alerte) -> Unit)? = null
) : RecyclerView.Adapter<AlerteAdapter.AlerteViewHolder>(), Filterable {

    private var listeAffichee: List<Alerte> = listeTotale

    class AlerteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtZone: TextView = view.findViewById(R.id.txtZone)
        val txtType: TextView = view.findViewById(R.id.txtType)
        // Vérifie bien que dans ton XML orange c'est txtStatus ou tvStatus.
        // Si ça re-bloque, change R.id.txtStatus par l'ID exact de ton XML.
        val txtStatus: TextView = view.findViewById(R.id.txtStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlerteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alerte, parent, false)
        return AlerteViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlerteViewHolder, position: Int) {
        val alerte = listeAffichee[position]

        // 1. Remplissage des données de base
        holder.txtZone.text = alerte.quartier.ifEmpty { "Zone inconnue" }
        holder.txtType.text = alerte.type

        // 2. Formatage de la date et du statut
        val ts = alerte.timestamp
        if (ts != null) {
            val date = ts.toDate()
            val sdf = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
            holder.txtStatus.text = "Le ${sdf.format(date)} - ${alerte.status}"
        } else {
            holder.txtStatus.text = "Statut : ${alerte.status}"
        }

        // 3. Logique d'interaction
        if (isAdmin) {
            holder.itemView.setOnClickListener { onItemClick?.invoke(alerte) }
            holder.itemView.setOnLongClickListener {
                onItemLongClick?.invoke(alerte)
                true
            }
        } else {
            holder.itemView.setOnClickListener(null)
            holder.itemView.setOnLongClickListener(null)
        }
    }

    override fun getItemCount() = listeAffichee.size

    fun updateData(nouvelleListe: List<Alerte>) {
        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize() = listeAffichee.size
            override fun getNewListSize() = nouvelleListe.size
            override fun areItemsTheSame(oldPos: Int, newPos: Int) =
                listeAffichee[oldPos].id == nouvelleListe[newPos].id
            override fun areContentsTheSame(oldPos: Int, newPos: Int) =
                listeAffichee[oldPos] == nouvelleListe[newPos]
        }
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        this.listeTotale = nouvelleListe
        this.listeAffichee = nouvelleListe
        diffResult.dispatchUpdatesTo(this)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString().lowercase()
                val filteredList = if (charSearch.isEmpty()) {
                    listeTotale
                } else {
                    listeTotale.filter {
                        it.quartier.lowercase().contains(charSearch)
                    }
                }
                return FilterResults().apply { values = filteredList }
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                listeAffichee = (results?.values as? List<Alerte>) ?: listOf()
                notifyDataSetChanged()
            }
        }
    }
}