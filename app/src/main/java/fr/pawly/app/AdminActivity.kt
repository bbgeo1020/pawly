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

data class Signalement(
    val titre: String,
    val detail: String,
    val type: String,
    var statut: String = "en_attente"
)

class AdminActivity : AppCompatActivity() {

    private val signalements = mutableListOf(
        Signalement("Profil suspect", "Thomas D. — Faux profil signalé", "profil"),
        Signalement("Avis inapproprié", "Lucas M. — Contenu offensant", "avis"),
        Signalement("Litige réservation #1247", "Remboursement refusé", "litige"),
        Signalement("Photo non conforme", "Sarah K. — Photo inappropriée", "photo")
    )

    private lateinit var sigAdapter: SignalementAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        supportActionBar?.hide()

        // Stats cliquables
        val stats = listOf(
            Triple(R.id.tvAdminUsers, "247 utilisateurs",
                "📊 Répartition :\n• 198 propriétaires\n• 43 gardiens\n• 6 admins\n\nInscriptions ce mois : +12"),
            Triple(R.id.tvAdminReservations, "89 réservations",
                "📅 Ce mois :\n• 62 confirmées\n• 18 terminées\n• 9 annulées\n\nTaux de completion : 87%"),
            Triple(R.id.tvAdminSignalements, "${signalements.size} signalements",
                "🚨 En attente : ${signalements.count { it.statut == "en_attente" }}\n✅ Traités : ${signalements.count { it.statut == "traite" }}"),
            Triple(R.id.tvAdminRevenue, "4 820€",
                "💰 Revenus ce mois\n\n• Commission plateforme : 10%\n• Total transactions : 48 200€\n• Paiements validés : 89")
        )

        stats.forEach { (id, titre, detail) ->
            val tv = findViewById<TextView>(id)
            tv.setOnClickListener {
                android.app.AlertDialog.Builder(this)
                    .setTitle(titre)
                    .setMessage(detail)
                    .setPositiveButton("Fermer", null)
                    .show()
            }
        }

        // Signalements avec traitement
        sigAdapter = SignalementAdapter(signalements)
        val rv = findViewById<RecyclerView>(R.id.rvSignalements)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = sigAdapter

        // Bouton gestion utilisateurs
        findViewById<Button>(R.id.btnAdminUsers).setOnClickListener {
            android.app.AlertDialog.Builder(this)
                .setTitle("👥 Gestion des utilisateurs")
                .setMessage(
                    "Utilisateurs actifs : 247\n\n" +
                            "Actions disponibles :\n" +
                            "• Suspendre un compte\n" +
                            "• Vérifier un profil gardien\n" +
                            "• Bannir un utilisateur\n" +
                            "• Envoyer une notification\n\n" +
                            "(Fonctionnalité connectée au backend en Sprint 3)"
                )
                .setPositiveButton("OK", null)
                .show()
        }

        // Bouton statistiques détaillées
        findViewById<Button>(R.id.btnAdminStats).setOnClickListener {
            android.app.AlertDialog.Builder(this)
                .setTitle("📈 Statistiques détaillées")
                .setMessage(
                    "Période : Juin 2026\n\n" +
                            "🐕 Gardes effectuées : 23\n" +
                            "🐈 Type d'animal le plus gardé : Chien (68%)\n" +
                            "⭐ Note moyenne plateforme : 4.7/5\n" +
                            "📍 Ville la plus active : Paris\n" +
                            "💰 Revenu moyen par garde : 42€\n" +
                            "📱 Utilisateurs mobile : 78%"
                )
                .setPositiveButton("OK", null)
                .show()
        }

        // Déconnexion
        findViewById<Button>(R.id.btnAdminLogout).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }
    }

    inner class SignalementAdapter(private val items: MutableList<Signalement>) :
        RecyclerView.Adapter<SignalementAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvTitre:  TextView = view.findViewById(R.id.tvSignalTitre)
            val tvDetail: TextView = view.findViewById(R.id.tvSignalDetail)
            val tvStatut: TextView = view.findViewById(R.id.tvSignalStatut)
            val btnTraiter: Button = view.findViewById(R.id.btnTraiterSignal)
            val btnIgnorer: Button = view.findViewById(R.id.btnIgnorerSignal)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_signalement, parent, false))

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val s = items[position]
            holder.tvTitre.text  = "⚠️ ${s.titre}"
            holder.tvDetail.text = s.detail
            holder.tvStatut.text = if (s.statut == "traite") "✅ Traité" else "⏳ En attente"
            holder.tvStatut.setTextColor(
                if (s.statut == "traite")
                    getColor(android.R.color.holo_green_dark)
                else
                    getColor(android.R.color.holo_orange_dark)
            )

            val estTraite = s.statut == "traite"
            holder.btnTraiter.isEnabled = !estTraite
            holder.btnIgnorer.isEnabled = !estTraite

            holder.btnTraiter.setOnClickListener {
                android.app.AlertDialog.Builder(holder.itemView.context)
                    .setTitle("Traiter ce signalement")
                    .setMessage("Action à effectuer sur : ${s.titre}\n\nSanction :")
                    .setPositiveButton("Avertissement") { _, _ ->
                        s.statut = "traite"
                        notifyItemChanged(position)
                        Toast.makeText(this@AdminActivity,
                            "✅ Avertissement envoyé", Toast.LENGTH_SHORT).show()
                    }
                    .setNeutralButton("Suspension") { _, _ ->
                        s.statut = "traite"
                        notifyItemChanged(position)
                        Toast.makeText(this@AdminActivity,
                            "🔴 Compte suspendu", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Annuler", null)
                    .show()
            }

            holder.btnIgnorer.setOnClickListener {
                s.statut = "traite"
                notifyItemChanged(position)
                Toast.makeText(this@AdminActivity,
                    "Signalement ignoré", Toast.LENGTH_SHORT).show()
            }
        }

        override fun getItemCount() = items.size
    }
}