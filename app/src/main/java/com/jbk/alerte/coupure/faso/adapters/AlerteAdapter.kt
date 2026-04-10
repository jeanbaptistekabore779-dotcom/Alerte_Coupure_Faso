package com.jbk.alerte.coupure.faso.adapters

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jbk.alerte.coupure.faso.R
import com.jbk.alerte.coupure.faso.models.Alerte
import java.text.SimpleDateFormat
import java.util.Locale

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
        val txtStatus: TextView = view.findViewById(R.id.txtStatus)
        val imgIcon: ImageView = view.findViewById(R.id.imgAlerteType)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlerteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_alerte, parent, false)
        return AlerteViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlerteViewHolder, position: Int) {
        val alerte = listeAffichee[position]
        val context = holder.itemView.context

        holder.txtZone.text = alerte.quartier.ifEmpty { "Zone inconnue" }
        holder.txtType.text = alerte.type

        val dateStr = alerte.timestamp?.let {
            val sdf = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
            "Le ${sdf.format(it.toDate())} - "
        } ?: ""
        holder.txtStatus.text = "$dateStr${alerte.status}"

        // --- 🎨 COULEURS DYNAMIQUES ---
        val typeUpper = alerte.type.uppercase()
        val statusUpper = alerte.status.uppercase()

        val (iconRes, colorHex) = when {
            typeUpper.contains("RETOUR") || statusUpper.contains("RESOLU") || statusUpper.contains("RÉSOLU") -> {
                Pair(android.R.drawable.checkbox_on_background, "#388E3C") // Vert
            }
            typeUpper.contains("COUPURE") || typeUpper.contains("PANNE") || typeUpper.contains("DEPANNAGE") -> {
                Pair(android.R.drawable.ic_dialog_alert, "#D32F2F") // Rouge
            }
            else -> {
                Pair(android.R.drawable.stat_sys_warning, "#F57C00") // Orange
            }
        }

        holder.imgIcon.setImageResource(iconRes)
        holder.imgIcon.setColorFilter(Color.parseColor(colorHex))
        holder.txtStatus.setTextColor(Color.parseColor(colorHex))

        // --- 🖱️ GESTION DES CLICS ---
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(alerte)
        }

        holder.itemView.setOnLongClickListener {
            if (isAdmin) {
                // Si c'est l'admin, le clic long sert à supprimer/modifier
                onItemLongClick?.invoke(alerte)
            } else {
                // 🚀 FONCTION PARTAGE (Pour les citoyens)
                val shareMsg = "⚡ Alerte Faso : Coupure (${alerte.type}) à ${alerte.quartier}. Statut : ${alerte.status}. "
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareMsg)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Partager l'alerte via"))
            }
            true
        }
    }

    override fun getItemCount() = listeAffichee.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString().lowercase()
                val filteredList: List<Alerte> = if (charSearch.isEmpty()) {
                    listeTotale
                } else {
                    listeTotale.filter {
                        // Recherche maintenant sur le quartier ET la ville
                        it.quartier.lowercase().contains(charSearch) ||
                                it.ville.lowercase().contains(charSearch)
                    }
                }
                return FilterResults().apply { values = filteredList }
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                listeAffichee = results?.values as? List<Alerte> ?: listOf()
                notifyDataSetChanged()
            }
        }
    }

    fun updateData(nouvelleListe: List<Alerte>) {
        this.listeTotale = nouvelleListe
        this.listeAffichee = nouvelleListe
        notifyDataSetChanged()
    }
}