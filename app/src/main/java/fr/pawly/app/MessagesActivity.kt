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

data class Message(
    val texte: String,
    val estMoi: Boolean,
    val heure: String
)

class MessagesActivity : AppCompatActivity() {

    private val reponsesAuto = listOf(
        "Super, j'ai bien noté votre demande ! 🐾",
        "Aucun problème, je m'en occupe dès maintenant.",
        "Votre animal est entre de bonnes mains, ne vous inquiétez pas 😊",
        "Je vous envoie une photo de Rocky dans quelques minutes !",
        "Il mange très bien et il est très joueur aujourd'hui 🎾",
        "Tout se passe très bien de notre côté !",
        "Je confirme les dates, pas de souci.",
        "Avez-vous des instructions particulières pour les repas ?",
        "Rocky a l'air d'apprécier sa nouvelle maison temporaire 🏠",
        "Je vous tiendrai informé via le journal de garde.",
        "Il a fait une longue promenade ce matin, il est bien fatigué 😴",
        "N'hésitez pas si vous avez des questions !",
        "Tout est sous contrôle, profitez de vos vacances ✈️",
        "Il s'entend très bien avec mes autres animaux 🐕🐈",
        "Je lui ai donné son médicament comme convenu 💊"
    )

    private val messages = mutableListOf(
        Message("Bonjour ! Je suis disponible pour garder votre animal 🐕",
            false, "10:30"),
        Message("Super ! Quelles sont vos disponibilités ?",
            true, "10:32"),
        Message("Je suis libre du lundi au vendredi, de 8h à 20h 😊",
            false, "10:35"),
        Message("Parfait, je vous envoie une demande de réservation",
            true, "10:36")
    )

    private var reponseIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages)

        val gardienName = intent.getStringExtra("GARDIEN_NAME") ?: "Gardien PAWLY"
        supportActionBar?.title = "💬 $gardienName"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val rv      = findViewById<RecyclerView>(R.id.rvMessages)
        val etMsg   = findViewById<EditText>(R.id.etMessageInput)
        val btnSend = findViewById<Button>(R.id.btnSendMessage)

        val adapter = MessagesAdapter(messages)
        rv.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        rv.adapter = adapter
        rv.scrollToPosition(messages.size - 1)

        btnSend.setOnClickListener {
            val text = etMsg.text.toString().trim()
            if (text.isEmpty()) {
                Toast.makeText(this, "Écrivez un message", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val heure = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            messages.add(Message(text, true, heure))
            adapter.notifyItemInserted(messages.size - 1)
            rv.scrollToPosition(messages.size - 1)
            etMsg.text.clear()

            // Réponse variée avec délai réaliste
            val delai = (1000L..3000L).random()
            rv.postDelayed({
                val reponse = reponsesAuto[reponseIndex % reponsesAuto.size]
                reponseIndex++
                val heureReponse = SimpleDateFormat("HH:mm", Locale.getDefault())
                    .format(Date())
                messages.add(Message(reponse, false, heureReponse))
                adapter.notifyItemInserted(messages.size - 1)
                rv.scrollToPosition(messages.size - 1)
            }, delai)
        }
    }

    class MessagesAdapter(private val messages: List<Message>) :
        RecyclerView.Adapter<MessagesAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val bubbleMoi:    View    = view.findViewById(R.id.bubbleMoi)
            val bubbleAutre:  View    = view.findViewById(R.id.bubbleAutre)
            val tvMsgMoi:     TextView = view.findViewById(R.id.tvMessageMoi)
            val tvHeureMoi:   TextView = view.findViewById(R.id.tvHeureMoi)
            val tvMsgAutre:   TextView = view.findViewById(R.id.tvMessageAutre)
            val tvHeureAutre: TextView = view.findViewById(R.id.tvHeureAutre)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message, parent, false))

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val msg = messages[position]
            if (msg.estMoi) {
                holder.bubbleMoi.visibility   = View.VISIBLE
                holder.bubbleAutre.visibility = View.GONE
                holder.tvMsgMoi.text   = msg.texte
                holder.tvHeureMoi.text = msg.heure
            } else {
                holder.bubbleMoi.visibility   = View.GONE
                holder.bubbleAutre.visibility = View.VISIBLE
                holder.tvMsgAutre.text   = msg.texte
                holder.tvHeureAutre.text = msg.heure
            }
        }

        override fun getItemCount() = messages.size
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}