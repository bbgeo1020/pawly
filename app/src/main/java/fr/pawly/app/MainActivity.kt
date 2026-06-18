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

    // Ces variables sont remplies au moment de la validation du formulaire
    // puis utilisées dans showOtpDialog()
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
            findViewById<Button>(R.id.btnUploadCarte)
                .backgroundTintList = android.content.res.ColorStateList.valueOf(
                getColor(R.color.pawly_teal))
        }
    }

    private val assuranceLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data?.data != null) {
            uriAssurance = result.data?.data
            findViewById<TextView>(R.id.tvAssuranceStatus).apply {
                text = "✅ Attestation uploadée"
                setTextColor(getColor(R.color.pawly_teal))
            }
            findViewById<Button>(R.id.btnUploadAssurance)
                .backgroundTintList = android.content.res.ColorStateList.valueOf(
                getColor(R.color.pawly_teal))
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
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "application/pdf"))
            }
            carteIdLauncher.launch(Intent.createChooser(intent, "Sélectionner votre pièce d'identité"))
        }

        btnUploadAssurance.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "application/pdf"))
            }
            assuranceLauncher.launch(Intent.createChooser(intent, "Sélectionner votre attestation"))
        }

        btnRegister.setOnClickListener {
            val firstName       = etFirstName.text.toString().trim()
            val lastName        = etLastName.text.toString().trim()
            val email           = emailInput.text.toString().trim()
            val phone           = phoneInput.text.toString().trim()
            val adresse         = etAdresse.text.toString().trim()
            val password        = passwordInput.text.toString().trim()
            val confirmPassword = confirmPasswordInput.text.toString().trim()

            // Reset erreurs
            etFirstName.error = null; etLastName.error = null
            emailInput.error  = null; phoneInput.error = null
            etAdresse.error   = null; passwordInput.error = null
            confirmPasswordInput.error = null

            if (firstName.isEmpty())  { etFirstName.error = "Prénom obligatoire"; return@setOnClickListener }
            if (lastName.isEmpty())   { etLastName.error  = "Nom obligatoire";    return@setOnClickListener }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailInput.error = "Email invalide"; return@setOnClickListener
            }
            if (phone.length != 9)    { phoneInput.error  = "9 chiffres requis";  return@setOnClickListener }
            if (adresse.isEmpty())    { etAdresse.error   = "Adresse obligatoire";return@setOnClickListener }
            if (password.length < 12) { passwordInput.error = "12 caractères minimum"; return@setOnClickListener }
            if (!password.any { it.isLetter() }) {
                passwordInput.error = "Doit contenir au moins une lettre"; return@setOnClickListener
            }
            if (password != confirmPassword) {
                confirmPasswordInput.error = "Mots de passe différents"; return@setOnClickListener
            }
            if (uriCarteIdentite == null) {
                Toast.makeText(this, "⚠️ Veuillez uploader votre pièce d'identité", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Sauvegarder pour usage dans showOtpDialog
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
        val roleLabel = when (pendingRole) {
            "prestataire" -> "Gardien / Prestataire"
            "admin"       -> "Administrateur PAWLY"
            else          -> "Propriétaire d'animal"
        }

        // Supabase envoie un vrai email de confirmation — on informe l'utilisateur
        android.app.AlertDialog.Builder(this)
            .setTitle("Vérification email 📧")
            .setMessage(
                "Un email de confirmation a été envoyé à $pendingEmail.\n\n" +
                        "Cliquez sur le lien dans l'email pour activer votre compte, " +
                        "puis revenez vous connecter."
            )
            .setPositiveButton("Créer mon compte") { _, _ ->

                lifecycleScope.launch {
                    try {
                        // 1. Créer le compte dans Supabase Auth
                        SupabaseManager.client.auth.signUpWith(Email) {
                            this.email    = pendingEmail
                            this.password = pendingPassword
                        }

                        // 2. Récupérer l'id généré
                        val userId = SupabaseManager.client.auth
                            .currentUserOrNull()?.id ?: ""

                        // 3. Insérer dans la table "utilisateur" (table réelle confirmée)
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
                                    statut    = if (uriCarteIdentite != null)
                                        "En vérification"
                                    else "Non vérifié"
                                )
                            )
                        }

                        // 4. Remplir UserStore
                        UserStore.prenom  = pendingFirstName
                        UserStore.nom     = pendingLastName
                        UserStore.email   = pendingEmail
                        UserStore.role    = roleLabel
                        UserStore.statut  = if (uriCarteIdentite != null)
                            "En vérification"
                        else "Non vérifié"

                        Toast.makeText(
                            this@MainActivity,
                            "✅ Bienvenue $pendingFirstName ! Compte $roleLabel créé.",
                            Toast.LENGTH_LONG
                        ).show()

                        // 5. Naviguer selon le rôle
                        val destination = when (pendingRole) {
                            "admin"       -> AdminActivity::class.java
                            "prestataire" -> PrestataireActivity::class.java
                            else          -> AddPetActivity::class.java
                        }
                        startActivity(Intent(this@MainActivity, destination))
                        finish()

                    } catch (e: Exception) {
                        Toast.makeText(
                            this@MainActivity,
                            "❌ Erreur inscription : ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            .setNegativeButton("Annuler") { dialog, _ -> dialog.cancel() }
            .show()
    }
}