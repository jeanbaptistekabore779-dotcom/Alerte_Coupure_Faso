package com.jbk.alerte.coupure.faso.adapters

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.jbk.alerte.coupure.faso.R
import com.jbk.alerte.coupure.faso.models.Signalement
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SignalementAdapter(private var listeSignalements: MutableList<Signalement>) :
    RecyclerView.Adapter<SignalementAdapter.SignalementViewHolder>() {

    class SignalementViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtType: TextView = view.findViewById(R.id.txtType)
        val txtZone: TextView = view.findViewById(R.id.txtZone)
        val txtDate: TextView = view.findViewById(R.id.txtDate)
        val imgStatus: ImageView = view.findViewById(R.id.imgStatus)
        val btnVoirCarte: MaterialButton = view.findViewById(R.id.btnVoirCarte)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SignalementViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_signalement, parent, false)
        return SignalementViewHolder(view)
    }

    override fun onBindViewHolder(holder: SignalementViewHolder, position: Int) {
        val signalement = listeSignalements[position]

        holder.txtZone.text = "Zone: ${signalement.zone}"

        // Formatage de la date (Timestamp Long vers Date)
        val dateFormatee = SimpleDateFormat("dd/MM HH:mm", Locale.FRANCE).format(Date(signalement.timestamp))
        holder.txtDate.text = "Signalé le : $dateFormatee"

        // CORRECTION : Comparaison par String car Firestore stocke des Strings
        // On compare avec .name ou directement la chaîne de caractères
        if (signalement.type.toString() == "COUPURE") {
            holder.txtType.text = "⚡ Coupure d'électricité"
            holder.txtType.setTextColor(Color.parseColor("#D32F2F"))
            holder.imgStatus.setColorFilter(Color.parseColor("#FF5252"))
            holder.imgStatus.setImageResource(android.R.drawable.ic_dialog_alert)
        } else {
            holder.txtType.text = "💡 Retour du courant"
            holder.txtType.setTextColor(Color.parseColor("#388E3C"))
            holder.imgStatus.setColorFilter(Color.parseColor("#4CAF50"))
            holder.imgStatus.setImageResource(android.R.drawable.ic_dialog_info)
        }

        // Action Google Maps
        holder.btnVoirCarte.setOnClickListener {
            if (signalement.latitude != 0.0 && signalement.longitude != 0.0) {
                val uri = "geo:${signalement.latitude},${signalement.longitude}?q=${signalement.latitude},${signalement.longitude}(${signalement.zone})"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                intent.setPackage("com.google.android.apps.maps")
                holder.itemView.context.startActivity(intent)
            } else {
                Toast.makeText(holder.itemView.context, "Position non renseignée", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int = listeSignalements.size

    fun updateData(newItems: List<Signalement>) {
        this.listeSignalements.clear()
        this.listeSignalements.addAll(newItems)
        notifyDataSetChanged()
    }
}