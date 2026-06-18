package fr.pawly.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

data class Vet(
    val name: String,
    val address: String,
    val phone: String,
    val distance: String,
    val urgence: Boolean
)

class VetActivity : AppCompatActivity() {

    private val vets = listOf(
        Vet("Dr. Martin — Clinique du Parc",
            "12 rue du Parc, Paris 11e",
            "0123456789", "0.3 km", true),
        Vet("Dr. Dupont — Cabinet Vétérinaire",
            "45 bd de la République, Paris 10e",
            "0134567890", "0.8 km", false),
        Vet("Clinique Vétérinaire 24h/24",
            "8 rue de la Santé, Paris 12e",
            "0145678901", "1.2 km", true),
        Vet("Dr. Bernard — SPA Animaux",
            "23 avenue Victor Hugo, Paris 9e",
            "0156789012", "1.5 km", false),
        Vet("Centre Vétérinaire d'Urgence",
            "67 rue de Rivoli, Paris 1er",
            "0167890123", "2.1 km", true)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vet)

        supportActionBar?.title = "Vétérinaires à proximité 🏥"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        findViewById<RecyclerView>(R.id.rvVets).apply {
            layoutManager = LinearLayoutManager(this@VetActivity)
            adapter = VetAdapter(vets)
        }
    }

    inner class VetAdapter(private val vets: List<Vet>) :
        RecyclerView.Adapter<VetAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView     = view.findViewById(R.id.tvVetName)
            val tvAddress: TextView  = view.findViewById(R.id.tvVetAddress)
            val tvPhone: TextView    = view.findViewById(R.id.tvVetPhone)
            val tvDistance: TextView = view.findViewById(R.id.tvVetDistance)
            val tvUrgence: TextView  = view.findViewById(R.id.tvVetUrgence)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_vet, parent, false)
            )

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val vet = vets[position]
            holder.tvName.text     = vet.name
            holder.tvAddress.text  = "📍 ${vet.address}"
            holder.tvPhone.text    = "📞 ${vet.phone}"
            holder.tvDistance.text = "🚶 ${vet.distance}"
            holder.tvUrgence.text  = if (vet.urgence)
                "🚨 Urgences 24h/24" else "⏰ Sur rendez-vous"
            holder.tvUrgence.setTextColor(
                if (vet.urgence)
                    getColor(android.R.color.holo_red_dark)
                else
                    getColor(android.R.color.darker_gray)
            )
            holder.tvPhone.setOnClickListener {
                startActivity(Intent(Intent.ACTION_DIAL,
                    Uri.parse("tel:${vet.phone}")))
            }
        }

        override fun getItemCount() = vets.size
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}