package fr.pawly.app

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import fr.pawly.app.data.ReservationRepository
import kotlinx.coroutines.launch
import java.util.Calendar

class BookingActivity : AppCompatActivity() {

    private var dateDebut: String = ""
    private var dateFin: String   = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking)

        supportActionBar?.title = "Réservation"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val tvTitle   = findViewById<TextView>(R.id.tvBookingTitle)
        val tvDesc    = findViewById<TextView>(R.id.tvBookingDescription)
        val tvPrice   = findViewById<TextView>(R.id.tvBookingPrice)
        val btnStart  = findViewById<Button>(R.id.btnSelectDate)
        val btnEnd    = findViewById<Button>(R.id.btnSelectEndDate)
        val btnPay    = findViewById<Button>(R.id.btnConfirmPayment)
        val etCard    = findViewById<EditText>(R.id.etCardNumber)
        val etExpiry  = findViewById<EditText>(R.id.etExpiry)
        val etCvv     = findViewById<EditText>(R.id.etCvv)
        val tvSummary = findViewById<TextView>(R.id.tvSummary)

        val offerTitle = intent.getStringExtra("OFFER_TITLE") ?: "Offre de garde"
        val offerPrice = intent.getStringExtra("OFFER_PRICE") ?: "Tarif non spécifié"
        // Type de garde déduit du titre de l'offre (pour la colonne type_garde)
        val typeGarde = intent.getStringExtra("OFFER_TYPE") ?: offerTitle

        tvTitle.text = offerTitle
        tvDesc.text  = intent.getStringExtra("OFFER_DESC") ?: ""
        tvPrice.text = offerPrice

        btnStart.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d ->
                // Format ISO yyyy-MM-dd attendu par Postgres (colonnes date)
                dateDebut = String.format("%04d-%02d-%02d", y, m + 1, d)
                btnStart.text = "📅 Début : $d/${m + 1}/$y"
                mettreAJourRecap(tvSummary, offerPrice)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnEnd.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d ->
                dateFin = String.format("%04d-%02d-%02d", y, m + 1, d)
                btnEnd.text = "📅 Fin : $d/${m + 1}/$y"
                mettreAJourRecap(tvSummary, offerPrice)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnPay.setOnClickListener {
            val card   = etCard.text.toString().trim()
            val expiry = etExpiry.text.toString().trim()
            val cvv    = etCvv.text.toString().trim()

            if (dateDebut.isEmpty() || dateFin.isEmpty()) {
                Toast.makeText(this,
                    "⚠️ Choisissez les dates de début et de fin",
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (card.length != 16) {
                etCard.error = "16 chiffres requis"
                return@setOnClickListener
            }
            if (expiry.length != 4) {
                etExpiry.error = "Format MMAA requis"
                return@setOnClickListener
            }
            if (cvv.length != 3) {
                etCvv.error = "3 chiffres requis"
                return@setOnClickListener
            }

            android.app.AlertDialog.Builder(this)
                .setTitle("Confirmer le paiement")
                .setMessage(
                    "Offre : $offerTitle\n" +
                            "Du : $dateDebut\n" +
                            "Au : $dateFin\n" +
                            "Carte : **** **** **** ${card.takeLast(4)}\n\n" +
                            "Confirmer le paiement ?"
                )
                .setPositiveButton("Confirmer") { _, _ ->
                    btnPay.isEnabled = false

                    // Extrait le prix numérique depuis une chaîne comme "20€ / jour"
                    val prixNumerique = offerPrice
                        .replace(Regex("[^0-9.,]"), "")
                        .replace(",", ".")
                        .toDoubleOrNull()

                    lifecycleScope.launch {
                        val result = ReservationRepository.ajouterReservation(
                            idPrestataire = null, // pas encore d'ID prestataire transmis par le Dashboard
                            dateDebut = dateDebut,
                            dateFin = dateFin,
                            typeGarde = typeGarde,
                            prixTotal = prixNumerique
                        )

                        btnPay.isEnabled = true

                        result.onSuccess {
                            Toast.makeText(this@BookingActivity,
                                "✅ Réservation confirmée et envoyée !",
                                Toast.LENGTH_LONG).show()

                            android.app.AlertDialog.Builder(this@BookingActivity)
                                .setTitle("Évaluer le gardien")
                                .setMessage("Voulez-vous noter votre gardien maintenant ?")
                                .setPositiveButton("Oui, noter !") { _, _ ->
                                    startActivity(Intent(this@BookingActivity, RatingActivity::class.java).apply {
                                        putExtra("GARDIEN_NAME", "votre gardien")
                                    })
                                    finish()
                                }
                                .setNegativeButton("Plus tard") { _, _ -> finish() }
                                .show()
                        }.onFailure { e ->
                            Toast.makeText(this@BookingActivity,
                                "❌ Erreur lors de la réservation : ${e.localizedMessage}",
                                Toast.LENGTH_LONG).show()
                        }
                    }
                }
                .setNegativeButton("Annuler") { dialog, _ -> dialog.cancel() }
                .show()
        }
    }

    private fun mettreAJourRecap(tvSummary: TextView, price: String) {
        if (dateDebut.isNotEmpty() && dateFin.isNotEmpty()) {
            tvSummary.text =
                "Période : du $dateDebut au $dateFin\n" +
                        "Tarif : $price\n" +
                        "⚠️ Paiement simulé — Stripe Sprint 3"
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}