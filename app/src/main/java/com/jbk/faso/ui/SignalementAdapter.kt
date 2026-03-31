package com.jbk.alerte.coupure.faso.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import bf.ujkz.alerte_coupure_faso.utils.DateUtils
import com.jbk.alerte.coupure.faso.R
import com.jbk.alerte.coupure.faso.databinding.ItemSignalementBinding
import com.jbk.alerte.coupure.faso.models.Signalement
import com.jbk.alerte.coupure.faso.models.TypeSignalement
import java.text.SimpleDateFormat
import java.util.*

class SignalementAdapter : RecyclerView.Adapter<SignalementAdapter.ViewHolder>() {

    private var liste = listOf<Signalement>()

    fun soumettreListe(nouvelleListe: List<Signalement>) {
        this.liste = nouvelleListe
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSignalementBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // On passe simplement l'objet au ViewHolder
        holder.bind(liste[position])
    }

    override fun getItemCount() = liste.size

    class ViewHolder(private val binding: ItemSignalementBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(signalement: Signalement) {
            binding.txtZone.text = signalement.zone

            // --- UTILISATION DE TON DATEUTILS ---
            // On utilise bf.ujkz.alerte_coupure_faso.utils.DateUtils
            binding.txtDate.text = DateUtils.formaterDate(signalement.timestamp)

            // Changement de couleur et texte selon le type
            if (signalement.type == TypeSignalement.COUPURE) {
                binding.txtType.text = "Coupure de courant"
                // Rouge pour le danger/coupure
                binding.imgStatus.setColorFilter(ContextCompat.getColor(itemView.context, android.R.color.holo_red_dark))
                binding.txtType.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.holo_red_dark))
            } else {
                binding.txtType.text = "Retour du courant"
                // Vert pour le rétablissement
                binding.imgStatus.setColorFilter(ContextCompat.getColor(itemView.context, android.R.color.holo_green_dark))
                binding.txtType.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.holo_green_dark))
            }
        }
    }
}