package fr.pawly.app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

data class DemandeReservation(
    val animal: String,
    val proprietaire: String,
    val dates: String,
    val prix: String,
    var statut: String = "en_attente"
)

data class GardeEffectuee(
    val animal: String,
    val proprietaire: String,
    val dates: String,
    val note: String,
    val commentaire: String
)

class PrestataireActivity : AppCompatActivity() {

    private val demandes = mutableListOf(
        DemandeReservation("🐕 Husky — Rocky", "Marie D.", "15/06 → 17/06", "40€"),
        DemandeReservation("🐈 Chat — Ficelle", "Jean P.", "20/06 → 25/06", "60€"),
        DemandeReservation("🐰 Lapin — Pompon", "Sophie L.", "18/06 → 19/06", "20€")
    )

    private val gardesAcceptees = mutableListOf(
        DemandeReservation("🐕 Labrador — Max", "Thomas R.", "01/06 → 03/06", "48€", "acceptee"),
        DemandeReservation("🐈 Siamois — Luna", "Camille B.", "05/06 → 07/06", "36€", "acceptee")
    )

    private val gardesTerminees = mutableListOf(
        GardeEffectuee("🐕 Beagle — Oscar", "Lucie M.", "10/05 → 15/05", "⭐ 5/5", "Super gardien, Rocky était ravi !"),
        GardeEffectuee("🐈 Maine Coon — Milo", "Pierre D.", "02/05 → 05/05", "⭐ 4/5", "Très professionnel, je recommande."),
        GardeEffectuee("🐕 Golden — Buddy", "Anna K.", "20/04 → 22/04", "⭐ 5/5", "Parfait ! Mon chien était très heureux.")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prestataire)
        supportActionBar?.hide()

        // Stats
        findViewById<TextView>(R.id.tvPrestataireNote).text   = "⭐ 4.8 / 5"
        findViewById<TextView>(R.id.tvPrestataireAvis).text   = "${gardesTerminees.size} avis"
        findViewById<TextView>(R.id.tvPrestataireGardes).text = "${gardesTerminees.size + gardesAcceptees.size} gardes"

        // Demandes en attente
        val rvDemandes = findViewById<RecyclerView>(R.id.rvDemandes)
        rvDemandes.layoutManager = LinearLayoutManager(this)
        rvDemandes.adapter = DemandeAdapter(demandes)

        // Navigation
        findViewById<Button>(R.id.btnVoirGardesAcceptees).setOnClickListener {
            showGardesAccepteesDialog()
        }
        findViewById<Button>(R.id.btnVoirAvis).setOnClickListener {
            showAvisDialog()
        }
        findViewById<Button>(R.id.btnPrestataireMessages).setOnClickListener {
            startActivity(Intent(this, MessagesActivity::class.java).apply {
                putExtra("GARDIEN_NAME", "Marie D. — Propriétaire")
            })
        }
        findViewById<Button>(R.id.btnPrestataireJournal).setOnClickListener {
            startActivity(Intent(this, JournalActivity::class.java))
        }
        findViewById<Button>(R.id.btnPrestataireProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        findViewById<Button>(R.id.btnPrestataireLogout).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }
    }

    private fun showGardesAccepteesDialog() {
        if (gardesAcceptees.isEmpty()) {
            android.app.AlertDialog.Builder(this)
                .setTitle("Gardes acceptées")
                .setMessage("Aucune garde acceptée pour le moment.")
                .setPositiveButton("OK", null).show()
            return
        }
        val sb = StringBuilder()
        gardesAcceptees.forEach { g ->
            sb.append("${g.animal}\n")
            sb.append("👤 ${g.proprietaire}\n")
            sb.append("📅 ${g.dates} — 💰 ${g.prix}\n")
            sb.append("─────────────────\n")
        }
        android.app.AlertDialog.Builder(this)
            .setTitle("✅ Mes gardes acceptées (${gardesAcceptees.size})")
            .setMessage(sb.toString())
            .setPositiveButton("Fermer", null)
            .show()
    }

    private fun showAvisDialog() {
        if (gardesTerminees.isEmpty()) {
            android.app.AlertDialog.Builder(this)
                .setTitle("Avis reçus")
                .setMessage("Aucun avis pour le moment.")
                .setPositiveButton("OK", null).show()
            return
        }
        val sb = StringBuilder()
        gardesTerminees.forEach { g ->
            sb.append("${g.animal} — ${g.proprietaire}\n")
            sb.append("${g.note}\n")
            sb.append("\"${g.commentaire}\"\n")
            sb.append("📅 ${g.dates}\n")
            sb.append("─────────────────\n")
        }
        android.app.AlertDialog.Builder(this)
            .setTitle("⭐ Avis et notes reçus")
            .setMessage(sb.toString())
            .setPositiveButton("Fermer", null)
            .show()
    }

    inner class DemandeAdapter(private val demandes: MutableList<DemandeReservation>) :
        RecyclerView.Adapter<DemandeAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvAnimal:       TextView = view.findViewById(R.id.tvDemandeAnimal)
            val tvProprietaire: TextView = view.findViewById(R.id.tvDemandeProprietaire)
            val tvDates:        TextView = view.findViewById(R.id.tvDemandeDates)
            val tvPrix:         TextView = view.findViewById(R.id.tvDemandePrix)
            val btnAccepter:    Button   = view.findViewById(R.id.btnAccepter)
            val btnRefuser:     Button   = view.findViewById(R.id.btnRefuser)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_demande, parent, false))

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val d = demandes[position]
            holder.tvAnimal.text       = d.animal
            holder.tvProprietaire.text = "👤 ${d.proprietaire}"
            holder.tvDates.text        = "📅 ${d.dates}"
            holder.tvPrix.text         = "💰 ${d.prix}"

            holder.btnAccepter.setOnClickListener {
                gardesAcceptees.add(d.copy(statut = "acceptee"))
                demandes.removeAt(position)
                notifyDataSetChanged()
                Toast.makeText(this@PrestataireActivity,
                    "✅ Garde acceptée !", Toast.LENGTH_SHORT).show()
                // Met à jour la stat
                findViewById<TextView>(R.id.tvPrestataireGardes).text =
                    "${gardesTerminees.size + gardesAcceptees.size} gardes"
            }
            holder.btnRefuser.setOnClickListener {
                demandes.removeAt(position)
                notifyDataSetChanged()
                Toast.makeText(this@PrestataireActivity,
                    "❌ Garde refusée.", Toast.LENGTH_SHORT).show()
            }
        }

        override fun getItemCount() = demandes.size
    }
}