package fr.pawly.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import fr.pawly.app.data.AnimalRepository
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val tvProfileName = findViewById<TextView>(R.id.tvProfileName)
        val tvStatAnimaux = findViewById<TextView>(R.id.tvStatAnimaux)
        val tvProfileEmail = findViewById<TextView>(R.id.tvProfileEmail)
        val tvProfilePhone = findViewById<TextView>(R.id.tvProfilePhone)
        val tvProfileAdresse = findViewById<TextView>(R.id.tvProfileAdresse)

        // Récupération des boutons d'action
        val btnMyPets = findViewById<Button>(R.id.btnMyPets)
        val btnMyReservations = findViewById<Button>(R.id.btnMyReservations)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        // Remplissage des informations de profil depuis le UserStore
        tvProfileName?.text = "${UserStore.prenom} ${UserStore.nom}"
        tvProfileEmail?.text = "✉️  ${UserStore.email}"
        tvProfilePhone?.text = "📞  ${UserStore.telephone.ifEmpty { "Non renseigné" }}"
        tvProfileAdresse?.text = "📍  ${UserStore.adresse.ifEmpty { "Non renseignée" }}"

        // Chargement dynamique du nombre d'animaux
        lifecycleScope.launch {
            AnimalRepository.getAnimaux()
                .onSuccess { liste ->
                    tvStatAnimaux?.text = "${liste.size}\nAnimaux"
                }
                .onFailure { e ->
                    Toast.makeText(this@ProfileActivity, "Erreur statistiques : ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
        }

        // --- GESTION DES CLICS SUR LES BOUTONS ---

        btnMyPets?.setOnClickListener {
            // Ouvre l'écran d'ajout/liste d'animaux
            startActivity(Intent(this, AddPetActivity::class.java))
        }

        btnMyReservations?.setOnClickListener {
            // Ouvre l'écran des réservations
            startActivity(Intent(this, ReservationsActivity::class.java))
        }

        btnLogout?.setOnClickListener {
            lifecycleScope.launch {
                try {
                    SupabaseManager.client.auth.signOut()
                    UserStore.currentUserId = ""

                    // Redirection vers l'écran de connexion
                    val intent = Intent(this@ProfileActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this@ProfileActivity, "Erreur déconnexion", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}