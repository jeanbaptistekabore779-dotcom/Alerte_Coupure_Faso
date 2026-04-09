package com.jbk.alerte.coupure.faso.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jbk.alerte.coupure.faso.R
import com.jbk.alerte.coupure.faso.models.Signalement
import com.jbk.alerte.coupure.faso.models.TypeSignalement
import android.graphics.Color
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SignalementAdapter(private val listeSignalements: List<Signalement>) : RecyclerView.Adapter<SignalementAdapter.SignalementViewHolder>() {

    class SignalementViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtType: TextView = view.findViewById(R.id.txtTypeSignalement)
        val txtZone: TextView = view.findViewById(R.id.txtZone)
        val txtDate: TextView = view.findViewById(R.id.txtDate)
        val indicateur: View = view.findViewById(R.id.indicatorColor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SignalementViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_signalement, parent, false)
        return SignalementViewHolder(view)
    }

    override fun onBindViewHolder(holder: SignalementViewHolder, position: Int) {
        val signalement = listeSignalements[position]

        holder.txtZone.text = "Zone: ${signalement.zone}"

        // Formatage de la date (Optionnel mais plus joli)
        val dateFormatee = SimpleDateFormat("dd/MM HH:mm", Locale.FRANCE).format(Date(signalement.timestamp))
        holder.txtDate.text = dateFormatee

        // Utilisation de TON Enum
        if (signalement.type == TypeSignalement.COUPURE) {
            holder.txtType.text = "⚡ Coupure d'électricité"
            holder.indicateur.setBackgroundColor(Color.parseColor("#FFFF5252")) // Rouge
        } else {
            holder.txtType.text = "💡 Retour du courant"
            holder.indicateur.setBackgroundColor(Color.parseColor("#FF4CAF50")) // Vert
        }
    }

    override fun getItemCount(): Int = listeSignalements.size
}