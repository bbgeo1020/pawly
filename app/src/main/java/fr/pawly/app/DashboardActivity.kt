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

data class Offer(
    val title: String,
    val description: String,
    val price: String,
    val type: String
)

class DashboardActivity : AppCompatActivity() {

    private val allOffers = listOf(
        Offer("Garde Chien — Paris 11e",
            "Chiot Husky, 2 jours. Maison avec jardin.", "20€ / jour", "Chien"),
        Offer("Garde Chat — Lyon",
            "Chat Ficelle, nourrissage une fois par jour.", "12€ / jour", "Chat"),
        Offer("Garde Lapin — Bordeaux",
            "Lapin Pompon cherche famille d'accueil.", "10€ / jour", "Lapin"),
        Offer("Promenade Chien — Lille",
            "Boxer, 1h tous les midis.", "15€ / promenade", "Chien"),
        Offer("Garde Chat — Marseille",
            "2 chats tigrés, 1 semaine. Nourriture fournie.", "14€ / jour", "Chat"),
        Offer("Garde Oiseau — Nice",
            "Perruche Kiwi pendant vacances.", "8€ / jour", "Oiseau"),
        Offer("Garde Chien — Toulouse",
            "Golden Retriever, câlin et joueur.", "22€ / jour", "Chien")
    )

    private lateinit var offersAdapter: OffersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        supportActionBar?.hide()

        findViewById<Button>(R.id.btnOpenMap).setOnClickListener {
            startActivity(Intent(this, MapsActivity::class.java))
        }

        findViewById<Button>(R.id.btnProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        offersAdapter = OffersAdapter(allOffers.toMutableList())
        findViewById<RecyclerView>(R.id.rvOffers).apply {
            layoutManager = LinearLayoutManager(this@DashboardActivity)
            adapter = offersAdapter
        }

        val filterOptions = arrayOf(
            "Tous les animaux", "Chien", "Chat", "Lapin", "Oiseau"
        )
        val spinner = findViewById<Spinner>(R.id.spinnerFilter)
        spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            filterOptions
        )
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, pos: Int, id: Long
            ) {
                val selected = filterOptions[pos]
                val filtered = if (selected == "Tous les animaux")
                    allOffers
                else
                    allOffers.filter { it.type == selected }
                offersAdapter.updateList(filtered)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    class OffersAdapter(private val offers: MutableList<Offer>) :
        RecyclerView.Adapter<OffersAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvTitle: TextView = view.findViewById(R.id.tvOfferTitle)
            val tvDesc: TextView  = view.findViewById(R.id.tvOfferDescription)
            val tvPrice: TextView = view.findViewById(R.id.tvOfferPrice)
            val tvType: TextView  = view.findViewById(R.id.tvOfferType)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_offer, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val offer = offers[position]
            holder.tvTitle.text = offer.title
            holder.tvDesc.text  = offer.description
            holder.tvPrice.text = offer.price
            holder.tvType.text  = when (offer.type) {
                "Chien"  -> "🐕 Chien"
                "Chat"   -> "🐈 Chat"
                "Lapin"  -> "🐰 Lapin"
                "Oiseau" -> "🐦 Oiseau"
                else     -> offer.type
            }
            holder.itemView.setOnClickListener {
                val intent = Intent(
                    holder.itemView.context,
                    BookingActivity::class.java
                ).apply {
                    putExtra("OFFER_TITLE", offer.title)
                    putExtra("OFFER_DESC",  offer.description)
                    putExtra("OFFER_PRICE", offer.price)
                }
                holder.itemView.context.startActivity(intent)
            }
        }

        override fun getItemCount() = offers.size

        fun updateList(newList: List<Offer>) {
            offers.clear()
            offers.addAll(newList)
            notifyDataSetChanged()
        }
    }
}