package fr.pawly.app

import android.os.Bundle
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
        val btnSavePet = findViewById<Button>(R.id.btnSavePet)

        btnSavePet?.setOnClickListener {
            val nom = etPetName?.text?.toString()?.trim() ?: ""
            val type = spinnerPetType?.selectedItem?.toString() ?: "Chien"

            if (nom.isEmpty()) {
                Toast.makeText(this, "Veuillez entrer le nom de l'animal", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val nouvelAnimal = AnimalSupabase(
                    idUser = UserStore.currentUserId,
                    nom = nom,
                    typeAnimal = type,
                    race = ""
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