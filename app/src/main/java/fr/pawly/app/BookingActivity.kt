package fr.pawly.app

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import fr.pawly.app.data.ReservationRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Calendar

class BookingActivity : AppCompatActivity() {

    private var dateDebut: String = ""
    private var dateFin: String   = ""
    private var prixFinalCalculer: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking)

        supportActionBar?.title = "Finaliser ma Garde"
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

        val offerTitle = intent.getStringExtra("OFFER_TITLE") ?: "Garde d'animaux"
        val offerPrice = intent.getStringExtra("OFFER_PRICE") ?: "15.00"
        val prestataireId = intent.getStringExtra("PRESTATAIRE_ID")

        tvTitle.text = offerTitle
        tvDesc.text  = intent.getStringExtra("OFFER_DESC") ?: "Garde Pawly en toute confiance."
        tvPrice.text = "$offerPrice € / jour"

        btnStart.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d ->
                dateDebut = String.format("%04d-%02d-%02d", y, m + 1, d)
                btnStart.text = "Début : $d/${m + 1}/$y"
                mettreAJourRecap(tvSummary, offerPrice)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnEnd.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d ->
                dateFin = String.format("%04d-%02d-%02d", y, m + 1, d)
                btnEnd.text = "Fin : $d/${m + 1}/$y"
                mettreAJourRecap(tvSummary, offerPrice)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnPay.setOnClickListener {
            val card = etCard.text.toString().trim()
            val expiry = etExpiry.text.toString().trim()
            val cvv = etCvv.text.toString().trim()

            if (dateDebut.isEmpty() || dateFin.isEmpty()) {
                Toast.makeText(this, "⚠️ Veuillez sélectionner la période de garde", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (card.length != 16 || expiry.length != 4 || cvv.length != 3) {
                Toast.makeText(this, "⚠️ Informations bancaires invalides", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            android.app.AlertDialog.Builder(this)
                .setTitle("💳 Paiement Sécurisé Stripe")
                .setMessage("Montant total facturé : $prixFinalCalculer €\n\nConfirmer le débit de votre carte ?")
                .setPositiveButton("Procéder au paiement") { _, _ ->
                    btnPay.isEnabled = false
                    lifecycleScope.launch {
                        val result = ReservationRepository.ajouterReservation(
                            idPrestataire = prestataireId,
                            dateDebut = dateDebut,
                            dateFin = dateFin,
                            typeGarde = offerTitle,
                            prixTotal = prixFinalCalculer
                        )
                        btnPay.isEnabled = true
                        result.onSuccess {
                            Toast.makeText(this@BookingActivity, "✅ Réservation enregistrée et payée !", Toast.LENGTH_LONG).show()
                            finish()
                        }.onFailure { e ->
                            Toast.makeText(this@BookingActivity, "❌ Erreur : ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                .setNegativeButton("Annuler", null)
                .show()
        }
    }

    private fun mettreAJourRecap(tvSummary: TextView, price: String) {
        if (dateDebut.isNotEmpty() && dateFin.isNotEmpty()) {
            try {
                val start = LocalDate.parse(dateDebut)
                val end = LocalDate.parse(dateFin)
                val jours = ChronoUnit.DAYS.between(start, end).coerceAtLeast(1)

                val prixJour = price.replace(Regex("[^0-9.,]"), "").replace(",", ".").toDoubleOrNull() ?: 0.0
                val sousTotal = prixJour * jours
                val frais = sousTotal * 0.05 // 5% de frais
                prixFinalCalculer = sousTotal + frais

                tvSummary.text = "Résumé :\n• Garde sur $jours jour(s)\n• Prestation : $sousTotal €\n• Frais de service : $frais €\n• Total payé : $prixFinalCalculer €"
            } catch (e: Exception) {
                tvSummary.text = "Période sélectionnée invalide."
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}