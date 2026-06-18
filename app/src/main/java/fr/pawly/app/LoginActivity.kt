package fr.pawly.app

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val emailInput     = findViewById<EditText>(R.id.etLoginEmail)
        val passwordInput  = findViewById<EditText>(R.id.etLoginPassword)
        val btnLogin       = findViewById<Button>(R.id.btnLogin)
        val tvGoToRegister = findViewById<TextView>(R.id.tvGoToRegister)
        val cbShowPassword = findViewById<CheckBox>(R.id.cbShowLoginPassword)

        cbShowPassword.setOnCheckedChangeListener { _, isChecked ->
            val cursor = passwordInput.selectionStart
            passwordInput.inputType = if (isChecked) {
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            passwordInput.setSelection(cursor)
        }

        btnLogin.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val pass  = passwordInput.text.toString().trim()

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailInput.error = "Email invalide"
                return@setOnClickListener
            }
            if (pass.isEmpty()) {
                passwordInput.error = "Veuillez entrer votre mot de passe"
                return@setOnClickListener
            }

            btnLogin.isEnabled = false
            btnLogin.text = "Connexion..."

            lifecycleScope.launch {
                try {
                    // 1. Connexion Supabase Auth
                    SupabaseManager.client.auth.signInWith(Email) {
                        this.email    = email
                        this.password = pass
                    }

                    // 2. Récupérer le profil pour connaître le rôle (table "utilisateur")
                    val userId = SupabaseManager.client.auth.currentUserOrNull()?.id ?: ""

                    val profil = SupabaseManager.client.postgrest["utilisateur"]
                        .select { filter { eq("id_user", userId) } }
                        .decodeSingleOrNull<UtilisateurDB>()

                    // 3. Remplir UserStore
                    if (profil != null) {
                        UserStore.prenom    = profil.prenom
                        UserStore.nom       = profil.nom
                        UserStore.email     = profil.email
                        UserStore.telephone = profil.telephone ?: ""
                        UserStore.adresse   = profil.adresse ?: ""
                        UserStore.bio       = profil.bio ?: ""
                        UserStore.role      = profil.role ?: "proprietaire"
                        UserStore.statut    = profil.statut ?: "Non vérifié"
                    }

                    // 4. Naviguer selon le rôle
                    val destination = when (profil?.role) {
                        "admin"       -> AdminActivity::class.java
                        "prestataire" -> PrestataireActivity::class.java
                        else          -> DashboardActivity::class.java
                    }

                    Toast.makeText(
                        this@LoginActivity,
                        "Connexion réussie ! 🐾",
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(Intent(this@LoginActivity, destination))
                    finish()

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(
                        this@LoginActivity,
                        "Erreur : ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                } finally {
                    btnLogin.isEnabled = true
                    btnLogin.text = "Se connecter"
                }
            }
        }

        tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}