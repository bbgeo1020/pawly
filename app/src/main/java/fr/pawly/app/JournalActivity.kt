package fr.pawly.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.pawly.app.data.JournalGardeRepository
import fr.pawly.app.data.JournalSupabase
import kotlinx.coroutines.launch

class JournalActivity : AppCompatActivity() {

    private lateinit var journalAdapter: JournalAdapter
    private val entreesActuelles = mutableListOf<JournalSupabase>()

    private val suggestionsIA = mapOf(
        "repas" to listOf(
            "💡 Essayez une gamelle anti-glouton pour ralentir la prise alimentaire",
            "💡 Les tapis de léchage peuvent enrichir le moment du repas",
            "💡 Une fontaine à eau encourage votre animal à s'hydrater davantage"
        ),
        "promenade" to listOf(
            "💡 Un harnais anti-traction améliore le confort lors des promenades",
            "💡 Les chaussettes de protection pour les pattes en hiver sont très utiles"
        ),
        "jeux" to listOf(
            "💡 Les jouets à mâcher Kong remplissables de friandises sont excellents",
            "💡 Une balle lanceur automatique pour les chiens actifs"
        ),
        "sieste" to listOf(
            "💡 Un lit orthopédique améliore le confort des grandes races",
            "💡 Un coin calme dédié réduit l'anxiété de votre animal"
        ),
        "toilettage" to listOf(
            "💡 Une brosse démêlante Furminator réduit les poils perdus de 90%"
        ),
        "note" to listOf(
            "💡 Un distributeur automatique de croquettes facilite les horaires de repas",
            "💡 Une caméra connectée vous permet de surveiller votre animal à distance"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_journal)

        supportActionBar?.title = "E-Journal de garde 📓"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        journalAdapter = JournalAdapter(entreesActuelles)
        val rv = findViewById<RecyclerView>(R.id.rvJournal)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = journalAdapter

        chargerEntrees()

        findViewById<Button>(R.id.btnAddEntry)?.setOnClickListener {
            showAddEntryDialog()
        }
    }

    private fun chargerEntrees() {
        lifecycleScope.launch {
            JournalGardeRepository.getEntries().onSuccess { liste ->
                entreesActuelles.clear()
                entreesActuelles.addAll(liste)
                journalAdapter.notifyDataSetChanged()
            }.onFailure { e ->
                Toast.makeText(this@JournalActivity,
                    "❌ Impossible de charger le journal : ${e.localizedMessage}",
                    Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showAddEntryDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_entry, null)
        val etTitle    = dialogView.findViewById<EditText>(R.id.etEntryTitle)
        val etDesc     = dialogView.findViewById<EditText>(R.id.etEntryDesc)
        val spinner    = dialogView.findViewById<Spinner>(R.id.spinnerEmoji)

        val emojis = arrayOf(
            "🍽️ Repas", "🚶 Promenade", "😴 Sieste",
            "🎾 Jeux", "🛁 Toilettage", "💊 Médicament",
            "📸 Photo", "📝 Note générale"
        )
        spinner.adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_dropdown_item, emojis)

        android.app.AlertDialog.Builder(this)
            .setTitle("Ajouter une entrée au journal")
            .setView(dialogView)
            .setPositiveButton("Ajouter + Suggestion IA") { _, _ ->
                val title = etTitle.text.toString().trim()
                val desc  = etDesc.text.toString().trim()
                if (title.isEmpty() || desc.isEmpty()) {
                    Toast.makeText(this, "⚠️ Remplissez tous les champs",
                        Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val selectedFull = emojis[spinner.selectedItemPosition]
                val typeActiviteLabel = selectedFull.split(" ").getOrElse(1) { "Note" }
                val typeContenu = typeActiviteLabel.lowercase()
                    .replace("générale", "")
                    .trim()
                    .ifEmpty { "note" }

                val suggestionsList = suggestionsIA[typeContenu] ?: suggestionsIA["note"]!!
                val suggestion = suggestionsList.random()

                lifecycleScope.launch {
                    val result = JournalGardeRepository.ajouterEntree(
                        titre = title,
                        contenu = desc,
                        typeContenu = typeContenu
                    )

                    result.onSuccess {
                        chargerEntrees()

                        android.app.AlertDialog.Builder(this@JournalActivity)
                            .setTitle("💡 Suggestion IA pour votre animal")
                            .setMessage(
                                "Entrée ajoutée : \"$title\"\n\n" +
                                        "Suggestion accessoire / environnement :\n\n$suggestion\n\n" +
                                        "Retrouvez d'autres conseils dans l'Assistant IA ! 🤖"
                            )
                            .setPositiveButton("Super, merci !", null)
                            .show()

                        Toast.makeText(this@JournalActivity, "✅ Entrée ajoutée !", Toast.LENGTH_SHORT).show()
                    }.onFailure { e ->
                        Toast.makeText(this@JournalActivity,
                            "❌ Erreur lors de l'ajout : ${e.localizedMessage}",
                            Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    inner class JournalAdapter(private val entries: MutableList<JournalSupabase>) :
        RecyclerView.Adapter<JournalAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvEmoji:      TextView? = view.findViewById(R.id.tvEntryEmoji)
            val tvTitle:      TextView? = view.findViewById(R.id.tvEntryTitle)
            val tvDesc:       TextView? = view.findViewById(R.id.tvEntryDesc)
            val tvHeure:      TextView? = view.findViewById(R.id.tvEntryHeure)
            val tvSuggestion: TextView? = view.findViewById(R.id.tvEntrySuggestion)
            val btnDelete:    Button?   = view.findViewById(R.id.btnDeleteEntry)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_journal, parent, false))

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val e = entries[position]
            holder.tvEmoji?.text = when (e.typeContenu) {
                "repas"      -> "🍽️"
                "promenade"  -> "🚶"
                "sieste"     -> "😴"
                "jeux"       -> "🎾"
                "toilettage" -> "🛁"
                "médicament", "medicament" -> "💊"
                "photo"      -> "📸"
                else         -> "📝"
            }
            holder.tvTitle?.text = e.titre
            holder.tvDesc?.text  = e.contenu
            holder.tvHeure?.text = formaterHeure(e.datePublication)

            holder.tvSuggestion?.visibility = View.GONE

            holder.btnDelete?.setOnClickListener {
                android.app.AlertDialog.Builder(holder.itemView.context)
                    .setTitle("Supprimer cette entrée ?")
                    .setPositiveButton("Supprimer") { _, _ ->
                        lifecycleScope.launch {
                            JournalGardeRepository.supprimerEntree(e.idArticle).onSuccess {
                                entries.removeAt(position)
                                notifyItemRemoved(position)
                                Toast.makeText(holder.itemView.context,
                                    "Entrée supprimée", Toast.LENGTH_SHORT).show()
                            }.onFailure { ex ->
                                Toast.makeText(holder.itemView.context,
                                    "❌ Erreur : ${ex.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                    .setNegativeButton("Annuler", null)
                    .show()
            }
        }

        override fun getItemCount() = entries.size

        private fun formaterHeure(datePublication: String?): String {
            if (datePublication.isNullOrEmpty()) return ""
            return try {
                val timePart = datePublication
                    .substringAfter("T", datePublication)
                    .substringAfter(" ")
                    .take(5)
                timePart
            } catch (e: Exception) {
                ""
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}