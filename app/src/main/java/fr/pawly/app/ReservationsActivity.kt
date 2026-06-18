package fr.pawly.app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import fr.pawly.app.data.ReservationRepository
import kotlinx.coroutines.launch

class ReservationsActivity : AppCompatActivity() {

    // En écrivant le package complet ici, l'ordinateur ne peut plus se tromper de modèle !
    private var listeReservations = mutableListOf<fr.pawly.app.data.ReservationSupabase>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reservations)

        chargerDonnees()
    }

    private fun chargerDonnees() {
        lifecycleScope.launch {
            ReservationRepository.getReservations()
                .onSuccess { list ->
                    listeReservations.clear()

                    // On force le typage pour rassurer le compilateur Android Studio
                    listeReservations.addAll(list as List<fr.pawly.app.data.ReservationSupabase>)

                    // Si tu as un adapter configuré, tu peux décommenter la ligne suivante :
                    // adapter.notifyDataSetChanged()
                }
                .onFailure { e ->
                    Toast.makeText(this@ReservationsActivity, "Erreur de chargement: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    fun executerAnnulation(idReservation: String) {
        lifecycleScope.launch {
            ReservationRepository.demanderRemboursement(idReservation)
                .onSuccess {
                    Toast.makeText(this@ReservationsActivity, "Demande transmise !", Toast.LENGTH_LONG).show()
                    chargerDonnees()
                }
                .onFailure { e ->
                    Toast.makeText(this@ReservationsActivity, "Échec : ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}