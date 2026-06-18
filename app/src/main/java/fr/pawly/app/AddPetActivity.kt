package fr.pawly.app

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.pawly.app.data.AnimalRepository
import fr.pawly.app.data.AnimalSupabase
import kotlinx.coroutines.launch
import java.util.Calendar

class AddPetActivity : AppCompatActivity() {

    private var uriDocumentAnimal: Uri? = null
    private lateinit var adapter: AnimalAdapter
    private val animauxActuels = mutableListOf<AnimalSupabase>()

    private val docPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data?.data != null) {
            uriDocumentAnimal = result.data?.data
            val tvDoc = findViewById<TextView>(R.id.tvDocAnimalStatus)
            tvDoc.text = "✅ Document uploadé"
            tvDoc.setTextColor(getColor(R.color.pawly_teal))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_pet)

        supportActionBar?.title = "Mes animaux"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val etName        = findViewById<EditText>(R.id.etPetName)
        val spinnerType   = findViewById<Spinner>(R.id.spinnerPetType)
        val spinnerAge    = findViewById<Spinner>(R.id.spinnerPetAge)
        val btnSave       = findViewById<Button>(R.id.btnSavePet)
        val rvAnimaux     = findViewById<RecyclerView>(R.id.rvAnimaux)
        val tvEmpty       = findViewById<TextView>(R.id.tvNoAnimaux)
        val btnUploadDoc  = findViewById<Button>(R.id.btnUploadDocAnimal)
        val tvDocStatus   = findViewById<TextView>(R.id.tvDocAnimalStatus)

        spinnerType.adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_dropdown_item,
            arrayOf("Chien", "Chat", "Lapin", "Oiseau", "Rongeur", "Autre"))

        spinnerAge.adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_dropdown_item,
            (0..30).map { if (it <= 1) "$it an" else "$it ans" })

        btnUploadDoc.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
                putExtra(Intent.EXTRA_MIME_TYPES,
                    arrayOf("image/*", "application/pdf"))
            }
            docPickerLauncher.launch(Intent.createChooser(intent, "Carnet de santé / Carnet de vaccin"))
        }

        adapter = AnimalAdapter(animauxActuels) { idAnimal, position ->
            lifecycleScope.launch {
                AnimalRepository.supprimerAnimal(idAnimal).onSuccess {
                    animauxActuels.removeAt(position)
                    adapter.notifyItemRemoved(position)
                    rafraichirVisibilite(tvEmpty, rvAnimaux)
                    Toast.makeText(this@AddPetActivity, "Animal supprimé", Toast.LENGTH_SHORT).show()
                }.onFailure { e ->
                    Toast.makeText(this@AddPetActivity, "❌ Erreur suppression : ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
        rvAnimaux.layoutManager = LinearLayoutManager(this)
        rvAnimaux.adapter = adapter

        chargerAnimaux(tvEmpty, rvAnimaux)

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            if (name.isEmpty()) {
                etName.error = "Le nom est obligatoire"
                return@setOnClickListener
            }
            val type = spinnerType.selectedItem.toString()
            val ageAns = spinnerAge.selectedItemPosition // index = nombre d'années
            val anneeNaissance = Calendar.getInstance().get(Calendar.YEAR) - ageAns
            val dateNaissance = "$anneeNaissance-01-01"
            val hasDoc = uriDocumentAnimal != null

            btnSave.isEnabled = false
            lifecycleScope.launch {
                val result = AnimalRepository.ajouterAnimal(
                    nom = name,
                    typeAnimal = type,
                    dateNaissance = dateNaissance
                )
                btnSave.isEnabled = true

                result.onSuccess {
                    etName.text.clear()
                    uriDocumentAnimal = null
                    tvDocStatus.text = "Aucun document uploadé"
                    tvDocStatus.setTextColor(getColor(R.color.pawly_grey))

                    val docMsg = if (hasDoc) " avec carnet de santé ✅" else ""
                    Toast.makeText(this@AddPetActivity,
                        "🐾 $name ($type) ajouté$docMsg !",
                        Toast.LENGTH_SHORT).show()

                    chargerAnimaux(tvEmpty, rvAnimaux)
                }.onFailure { e ->
                    Toast.makeText(this@AddPetActivity,
                        "❌ Erreur lors de l'ajout : ${e.localizedMessage}",
                        Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun chargerAnimaux(tvEmpty: TextView, rvAnimaux: RecyclerView) {
        lifecycleScope.launch {
            AnimalRepository.getAnimaux().onSuccess { liste ->
                animauxActuels.clear()
                animauxActuels.addAll(liste)
                adapter.notifyDataSetChanged()
                rafraichirVisibilite(tvEmpty, rvAnimaux)
            }.onFailure { e ->
                Toast.makeText(this@AddPetActivity,
                    "❌ Impossible de charger les animaux : ${e.localizedMessage}",
                    Toast.LENGTH_LONG).show()
                rafraichirVisibilite(tvEmpty, rvAnimaux)
            }
        }
    }

    private fun rafraichirVisibilite(tvEmpty: TextView, rvAnimaux: RecyclerView) {
        if (animauxActuels.isEmpty()) {
            tvEmpty.visibility   = View.VISIBLE
            rvAnimaux.visibility = View.GONE
        } else {
            tvEmpty.visibility   = View.GONE
            rvAnimaux.visibility = View.VISIBLE
        }
    }

    class AnimalAdapter(
        private val animaux: MutableList<AnimalSupabase>,
        private val onDelete: (idAnimal: String, position: Int) -> Unit
    ) : RecyclerView.Adapter<AnimalAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvNom:    TextView = view.findViewById(R.id.tvAnimalNom)
            val tvInfo:   TextView = view.findViewById(R.id.tvAnimalInfo)
            val tvDoc:    TextView = view.findViewById(R.id.tvAnimalDoc)
            val btnDel:   Button   = view.findViewById(R.id.btnDeleteAnimal)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_animal, parent, false))

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val a = animaux[position]
            val emoji = when (a.typeAnimal) {
                "Chien"   -> "🐕"
                "Chat"    -> "🐈"
                "Lapin"   -> "🐰"
                "Oiseau"  -> "🐦"
                "Rongeur" -> "🐹"
                else      -> "🐾"
            }
            val age = calculerAgeDepuisDate(a.dateNaissance)
            holder.tvNom.text  = "$emoji  ${a.nom}"
            holder.tvInfo.text = "${a.typeAnimal} — $age"
            val hasDoc = !a.photoUrl.isNullOrEmpty() || !a.infosMedicales.isNullOrEmpty()
            holder.tvDoc.text  = if (hasDoc) "📄 Document santé ✅" else "📄 Pas de document"
            holder.tvDoc.setTextColor(
                if (hasDoc) 0xFF2E9E9E.toInt() else 0xFF8A9BA8.toInt()
            )
            holder.btnDel.setOnClickListener {
                android.app.AlertDialog.Builder(holder.itemView.context)
                    .setTitle("Supprimer ${a.nom} ?")
                    .setPositiveButton("Supprimer") { _, _ ->
                        onDelete(a.idAnimal, position)
                    }
                    .setNegativeButton("Annuler", null)
                    .show()
            }
        }

        private fun calculerAgeDepuisDate(dateNaissance: String?): String {
            if (dateNaissance.isNullOrEmpty()) return "âge inconnu"
            return try {
                val annee = dateNaissance.substring(0, 4).toInt()
                val anneeActuelle = Calendar.getInstance().get(Calendar.YEAR)
                val age = anneeActuelle - annee
                if (age <= 1) "$age an" else "$age ans"
            } catch (e: Exception) {
                "âge inconnu"
            }
        }

        override fun getItemCount() = animaux.size
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}