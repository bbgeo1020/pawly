package fr.pawly.app

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import fr.pawly.app.data.AnimalRepository
import fr.pawly.app.data.AnimalSupabase
import kotlinx.coroutines.launch

class AddPetActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_pet)

        val etPetName = findViewById<EditText>(R.id.etPetName)
        val spinnerPetType = findViewById<Spinner>(R.id.spinnerPetType)
        val spinnerPetAge = findViewById<Spinner>(R.id.spinnerPetAge)
        val btnSavePet = findViewById<Button>(R.id.btnSavePet)

        // 🟢 Configurer la liste des types d'animaux
        val typesAnimaux = arrayOf("Chien", "Chat", "Lapin", "Rongeur", "Oiseau", "Autre")
        val adapterType = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, typesAnimaux)
        spinnerPetType?.adapter = adapterType

        // 🟢 Configurer la liste des âges
        val agesAnimaux = arrayOf("Moins d'un an", "1 an", "2 ans", "3 ans", "4 ans", "5 ans", "6 ans", "7 ans", "8 ans +")
        val adapterAge = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, agesAnimaux)
        spinnerPetAge?.adapter = adapterAge

        btnSavePet?.setOnClickListener {
            val nom = etPetName?.text?.toString()?.trim() ?: ""
            val type = spinnerPetType?.selectedItem?.toString() ?: "Chien"
            val age = spinnerPetAge?.selectedItem?.toString() ?: "1 an"

            if (nom.isEmpty()) {
                Toast.makeText(this, "Veuillez entrer le nom de l'animal", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val nouvelAnimal = AnimalSupabase(
                    idUser = UserStore.currentUserId,
                    nom = nom,
                    typeAnimal = type,
                    race = age // On peut stocker momentanément l'âge dans le champ race pour tester !
                )

                AnimalRepository.ajouterAnimal(nouvelAnimal)
                    .onSuccess {
                        Toast.makeText(this@AddPetActivity, "Animal ajouté avec succès !", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .onFailure { e ->
                        Toast.makeText(this@AddPetActivity, "Erreur : ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
            }
        }
    }
}