package fr.pawly.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.text.InputType
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

        val etEmail = findViewById<EditText>(R.id.etLoginEmail)
        val etPassword = findViewById<EditText>(R.id.etLoginPassword)
        val cbShowLoginPassword = findViewById<CheckBox>(R.id.cbShowLoginPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvGoToRegister = findViewById<TextView>(R.id.tvGoToRegister)

        tvGoToRegister?.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        cbShowLoginPassword?.setOnCheckedChangeListener { _, isChecked ->
            val cursor = etPassword.selectionStart
            etPassword.inputType = if (isChecked) {
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            etPassword.setSelection(cursor)
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir les champs", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    SupabaseManager.client.auth.signInWith(Email) {
                        this.email = email
                        this.password = password
                    }

                    val userId = SupabaseManager.client.auth.currentUserOrNull()?.id ?: ""
                    if (userId.isNotEmpty()) {
                        UserStore.currentUserId = userId

                        // CORRIGÉ : "Utilisateur" avec un U majuscule pour matcher ta table Supabase
                        val userProfile = SupabaseManager.client.postgrest["Utilisateur"]
                            .select { filter { eq("id_user", userId) } }
                            .decodeSingle<UtilisateurDB>()

                        UserStore.prenom = userProfile.prenom
                        UserStore.nom = userProfile.nom
                        UserStore.email = userProfile.email
                        UserStore.telephone = userProfile.telephone ?: ""
                        UserStore.adresse = userProfile.adresse ?: ""
                        UserStore.role = userProfile.role ?: "proprietaire"
                        UserStore.statut = userProfile.statut ?: "Non vérifié"
                        UserStore.bio = userProfile.bio ?: ""

                        val intent = when (UserStore.role) {
                            "admin" -> Intent(this@LoginActivity, AdminActivity::class.java)
                            "prestataire" -> Intent(this@LoginActivity, PrestataireActivity::class.java)
                            else -> Intent(this@LoginActivity, DashboardActivity::class.java)
                        }
                        startActivity(intent)
                        finish()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@LoginActivity, "Erreur : ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}