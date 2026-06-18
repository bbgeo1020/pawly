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

        val btnMyPets = findViewById<Button>(R.id.btnMyPets)
        val btnMyReservations = findViewById<Button>(R.id.btnMyReservations)
        val btnJournal = findViewById<Button>(R.id.btnJournal)
        val btnVets = findViewById<Button>(R.id.btnVets)
        val btnMessages = findViewById<Button>(R.id.btnMessages)
        val btnChatbot = findViewById<Button>(R.id.btnChatbot)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        tvProfileName?.text = "${UserStore.prenom} ${UserStore.nom}"
        tvProfileEmail?.text = "✉️  ${UserStore.email}"
        tvProfilePhone?.text = "📞  ${UserStore.telephone.ifEmpty { "Non renseigné" }}"
        tvProfileAdresse?.text = "📍  ${UserStore.adresse.ifEmpty { "Non renseignée" }}"

        lifecycleScope.launch {
            AnimalRepository.getAnimaux()
                .onSuccess { liste ->
                    tvStatAnimaux?.text = "${liste.size}\nAnimaux"
                }
                .onFailure {
                    tvStatAnimaux?.text = "0\nAnimaux"
                }
        }

        btnMyPets?.setOnClickListener {
            startActivity(Intent(this, AddPetActivity::class.java))
        }

        btnMyReservations?.setOnClickListener {
            startActivity(Intent(this, ReservationsActivity::class.java))
        }

        btnJournal?.setOnClickListener {
            try {
                startActivity(Intent(this, JournalActivity::class.java))
            } catch (e: Exception) {
                Toast.makeText(this, "📓 Écran Journal inaccessible", Toast.LENGTH_SHORT).show()
            }
        }

        btnVets?.setOnClickListener {
            startActivity(Intent(this, VetActivity::class.java))
        }

        btnChatbot?.setOnClickListener {
            startActivity(Intent(this, ChatbotActivity::class.java))
        }

        btnMessages?.setOnClickListener {
            try {
                startActivity(Intent(this, Class.forName("fr.pawly.app.MessagesActivity")))
            } catch (e: Exception) {
                Toast.makeText(this, "💬 L'écran de Messagerie locale est indisponible", Toast.LENGTH_SHORT).show()
            }
        }

        btnLogout?.setOnClickListener {
            lifecycleScope.launch {
                try {
                    SupabaseManager.client.auth.signOut()
                    UserStore.currentUserId = ""
                    val intent = Intent(this@ProfileActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this@ProfileActivity, "Erreur lors de la déconnexion", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}