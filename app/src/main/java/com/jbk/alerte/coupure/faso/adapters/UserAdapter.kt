package com.jbk.alerte.coupure.faso.adapters

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.jbk.alerte.coupure.faso.R
import com.jbk.alerte.coupure.faso.models.User

class UserAdapter(
    private var users: List<User>,
    private val onBlockClick: (User) -> Unit,
    private val onDeleteClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNom: TextView = view.findViewById(R.id.tvNomUser)
        val tvEmail: TextView = view.findViewById(R.id.tvEmailUser)
        val btnAction: MaterialButton = view.findViewById(R.id.btnActionBloquer) // Utilisation de MaterialButton
        val imgAvatar: android.widget.ImageView = view.findViewById(R.id.imgUserAvatar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]

        // Affichage du nom (Vérifie que ton modèle User possède bien le champ 'prenom')
        val nomComplet = "${user.nom}".trim()
        holder.tvNom.text = if (nomComplet.isNotEmpty()) nomComplet else "Utilisateur"
        holder.tvEmail.text = user.email

        // Gestion du bouton avec backgroundTint pour MaterialButton
        if (user.estBloque) {
            holder.btnAction.text = "Débloquer"
            holder.btnAction.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4CAF50"))
        } else {
            holder.btnAction.text = "Bloquer"
            holder.btnAction.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F44336"))
        }

        // Chargement de l'image (Ne pas oublier Glide pour le rendu pro)
        Glide.with(holder.itemView.context)
            .load(user.photoUrl)
            .placeholder(R.drawable.ic_person)
            .circleCrop()
            .into(holder.imgAvatar)

        holder.btnAction.setOnClickListener { onBlockClick(user) }

        holder.itemView.setOnLongClickListener {
            onDeleteClick(user)
            true
        }
    }

    override fun getItemCount() = users.size

    fun updateData(newUsers: List<User>) {
        this.users = newUsers
        notifyDataSetChanged()
    }
}