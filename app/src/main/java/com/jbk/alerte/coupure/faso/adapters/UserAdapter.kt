package com.jbk.alerte.coupure.faso.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jbk.alerte.coupure.faso.R
import com.jbk.alerte.coupure.faso.models.User

class UserAdapter(
    private var users: List<User>, // Changé en var pour pouvoir mettre à jour la liste
    private val onBlockClick: (User) -> Unit,
    private val onDeleteClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNom: TextView = view.findViewById(R.id.tvNomUser)
        val tvEmail: TextView = view.findViewById(R.id.tvEmailUser)
        val btnAction: Button = view.findViewById(R.id.btnActionBloquer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]

        // On concatène Nom et Prénom pour un meilleur affichage
        val nomComplet = "${user.nom} ${user.prenom}".trim()
        holder.tvNom.text = if (nomComplet.isNotEmpty()) nomComplet else "Utilisateur sans nom"
        holder.tvEmail.text = user.email

        // 1. Gestion dynamique du bouton Bloquer/Débloquer
        if (user.estBloque) {
            holder.btnAction.text = "Débloquer"
            holder.btnAction.setBackgroundColor(Color.parseColor("#4CAF50")) // Vert plus doux
        } else {
            holder.btnAction.text = "Bloquer"
            holder.btnAction.setBackgroundColor(Color.parseColor("#F44336")) // Rouge plus doux
        }

        // 2. Action au clic sur le bouton
        holder.btnAction.setOnClickListener {
            onBlockClick(user)
        }

        // 3. Action au clic long pour la suppression
        holder.itemView.setOnLongClickListener {
            onDeleteClick(user)
            true
        }
    }

    override fun getItemCount() = users.size

    // CRUCIAL : Permet de rafraîchir la liste quand Firestore change
    fun updateData(newUsers: List<User>) {
        this.users = newUsers
        notifyDataSetChanged()
    }
}