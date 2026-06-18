package fr.pawly.app

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class RatingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rating)

        supportActionBar?.title = "Noter votre gardien"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val gardienName = intent.getStringExtra("GARDIEN_NAME") ?: "Votre gardien"
        val tvGardien   = findViewById<TextView>(R.id.tvRatingGardienName)
        val ratingBar   = findViewById<RatingBar>(R.id.ratingBar)
        val etComment   = findViewById<EditText>(R.id.etRatingComment)
        val btnSubmit   = findViewById<Button>(R.id.btnSubmitRating)

        tvGardien.text = "Notez $gardienName"

        btnSubmit.setOnClickListener {
            val note    = ratingBar.rating
            val comment = etComment.text.toString().trim()

            if (note == 0f) {
                Toast.makeText(this,
                    "⚠️ Donnez une note avant de valider",
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (comment.isEmpty()) {
                etComment.error = "Un commentaire est obligatoire"
                return@setOnClickListener
            }

            val stars = "⭐".repeat(note.toInt())
            Toast.makeText(this,
                "Merci ! Vous avez noté $gardienName : $stars",
                Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}