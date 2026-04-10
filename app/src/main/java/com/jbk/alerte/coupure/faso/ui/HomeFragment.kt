package com.jbk.alerte.coupure.faso.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.jbk.alerte.coupure.faso.R
import com.jbk.alerte.coupure.faso.adapters.AlerteAdapter
import com.jbk.alerte.coupure.faso.models.Alerte

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var adapter: AlerteAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.rvSignalements)
        adapter = AlerteAdapter(mutableListOf(), isAdmin = false)

        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        view.findViewById<TextView>(R.id.tvSalut).text = "Salut Jean Baptiste !"

        db.collection("alertes")
            .orderBy("ville", Query.Direction.ASCENDING)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, _ ->
                if (snapshots != null) {
                    val list = snapshots.toObjects(Alerte::class.java).onEachIndexed { index, alerte ->
                        alerte.id = snapshots.documents[index].id
                    }
                    adapter.updateData(list)
                }
            }
    }
}