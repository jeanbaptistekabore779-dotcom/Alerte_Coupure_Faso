package com.jbk.faso.ui

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jbk.alerte.coupure.faso.R
import com.jbk.faso.models.Communique
import java.text.SimpleDateFormat
import java.util.*

class CommuniqueAdapter(private var liste: List<Communique>) :
    RecyclerView.Adapter<CommuniqueAdapter.CommuniqueViewHolder>() {

    class CommuniqueViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitre: TextView = view.findViewById(R.id.tvTitreCommunique)
        val tvMsg: TextView = view.findViewById(R.id.tvMessageCommunique)
        val tvDate: TextView = view.findViewById(R.id.tvDateCommunique)
        val btnShare: ImageButton = view.findViewById(R.id.btnShare) // Assure-toi d'ajouter ce bouton dans ton XML
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommuniqueViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_communique, parent, false)
        return CommuniqueViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommuniqueViewHolder, position: Int) {
        val item = liste[position]
        holder.tvTitre.text = item.titre
        holder.tvMsg.text = item.message

        // Formatage de la date
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE)
        holder.tvDate.text = sdf.format(Date(item.timestamp))

        // Logique de partage
        holder.btnShare.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "⚡ *ALERTE SONABEL* ⚡\n\n*${item.titre}*\n${item.message}\n\n_Via Alerte Coupure Faso_")
            }
            holder.itemView.context.startActivity(Intent.createChooser(shareIntent, "Partager l'alerte via :"))
        }
    }

    override fun getItemCount() = liste.size

    fun mettreAJour(nouvelleListe: List<Communique>) {
        this.liste = nouvelleListe
        notifyDataSetChanged()

        android.util.Log.d("ADAPTER_DEBUG", "Nombre d'éléments reçus : ${nouvelleListe.size}")
        notifyDataSetChanged()
    }
}