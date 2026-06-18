package fr.pawly.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import fr.pawly.app.data.ReservationRepository
import kotlinx.coroutines.launch
import java.util.Locale

class ReservationsActivity : AppCompatActivity() {

    private var listeReservations = mutableListOf<ReservationSupabase>()
    private lateinit var reservationsAdapter: ReservationsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reservations)

        supportActionBar?.title = "Mes Réservations 📅"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val rv = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvReservations)
        reservationsAdapter = ReservationsAdapter(listeReservations)
        rv.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        rv.adapter = reservationsAdapter

        chargerDonnees()
    }

    private fun chargerDonnees() {
        lifecycleScope.launch {
            ReservationRepository.getReservations()
                .onSuccess { list ->
                    listeReservations.clear()
                    listeReservations.addAll(list) // Plus besoin de boucle de conversion complexe !
                    reservationsAdapter.notifyDataSetChanged()
                }
                .onFailure { e ->
                    Toast.makeText(this@ReservationsActivity, "Erreur : ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    fun executerAnnulation(idReservation: String, position: Int) {
        lifecycleScope.launch {
            ReservationRepository.demanderRemboursement(idReservation)
                .onSuccess {
                    Toast.makeText(this@ReservationsActivity, "Demande transmise !", Toast.LENGTH_LONG).show()
                    listeReservations[position] = listeReservations[position].copy(statut = "annulee")
                    reservationsAdapter.notifyItemChanged(position)
                }
                .onFailure { e ->
                    Toast.makeText(this@ReservationsActivity, "Échec : ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    inner class ReservationsAdapter(private val items: List<ReservationSupabase>) :
        androidx.recyclerview.widget.RecyclerView.Adapter<ReservationsAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
            val tvResaTitle: TextView = view.findViewById(R.id.tvResaTitle)
            val tvResaStatut: TextView = view.findViewById(R.id.tvResaStatut)
            val tvResaDates: TextView = view.findViewById(R.id.tvResaDates)
            val tvResaPrix: TextView = view.findViewById(R.id.tvResaPrix)
            val btnAnnulerResa: Button = view.findViewById(R.id.btnAnnulerResa)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reservation, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val res = items[position]

            holder.tvResaTitle.text = res.typeGarde ?: "Garde d'animaux"
            holder.tvResaDates.text = String.format(Locale.FRANCE, "📅 Du %s au %s", res.dateDebut, res.dateFin)
            holder.tvResaPrix.text = String.format(Locale.FRANCE, "💰 %.2f €", res.prixTotalFraisPlateforme ?: 0.0)

            val statutTexte = res.statut.replace("_", " ").uppercase()
            holder.tvResaStatut.text = String.format(Locale.FRANCE, "● %s", statutTexte)

            if (res.statut == "annulee" || res.statutRemboursement == "Demande") {
                holder.btnAnnulerResa.visibility = View.GONE
            } else {
                holder.btnAnnulerResa.visibility = View.VISIBLE
                holder.btnAnnulerResa.setOnClickListener {
                    res.idReservation?.let { id -> executerAnnulation(id, position) }
                }
            }
        }

        override fun getItemCount() = items.size
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}