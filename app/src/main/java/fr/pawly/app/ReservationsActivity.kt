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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.pawly.app.data.ReservationRepository
import fr.pawly.app.data.ReservationSupabase
import kotlinx.coroutines.launch

class ReservationsActivity : AppCompatActivity() {

    private val reservationsActuelles = mutableListOf<ReservationSupabase>()
    private lateinit var adapter: ReservationAdapter
    private lateinit var rv: RecyclerView
    private lateinit var tvEmpty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reservations)
        supportActionBar?.title = "Mes réservations"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        rv      = findViewById(R.id.rvReservations)
        tvEmpty = findViewById(R.id.tvEmptyReservations)

        adapter = ReservationAdapter(reservationsActuelles) { idReservation, position ->
            annulerReservation(idReservation, position)
        }
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        chargerReservations()
    }

    private fun chargerReservations() {
        lifecycleScope.launch {
            ReservationRepository.getReservations().onSuccess { liste ->
                reservationsActuelles.clear()
                reservationsActuelles.addAll(liste)
                adapter.notifyDataSetChanged()
                rafraichirVisibilite()
            }.onFailure { e ->
                Toast.makeText(this@ReservationsActivity,
                    "❌ Impossible de charger vos réservations : ${e.localizedMessage}",
                    Toast.LENGTH_LONG).show()
                rafraichirVisibilite()
            }
        }
    }

    private fun annulerReservation(idReservation: String?, position: Int) {
        if (idReservation == null) {
            Toast.makeText(this, "❌ Réservation invalide", Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch {
            ReservationRepository.annulerReservation(idReservation).onSuccess {
                reservationsActuelles[position] = reservationsActuelles[position].copy(statut = "annulee")
                adapter.notifyItemChanged(position)
                Toast.makeText(this@ReservationsActivity, "Réservation annulée", Toast.LENGTH_SHORT).show()
            }.onFailure { e ->
                Toast.makeText(this@ReservationsActivity,
                    "❌ Erreur lors de l'annulation : ${e.localizedMessage}",
                    Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun rafraichirVisibilite() {
        if (reservationsActuelles.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            rv.visibility      = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rv.visibility      = View.VISIBLE
        }
    }

    class ReservationAdapter(
        private val items: MutableList<ReservationSupabase>,
        private val onAnnuler: (idReservation: String?, position: Int) -> Unit
    ) : RecyclerView.Adapter<ReservationAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvTitle:    TextView = view.findViewById(R.id.tvResaTitle)
            val tvDates:    TextView = view.findViewById(R.id.tvResaDates)
            val tvPrix:     TextView = view.findViewById(R.id.tvResaPrix)
            val tvStatut:   TextView = view.findViewById(R.id.tvResaStatut)
            val btnAnnuler: Button   = view.findViewById(R.id.btnAnnulerResa)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_reservation, parent, false))

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val r = items[position]
            holder.tvTitle.text  = r.typeGarde ?: "Garde"
            holder.tvDates.text  = "📅 Du ${r.dateDebut} au ${r.dateFin}"
            val prixTexte = r.prixTotalFraisPlateforme?.let { "%.2f€".format(it) } ?: "Tarif non spécifié"
            holder.tvPrix.text   = "💰 $prixTexte"
            holder.tvStatut.text = when (r.statut) {
                "confirmee", "acceptee" -> "✅ Confirmée"
                "annulee"               -> "❌ Annulée"
                "refusee"                -> "❌ Refusée"
                else                     -> "⏳ En attente"
            }
            holder.btnAnnuler.isEnabled = r.statut != "annulee"
            holder.btnAnnuler.setOnClickListener {
                android.app.AlertDialog.Builder(holder.itemView.context)
                    .setTitle("Annuler ?")
                    .setPositiveButton("Oui") { _, _ ->
                        onAnnuler(r.idReservation, position)
                    }
                    .setNegativeButton("Non", null)
                    .show()
            }
        }

        override fun getItemCount() = items.size
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}