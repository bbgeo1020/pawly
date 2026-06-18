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
            "maintenant"
        )
    )

    private var animalDetecte: String = ""

    // Base de connaissances par animal
    private val conseilsAnimaux = mapOf(
        "chien" to mapOf(
            "garde" to listOf(
                "🐕 Pour un chien, privilégiez un gardien avec jardin ou proche d'un parc.",
                "Assurez-vous que le gardien peut faire 2-3 promenades par jour minimum.",
                "Vérifiez que le gardien accepte votre race spécifique — certains refusent les grandes races.",
                "Demandez au gardien s'il a de l'expérience avec des chiots si votre chien est jeune."
            ),
            "sante" to listOf(
                "Vérifiez que les vaccinations (rage, CHPPI) sont à jour avant de confier votre chien.",
                "Emportez sa nourriture habituelle pour éviter les troubles digestifs.",
                "Laissez un jouet avec votre odeur pour rassurer votre chien.",
                "Donnez les instructions précises pour les médicaments si votre chien en prend."
            ),
            "conseils" to listOf(
                "Faites une courte visite de présentation avant la garde.",
                "Prévoyez son carnet de santé et les contacts du vétérinaire habituel.",
                "Informez le gardien des habitudes de votre chien (heures de repas, jouets préférés)."
            )
        ),
        "chat" to mapOf(
            "garde" to listOf(
                "🐈 Les chats préfèrent souvent rester dans leur environnement habituel.",
                "Un gardien qui vient à domicile est souvent mieux supporté par les chats.",
                "Vérifiez que le gardien a de l'expérience avec les chats solitaires ou anxieux.",
                "Assurez-vous que la litière sera nettoyée au minimum une fois par jour."
            ),
            "sante" to listOf(
                "Les chats stressent facilement lors des changements d'environnement.",
                "Laissez ses affaires (griffoir, couverture) pour qu'il ait ses repères olfactifs.",
                "Vérifiez les vaccinations (typhus, leucose) avant la garde.",
                "Un Feliway diffuseur peut aider contre le stress du déplacement."
            ),
            "conseils" to listOf(
                "Laissez un vêtement avec votre odeur pour rassurer votre chat.",
                "Prévenez le gardien si votre chat est exclusivement d'intérieur.",
                "Informez-le des zones où votre chat aime dormir et se cacher."
            )
        ),
        "lapin" to mapOf(
            "garde" to listOf(
                "🐰 Assurez-vous que le gardien accepte les NAC (nouveaux animaux de compagnie).",
                "Le lapin a besoin d'un espace suffisant — demandez la taille disponible.",
                "Vérifiez que le gardien connaît le régime alimentaire du lapin (foin, légumes).",
                "Le lapin est sensible aux courants d'air et aux températures extrêmes."
            ),
            "sante" to listOf(
                "Apportez assez de foin Timothy — c'est la base de l'alimentation du lapin.",
                "Évitez les légumes trop sucrés (carottes en quantité limitée).",
                "Le lapin doit avoir accès à l'eau fraîche en permanence.",
                "Surveillez les signes de GI stasis (arrêt du transit) — c'est une urgence vétérinaire."
            ),
            "conseils" to listOf(
                "Laissez les instructions précises sur les portions et les horaires.",
                "Apportez sa cage et ses accessoires habituels.",
                "Le lapin peut être très stressé par le transport — minimisez les trajets."
            )
        ),
        "oiseau" to mapOf(
            "garde" to listOf(
                "🐦 Assurez-vous que le gardien a de l'expérience avec les oiseaux.",
                "L'oiseau a besoin de stimulation — demandez si le gardien interagit avec lui.",
                "Vérifiez que la pièce est à l'abri des courants d'air et du soleil direct.",
                "Les oiseaux peuvent être très bruyants — prévenez le gardien !"
            ),
            "sante" to listOf(
                "Les oiseaux sont très sensibles aux fumées et produits ménagers — attention !",
                "Apportez sa nourriture habituelle (graines, granulés, fruits frais).",
                "La cage doit être nettoyée régulièrement.",
                "Les oiseaux ont besoin de 10-12h de sommeil dans l'obscurité."
            ),
            "conseils" to listOf(
                "Laissez ses jouets habituels dans la cage.",
                "Informez le gardien des mots et interactions que votre oiseau apprécie.",
                "Couvrez la cage la nuit avec un tissu sombre."
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

        // Suggestions rapides
        val suggestions = listOf("Mon chien", "Mon chat", "Mon lapin", "Mon oiseau",
            "Conseils garde", "Conseils santé")
        val chipGroup = findViewById<LinearLayout>(R.id.chipGroup)
        suggestions.forEach { sugg ->
            val btn = Button(this).apply {
                text = sugg
                textSize = 12f
                setTextColor(getColor(R.color.pawly_teal))
                backgroundTintList = android.content.res.ColorStateList.valueOf(
                    getColor(R.color.pawly_light_grey))
                val p = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(4, 0, 4, 0) }
                layoutParams = p
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

        // Génération réponse IA locale
        rv.postDelayed({
            val reponse = genererReponseIA(text.lowercase())
            val heureIA = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            messages.add(ChatMessage(reponse, true, heureIA))
            adapter.notifyItemInserted(messages.size - 1)
            rv.scrollToPosition(messages.size - 1)
        }, 800)
    }

    private fun genererReponseIA(input: String): String {
        // Détection de l'animal
        when {
            input.contains("chien") || input.contains("dog") ||
                    input.contains("toutou") -> animalDetecte = "chien"
            input.contains("chat") || input.contains("félin") ||
                    input.contains("minou") -> animalDetecte = "chat"
            input.contains("lapin") -> animalDetecte = "lapin"
            input.contains("oiseau") || input.contains("perroquet") ||
                    input.contains("perruche") -> animalDetecte = "oiseau"
        }

        // Réponse selon le contexte
        return when {
            // Salutations
            input.contains("bonjour") || input.contains("salut") || input.contains("hello") ->
                "Bonjour ! 🐾 Je suis votre assistant PAWLY.\n\nQuel type d'animal avez-vous ? (chien, chat, lapin, oiseau...)"

            // Demande sur un animal spécifique
            animalDetecte.isNotEmpty() && (input.contains("garde") || input.contains("gardien") || input.contains("garder")) -> {
                val conseils = conseilsAnimaux[animalDetecte]?.get("garde") ?: listOf()
                "🐾 Conseils de garde pour votre $animalDetecte :\n\n" +
                        conseils.joinToString("\n\n") + "\n\n" +
                        "Voulez-vous aussi des conseils santé pour votre $animalDetecte ?"
            }

            input.contains("santé") || input.contains("sante") || input.contains("médic") || input.contains("maladie") -> {
                if (animalDetecte.isEmpty()) {
                    "Pour des conseils santé personnalisés, dites-moi d'abord quel animal vous avez ! 🐾"
                } else {
                    val conseils = conseilsAnimaux[animalDetecte]?.get("sante") ?: listOf()
                    "🏥 Conseils santé pour votre $animalDetecte :\n\n" +
                            conseils.joinToString("\n\n")
                }
            }

            input.contains("conseil") || input.contains("astuce") || input.contains("aide") -> {
                if (animalDetecte.isEmpty()) {
                    "Bien sûr ! Quel animal avez-vous ? Je vous donnerai des conseils personnalisés. 🐾"
                } else {
                    val conseils = conseilsAnimaux[animalDetecte]?.get("conseils") ?: listOf()
                    "💡 Conseils pratiques pour votre $animalDetecte :\n\n" +
                            conseils.joinToString("\n\n")
                }
            }

            // Questions sur PAWLY
            input.contains("réservation") || input.contains("reserver") || input.contains("réserver") ->
                "Pour réserver une garde sur PAWLY :\n\n" +
                        "1. Allez dans le Dashboard\n" +
                        "2. Parcourez les offres disponibles\n" +
                        "3. Cliquez sur une offre qui vous convient\n" +
                        "4. Choisissez vos dates\n" +
                        "5. Confirmez le paiement\n\n" +
                        "Vous pouvez suivre vos réservations dans votre profil ! 📅"

            input.contains("prix") || input.contains("tarif") || input.contains("coût") || input.contains("cout") ->
                "💰 Les tarifs sur PAWLY varient selon :\n\n" +
                        "• Type de garde (domicile/pension)\n" +
                        "• Type d'animal\n" +
                        "• Durée de la garde\n" +
                        "• Expérience du gardien\n\n" +
                        "En moyenne : 10€ à 25€ par jour.\n" +
                        "PAWLY prend une commission de 10% pour assurer la qualité du service."

            input.contains("urgence") || input.contains("vétérinaire") || input.contains("veterinaire") ->
                "🚨 En cas d'urgence vétérinaire :\n\n" +
                        "1. Allez dans votre Profil\n" +
                        "2. Cliquez sur 'Vétérinaires à proximité'\n" +
                        "3. Appelez directement le vétérinaire\n\n" +
                        "Les vétérinaires avec 🚨 sont disponibles 24h/24 !"

            input.contains("merci") || input.contains("super") || input.contains("parfait") ->
                "Avec plaisir ! 🐾 N'hésitez pas si vous avez d'autres questions.\n\nBonne garde à votre animal ! ❤️"

            // Animal détecté mais question générale
            animalDetecte.isNotEmpty() ->
                "Je vois que vous avez un $animalDetecte ! 🐾\n\n" +
                        "Je peux vous aider avec :\n" +
                        "• Des conseils de garde\n" +
                        "• Des conseils santé\n" +
                        "• Des astuces pratiques\n\n" +
                        "Que souhaitez-vous savoir ?"

            // Question non comprise
            else ->
                "Je n'ai pas bien compris votre question. 🤔\n\n" +
                        "Je peux vous aider sur :\n" +
                        "• Les conseils de garde (dites-moi votre animal)\n" +
                        "• Les conseils santé\n" +
                        "• Le fonctionnement de PAWLY\n" +
                        "• Les tarifs et réservations\n\n" +
                        "Quel animal avez-vous ?"
        }
    }

    class ChatAdapter(private val messages: List<ChatMessage>) :
        RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val bubbleIA:    View    = view.findViewById(R.id.bubbleIA)
            val bubbleUser:  View    = view.findViewById(R.id.bubbleUser)
            val tvIA:        TextView = view.findViewById(R.id.tvMsgIA)
            val tvUser:      TextView = view.findViewById(R.id.tvMsgUser)
            val tvHeureIA:   TextView = view.findViewById(R.id.tvHeureIA)
            val tvHeureUser: TextView = view.findViewById(R.id.tvHeureUser)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_chat, parent, false))

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val msg = messages[position]
            if (msg.estIA) {
                holder.bubbleIA.visibility   = View.VISIBLE
                holder.bubbleUser.visibility = View.GONE
                holder.tvIA.text   = msg.texte
                holder.tvHeureIA.text = msg.heure
            } else {
                holder.bubbleIA.visibility   = View.GONE
                holder.bubbleUser.visibility = View.VISIBLE
                holder.tvUser.text  = msg.texte
                holder.tvHeureUser.text = msg.heure
            }
        }

        override fun getItemCount() = messages.size
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}