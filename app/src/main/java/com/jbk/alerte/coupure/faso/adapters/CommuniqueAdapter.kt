package com.jbk.alerte.coupure.faso.adapters

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.jbk.alerte.coupure.faso.R
import com.jbk.alerte.coupure.faso.models.Communique
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommuniqueAdapter(private var liste: List<Communique>) :
    RecyclerView.Adapter<CommuniqueAdapter.CommuniqueViewHolder>() {

    class CommuniqueViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitre: TextView = view.findViewById(R.id.tvTitreCommunique)
        val tvMsg: TextView = view.findViewById(R.id.tvMessageCommunique)
        val tvDate: TextView = view.findViewById(R.id.tvDateCommunique)
        val btnShare: ImageButton = view.findViewById(R.id.btnShare)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommuniqueViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_communique, parent, false)
        return CommuniqueViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommuniqueViewHolder, position: Int) {
        val item = liste[position]
        holder.tvTitre.text = item.titre
        holder.tvMsg.text = item.message

        // Formatage de la date en français pour le Burkina
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE)
        holder.tvDate.text = sdf.format(Date(item.timestamp))

        // Logique de partage WhatsApp / SMS
        holder.btnShare.setOnClickListener {
            try {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "⚡ *ALERTE SONABEL* ⚡\n\n*${item.titre}*\n${item.message}\n\n_Via Alerte Coupure Faso_")
                }
                holder.itemView.context.startActivity(Intent.createChooser(shareIntent, "Partager l'alerte via :"))
            } catch (e: Exception) {
                Toast.makeText(holder.itemView.context, "Erreur de partage", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount() = liste.size

    fun mettreAJour(nouvelleListe: List<Communique>) {
        this.liste = nouvelleListe
        notifyDataSetChanged()
        Log.d("ADAPTER_DEBUG", "Nombre d'éléments reçus : ${nouvelleListe.size}")
    }
}