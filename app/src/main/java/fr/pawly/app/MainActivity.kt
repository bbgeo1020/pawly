package fr.pawly.app

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.util.Patterns
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private var uriCarteIdentite: Uri? = null
    private var uriAssurance: Uri?     = null

    private var pendingFirstName = ""
    private var pendingLastName  = ""
    private var pendingEmail     = ""
    private var pendingPhone     = ""
    private var pendingAdresse   = ""
    private var pendingPassword  = ""
    private var pendingRole      = "proprietaire"

    private val carteIdLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data?.data != null) {
            uriCarteIdentite = result.data?.data
            findViewById<TextView>(R.id.tvCarteStatus).apply {
                text = "✅ Pièce d'identité uploadée"
                setTextColor(getColor(R.color.pawly_teal))
            }
        }
    }

    private val assuranceLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data?.data != null) {
            uriAssurance = result.data?.data
            findViewById<TextView>(R.id.tvAssuranceStatus).apply {
                text = "✅ Attestation uploadée"
                setTextColor(getColor(R.color.pawly_teal))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        val etFirstName          = findViewById<EditText>(R.id.etFirstName)
        val etLastName           = findViewById<EditText>(R.id.etLastName)
        val emailInput           = findViewById<EditText>(R.id.etEmail)
        val phoneInput           = findViewById<EditText>(R.id.etPhone)
        val etAdresse            = findViewById<EditText>(R.id.etAdresse)
        val rgRole               = findViewById<RadioGroup>(R.id.rgRole)
        val passwordInput        = findViewById<EditText>(R.id.etPassword)
        val confirmPasswordInput = findViewById<EditText>(R.id.etConfirmPassword)
        val btnRegister          = findViewById<Button>(R.id.btnRegister)
        val cbShowPassword       = findViewById<CheckBox>(R.id.cbShowPassword)
        val tvBackToLogin        = findViewById<TextView>(R.id.tvBackToLogin)
        val btnUploadCarte       = findViewById<Button>(R.id.btnUploadCarte)
        val btnUploadAssurance   = findViewById<Button>(R.id.btnUploadAssurance)

        tvBackToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        phoneInput.filters = arrayOf(InputFilter.LengthFilter(9))

        cbShowPassword.setOnCheckedChangeListener { _, isChecked ->
            val cursor = passwordInput.selectionStart
            passwordInput.inputType = if (isChecked)
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            passwordInput.setSelection(cursor)
        }

        btnUploadCarte.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "*/*" }
            carteIdLauncher.launch(Intent.createChooser(intent, "Pièce d'identité"))
        }

        btnUploadAssurance.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "*/*" }
            assuranceLauncher.launch(Intent.createChooser(intent, "Attestation d'assurance"))
        }

        btnRegister.setOnClickListener {
            val firstName       = etFirstName.text.toString().trim()
            val lastName        = etLastName.text.toString().trim()
            val email           = emailInput.text.toString().trim()
            val phone           = phoneInput.text.toString().trim()
            val adresse         = etAdresse.text.toString().trim()
            val password        = passwordInput.text.toString().trim()
            val confirmPassword = confirmPasswordInput.text.toString().trim()

            if (firstName.isEmpty() || lastName.isEmpty() || adresse.isEmpty()) {
                Toast.makeText(this, "⚠️ Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailInput.error = "Email invalide"; return@setOnClickListener
            }
            if (password != confirmPassword || password.length < 12) {
                passwordInput.error = "Mot de passe non valide (12 chars min)"; return@setOnClickListener
            }

            pendingFirstName = firstName
            pendingLastName  = lastName
            pendingEmail     = email
            pendingPhone     = phone
            pendingAdresse   = adresse
            pendingPassword  = password
            pendingRole      = when (rgRole.checkedRadioButtonId) {
                R.id.rbProprietaire -> "proprietaire"
                R.id.rbPrestataire  -> "prestataire"
                R.id.rbAdmin        -> "admin"
                else                -> "proprietaire"
            }

            showOtpDialog()
        }
    }

    private fun showOtpDialog() {
        android.app.AlertDialog.Builder(this)
            .setTitle("Vérification email 📧")
            .setMessage("Cliquez sur Valider pour créer votre profil sur Supabase.")
            .setPositiveButton("Valider") { _, _ ->
                lifecycleScope.launch {
                    try {
                        SupabaseManager.client.auth.signUpWith(Email) {
                            this.email    = pendingEmail
                            this.password = pendingPassword
                        }

                        val userId = SupabaseManager.client.auth.currentUserOrNull()?.id ?: ""

                        if (userId.isNotEmpty()) {
                            SupabaseManager.client.postgrest["utilisateur"].insert(
                                UtilisateurDB(
                                    idUser    = userId,
                                    prenom    = pendingFirstName,
                                    nom       = pendingLastName,
                                    email     = pendingEmail,
                                    telephone = pendingPhone,
                                    adresse   = pendingAdresse,
                                    role      = pendingRole,
                                    statut    = "En vérification"
                                )
                            )
                            UserStore.currentUserId = userId
                        }

                        UserStore.prenom  = pendingFirstName
                        UserStore.nom     = pendingLastName
                        UserStore.email   = pendingEmail
                        UserStore.role    = pendingRole
                        UserStore.statut  = "En vérification"

                        val destination = when (pendingRole) {
                            "admin"       -> AdminActivity::class.java
                            "prestataire" -> PrestataireActivity::class.java
                            else          -> DashboardActivity::class.java
                        }
                        startActivity(Intent(this@MainActivity, destination))
                        finish()

                    } catch (e: Exception) {
                        Toast.makeText(this@MainActivity, "❌ Erreur : ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
}