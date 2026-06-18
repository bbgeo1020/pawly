package fr.pawly.app

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import fr.pawly.app.data.AnimalRepository
import fr.pawly.app.data.ReservationRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        supportActionBar?.hide()

        chargerProfilDepuisSupabase()

        findViewById<Button>(R.id.btnEditProfile).setOnClickListener {
            showEditProfileDialog()
        }

        // --- Navigation ---
        findViewById<Button>(R.id.btnVerifDocuments).setOnClickListener { showDocumentsVerifDialog() }
        findViewById<Button>(R.id.btnMyPets).setOnClickListener { startActivity(Intent(this, AddPetActivity::class.java)) }
        findViewById<Button>(R.id.btnMyReservations).setOnClickListener { startActivity(Intent(this, ReservationsActivity::class.java)) }
        findViewById<Button>(R.id.btnJournal).setOnClickListener { startActivity(Intent(this, JournalActivity::class.java)) }
        findViewById<Button>(R.id.btnVets).setOnClickListener { startActivity(Intent(this, VetActivity::class.java)) }
        findViewById<Button>(R.id.btnMessages).setOnClickListener {
            startActivity(Intent(this, MessagesActivity::class.java).apply { putExtra("GARDIEN_NAME", "Thomas — Dog-sitter") })
        }
        findViewById<Button>(R.id.btnChatbot).setOnClickListener { startActivity(Intent(this, ChatbotActivity::class.java)) }

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            android.app.AlertDialog.Builder(this)
                .setTitle("Déconnexion")
                .setMessage("Voulez-vous vraiment vous déconnecter ?")
                .setPositiveButton("Oui") { _, _ ->
                    lifecycleScope.launch {
                        try {
                            SupabaseManager.client.auth.signOut()
                        } catch (e: Exception) {}
                    }
                    startActivity(Intent(this, LoginActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK })
                }
                .setNegativeButton("Non") { d, _ -> d.cancel() }
                .show()
        }
    }

    private fun chargerProfilDepuisSupabase() {
        val currentUid = try { SupabaseManager.client.auth.currentUserOrNull()?.id } catch (e: Exception) { null }

        if (currentUid == null) {
            afficherProfil()
            chargerStats()
            return
        }

        lifecycleScope.launch {
            try {
                val userProfile = SupabaseManager.client.postgrest["utilisateur"]
                    .select { filter { eq("id_user", currentUid) } }
                    .decodeSingle<UtilisateurDB>()

                UserStore.prenom    = userProfile.prenom
                UserStore.nom       = userProfile.nom
                UserStore.email     = userProfile.email
                UserStore.telephone = userProfile.telephone ?: ""
                UserStore.adresse   = userProfile.adresse ?: ""
                UserStore.bio       = userProfile.bio ?: ""
                UserStore.statut    = userProfile.statut ?: "Non vérifié"
                UserStore.role      = userProfile.role ?: "proprietaire"

                afficherProfil()
            } catch (e: Exception) {
                Toast.makeText(this@ProfileActivity, "Créez votre profil en cliquant sur Modifier !", Toast.LENGTH_LONG).show()
                afficherProfil()
            }
            chargerStats()
        }
    }

    // ── Charge les vraies statistiques (animaux + réservations) depuis Supabase ──
    private fun chargerStats() {
        lifecycleScope.launch {
            AnimalRepository.getAnimaux().onSuccess { animaux ->
                UserStore.nbAnimaux = animaux.size
                findViewById<TextView>(R.id.tvStatAnimaux).text = "${UserStore.nbAnimaux}\nAnimaux"
            }.onFailure {
                findViewById<TextView>(R.id.tvStatAnimaux).text = "0\nAnimaux"
            }

            ReservationRepository.getReservations().onSuccess { reservations ->
                UserStore.nbGardes = reservations.size
                findViewById<TextView>(R.id.tvStatGardes).text = "${UserStore.nbGardes}\nRéservations"
            }.onFailure {
                findViewById<TextView>(R.id.tvStatGardes).text = "0\nRéservations"
            }
        }
    }

    private fun afficherProfil() {
        findViewById<TextView>(R.id.tvProfileName).text = "${UserStore.prenom} ${UserStore.nom}"
        findViewById<TextView>(R.id.tvProfileRole).text = UserStore.role
        findViewById<TextView>(R.id.tvProfileBio).text  = UserStore.bio
        findViewById<TextView>(R.id.tvProfileEmail).text = "📧  ${UserStore.email}"
        findViewById<TextView>(R.id.tvProfilePhone).text = "📱  ${UserStore.telephone}"
        findViewById<TextView>(R.id.tvProfileAdresse).text = "📍  ${UserStore.adresse}"

        val tvStatut = findViewById<TextView>(R.id.tvStatutVerification)
        tvStatut.text = when (UserStore.statut) {
            "Vérifié" -> "✅ Compte vérifié"
            "En vérification" -> "⏳ Documents en cours de vérification"
            else -> "❌ Non vérifié — Uploadez vos documents"
        }
        tvStatut.setTextColor(when (UserStore.statut) {
            "Vérifié"         -> getColor(R.color.pawly_teal)
            "En vérification" -> getColor(R.color.pawly_orange)
            else              -> getColor(R.color.pawly_red)
        })

        // Valeurs par défaut affichées immédiatement ; chargerStats() les mettra à jour
        findViewById<TextView>(R.id.tvStatAnimaux).text = "${UserStore.nbAnimaux}\nAnimaux"
        findViewById<TextView>(R.id.tvStatGardes).text = "${UserStore.nbGardes}\nRéservations"
        findViewById<TextView>(R.id.tvStatAvis).text = "${UserStore.nbAvis}\nAvis"
    }

    private fun showEditProfileDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        view.findViewById<EditText>(R.id.etEditPrenom).setText(UserStore.prenom)
        view.findViewById<EditText>(R.id.etEditNom).setText(UserStore.nom)
        view.findViewById<EditText>(R.id.etEditEmail).setText(UserStore.email)
        view.findViewById<EditText>(R.id.etEditTelephone).setText(UserStore.telephone)
        view.findViewById<EditText>(R.id.etEditAdresse).setText(UserStore.adresse)
        view.findViewById<EditText>(R.id.etEditBio).setText(UserStore.bio)

        android.app.AlertDialog.Builder(this)
            .setTitle("✏️ Modifier mon profil")
            .setView(view)
            .setPositiveButton("Enregistrer") { _, _ ->
                val prenom    = view.findViewById<EditText>(R.id.etEditPrenom).text.toString().trim()
                val nom       = view.findViewById<EditText>(R.id.etEditNom).text.toString().trim()
                val email     = view.findViewById<EditText>(R.id.etEditEmail).text.toString().trim()
                val tel       = view.findViewById<EditText>(R.id.etEditTelephone).text.toString().trim()
                val adresse   = view.findViewById<EditText>(R.id.etEditAdresse).text.toString().trim()
                val bio       = view.findViewById<EditText>(R.id.etEditBio).text.toString().trim()

                if (prenom.isNotEmpty()) UserStore.prenom    = prenom
                if (nom.isNotEmpty())    UserStore.nom        = nom
                if (email.isNotEmpty())  UserStore.email      = email
                if (tel.isNotEmpty())    UserStore.telephone  = tel
                if (adresse.isNotEmpty()) UserStore.adresse   = adresse
                if (bio.isNotEmpty())    UserStore.bio        = bio

                val currentUid = try { SupabaseManager.client.auth.currentUserOrNull()?.id } catch (e: Exception) { null }
                if (currentUid != null) {
                    val updatedUser = UtilisateurDB(
                        idUser = currentUid,
                        nom = UserStore.nom,
                        prenom = UserStore.prenom,
                        email = UserStore.email,
                        telephone = UserStore.telephone,
                        adresse = UserStore.adresse,
                        bio = UserStore.bio,
                        role = UserStore.role,
                        statut = UserStore.statut
                    )

                    lifecycleScope.launch {
                        try {
                            SupabaseManager.client.postgrest["utilisateur"].upsert(updatedUser)
                            Toast.makeText(this@ProfileActivity, "✅ Sauvegardé sur Supabase !", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(this@ProfileActivity, "❌ Erreur Cloud : ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                afficherProfil()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun showDocumentsVerifDialog() {
        android.app.AlertDialog.Builder(this)
            .setTitle("📁 Vérification de documents")
            .setMessage("Documents uploadés permettent d'obtenir le badge Certifié.\n\nStatut actuel : ${UserStore.statut}")
            .setPositiveButton("Simuler vérification") { _, _ ->
                UserStore.statut = "Vérifié"
                afficherProfil()
            }
            .setNegativeButton("Fermer", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        chargerProfilDepuisSupabase()
    }
}