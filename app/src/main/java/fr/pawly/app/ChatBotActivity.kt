package fr.pawly.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

data class ChatMessage(
    val texte: String,
    val estIA: Boolean,
    val heure: String
)

class ChatbotActivity : AppCompatActivity() {

    private val messages = mutableListOf(
        ChatMessage(
            "Bonjour ! 🐾 Je suis l'assistant PAWLY.\n\nDites-moi quel type d'animal vous avez et je vous donnerai des conseils personnalisés pour sa garde et son entretien !",
            true,
            "10:00"
        )
    )

    private var animalDetecte: String = "chien"

    private val conseilsAnimaux = mapOf(
        "chien" to mapOf(
            "garde" to listOf(
                "🐕 Pour un chien, privilégiez un gardien avec jardin ou proche d'un parc.",
                "Assurez-vous que le gardien peut faire 2-3 promenades par jour minimum."
            ),
            "sante" to listOf(
                "Vérifiez que les vaccinations (rage, CHPPI) sont à jour avant de confier votre chien.",
                "Emportez sa nourriture habituelle pour éviter les troubles digestifs."
            )
        ),
        "chat" to mapOf(
            "garde" to listOf(
                "🐈 Les chats préfèrent souvent rester dans leur environnement habituel.",
                "Un gardien qui vient à domicile est souvent mieux supporté par les chats."
            ),
            "sante" to listOf(
                "Les chats stressent facilement lors des changements d'environnement.",
                "Un diffuseur de phéromones apaisantes peut aider à gérer l'anxiété du transport."
            )
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatbot)

        supportActionBar?.title = "🤖 Assistant PAWLY IA"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val rv      = findViewById<RecyclerView>(R.id.rvChat)
        val etInput = findViewById<EditText>(R.id.etChatInput)
        val btnSend = findViewById<Button>(R.id.btnChatSend)

        val adapter = ChatAdapter(messages)
        rv.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        rv.adapter = adapter

        val chipGroup = findViewById<LinearLayout>(R.id.chipGroup)
        val suggestions = listOf("Mon chien", "Mon chat", "Conseils garde", "Conseils santé")

        suggestions.forEach { sugg ->
            val btn = Button(this).apply {
                text = sugg
                textSize = 12f
                setTextColor(getColor(R.color.pawly_teal))
                backgroundTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.pawly_light_grey))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(4, 0, 4, 0) }

                setOnClickListener { envoyerMessage(sugg, adapter, rv) }
            }
            chipGroup.addView(btn)
        }

        btnSend.setOnClickListener {
            val text = etInput.text.toString().trim()
            if (text.isNotEmpty()) {
                envoyerMessage(text, adapter, rv)
                etInput.text.clear()
            }
        }
    }

    private fun envoyerMessage(text: String, adapter: ChatAdapter, rv: RecyclerView) {
        val heure = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        messages.add(ChatMessage(text, false, heure))
        adapter.notifyItemInserted(messages.size - 1)
        rv.scrollToPosition(messages.size - 1)

        rv.postDelayed({
            val reponse = genererReponseIA(text.lowercase())
            messages.add(ChatMessage(reponse, true, heure))
            adapter.notifyItemInserted(messages.size - 1)
            rv.scrollToPosition(messages.size - 1)
        }, 800)
    }

    private fun genererReponseIA(userInput: String): String {
        if (userInput.contains("chien")) animalDetecte = "chien"
        if (userInput.contains("chat")) animalDetecte = "chat"

        val categorie = when {
            userInput.contains("garde") -> "garde"
            userInput.contains("sant") -> "sante"
            else -> null
        }

        return if (categorie != null) {
            conseilsAnimaux[animalDetecte]?.get(categorie)?.random()
                ?: "Je n'ai pas trouvé de conseil spécifique."
        } else {
            "Bien reçu pour votre $animalDetecte ! Demandez-moi des 'conseils garde' ou 'conseils santé' 🐾."
        }
    }

    // ── ADAPTER CORRIGÉ POUR TON LAYOUT UNIQUE (R.layout.item_chat) ──
    class ChatAdapter(private val msgList: List<ChatMessage>) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            // Vue de la bulle IA
            val bubbleIA: View = view.findViewById(R.id.bubbleIA)
            val tvMsgIA: TextView = view.findViewById(R.id.tvMsgIA)
            val tvHeureIA: TextView = view.findViewById(R.id.tvHeureIA)

            // Vue de la bulle Utilisateur
            val bubbleUser: View = view.findViewById(R.id.bubbleUser)
            val tvMsgUser: TextView = view.findViewById(R.id.tvMsgUser)
            val tvHeureUser: TextView = view.findViewById(R.id.tvHeureUser)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            // Remplace R.layout.item_chat par le nom de ton fichier XML s'il est différent
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val msg = msgList[position]

            if (msg.estIA) {
                // On bascule les visibilités pour la bulle IA
                holder.bubbleIA.visibility = View.VISIBLE
                holder.bubbleUser.visibility = View.GONE

                holder.tvMsgIA.text = msg.texte
                holder.tvHeureIA.text = msg.heure
            } else {
                // On bascule les visibilités pour la bulle Utilisateur
                holder.bubbleIA.visibility = View.GONE
                holder.bubbleUser.visibility = View.VISIBLE

                holder.tvMsgUser.text = msg.texte
                holder.tvHeureUser.text = msg.heure
            }
        }

        override fun getItemCount() = msgList.size
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}