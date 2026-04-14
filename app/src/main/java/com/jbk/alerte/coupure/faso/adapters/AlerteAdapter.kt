package com.jbk.alerte.coupure.faso.adapters

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jbk.alerte.coupure.faso.R
import com.jbk.alerte.coupure.faso.models.Alerte
import java.text.SimpleDateFormat
import java.util.Locale

@SuppressLint("SetTextI18n")
class AlerteAdapter(
    private var listeTotale: List<Alerte>,
    private val isAdmin: Boolean,
    private val onItemClick: ((Alerte) -> Unit)? = null,
    private val onItemLongClick: ((Alerte) -> Unit)? = null
) : RecyclerView.Adapter<AlerteAdapter.AlerteViewHolder>(), Filterable {

    private var listeAffichee: List<Alerte> = listeTotale

    class AlerteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // ✅ IDs corrigés pour correspondre exactement à item_alerte.xml
        val tvZone: TextView = view.findViewById(R.id.tvZone)
        val tvType: TextView = view.findViewById(R.id.tvType)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)   // ✅ Statut seul
        val tvDate: TextView = view.findViewById(R.id.tvDate)        // ✅ Date seule
        val imgAlerteType: ImageView = view.findViewById(R.id.imgAlerteType) // ✅ Corrigé
        val ivUserPhoto: ImageView = view.findViewById(R.id.ivUserPhoto)
        val btnLocaliser: ImageButton = view.findViewById(R.id.btnLocaliser) // ✅ Corrigé
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlerteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_alerte, parent, false)
        return AlerteViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlerteViewHolder, position: Int) {
        android.util.Log.d("ADAPTER", "Binding $position / ${listeAffichee.size}")
        val alerte = listeAffichee[position]
        val context = holder.itemView.context

        // ✅ tvZone et tvType avec les bons noms de propriétés
        holder.tvZone.text = "Zone: ${alerte.quartier.ifEmpty { "Inconnue" }}"
        holder.tvType.text = alerte.type

        // ✅ tvStatus et tvDate sont maintenant séparés
        val dateStr = alerte.timestamp?.let {
            val sdf = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
            sdf.format(it.toDate())
        } ?: "Date inconnue"

        holder.tvStatus.text = alerte.status
        holder.tvDate.text = dateStr

        val typeUpper = alerte.type.uppercase()
        val statusUpper = alerte.status.uppercase()

        val (iconRes, colorHex) = when {
            typeUpper.contains("RETOUR") || statusUpper.contains("RÉSOLU") ->
                Pair(android.R.drawable.btn_star_big_on, "#388E3C")
            typeUpper.contains("COUPURE") || typeUpper.contains("PANNE") ->
                Pair(android.R.drawable.ic_dialog_alert, "#D32F2F")
            else ->
                Pair(android.R.drawable.stat_sys_warning, "#F57C00")
        }

        // ✅ imgAlerteType corrigé
        holder.imgAlerteType.setImageResource(iconRes)
        holder.imgAlerteType.setColorFilter(colorHex.toColorInt())
        holder.tvStatus.setTextColor(colorHex.toColorInt())

        // ✅ Chargement sécurisé de la photo de l'auteur
        val photoUrl = alerte.auteurPhotoUrl?.takeIf { it.isNotBlank() }
        if (!photoUrl.isNullOrEmpty()) {
            Glide.with(context)
                .load(photoUrl)
                .placeholder(R.drawable.ic_person)
                .circleCrop()
                .into(holder.ivUserPhoto)
        } else {
            holder.ivUserPhoto.setImageResource(R.drawable.ic_person)
        }

        // Bouton localiser
        holder.btnLocaliser.setOnClickListener {
            try {
                val quartierRecherche = alerte.quartier.replace(" ", "+")
                val ville = if (alerte.ville.isNotEmpty()) alerte.ville.replace(" ", "+") else "Burkina+Faso"
                val gmmIntentUri = "geo:0,0?q=$quartierRecherche+$ville+Burkina+Faso".toUri()
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")

                if (mapIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(mapIntent)
                } else {
                    val webUri = "https://www.google.com/maps/search/?api=1&query=$quartierRecherche+$ville".toUri()
                    context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Erreur lors de l'ouverture de la carte", Toast.LENGTH_SHORT).show()
            }
        }

        holder.itemView.setOnClickListener { onItemClick?.invoke(alerte) }

        if (isAdmin) {
            holder.itemView.setOnLongClickListener {
                onItemLongClick?.invoke(alerte)
                true
            }
        }
    }

    override fun getItemCount() = listeAffichee.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): Filter.FilterResults {
                val charSearch = constraint.toString().lowercase()
                val filteredList: List<Alerte> = if (charSearch.isEmpty()) {
                    listeTotale
                } else {
                    listeTotale.filter {
                        it.quartier.lowercase().contains(charSearch) ||
                                it.ville.lowercase().contains(charSearch)
                    }
                }
                return Filter.FilterResults().apply { values = filteredList }
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: Filter.FilterResults?) {
                listeAffichee = results?.values as? List<Alerte> ?: listOf()
                notifyDataSetChanged()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(nouvelleListe: List<Alerte>) {
        this.listeTotale = nouvelleListe
        this.listeAffichee = nouvelleListe
        notifyDataSetChanged()
    }
}