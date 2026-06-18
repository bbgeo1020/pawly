package fr.pawly.app.data

import fr.pawly.app.SupabaseManager
import fr.pawly.app.UserStore
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object JournalGardeRepository {
    private val db = SupabaseManager.client.postgrest

    private fun getUserId(): String {
        return UserStore.currentUserId.ifEmpty {
            SupabaseManager.client.auth.currentUserOrNull()?.id ?: ""
        }
    }

    suspend fun getEntries(): Result<List<JournalSupabase>> = withContext(Dispatchers.IO) {
        try {
            val userId = getUserId()
            if (userId.isEmpty()) return@withContext Result.failure(Exception("ID manquant"))

            // Filtre pour afficher uniquement le journal de l'utilisateur connecté
            val liste = db["e_journal"].select {
                filter { eq("id_prestataire", userId) }
            }.decodeList<JournalSupabase>()
            Result.success(liste)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun ajouterEntree(titre: String, contenu: String, typeContenu: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = getUserId()
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.FRANCE).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val dateActuelle = sdf.format(Date())

            val nouvelleEntree = JournalSupabase(
                idPrestataire = userId,
                titre = titre,
                contenu = contenu,
                typeContenu = typeContenu,
                datePublication = dateActuelle
            )
            db["e_journal"].insert(nouvelleEntree)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun supprimerEntree(idArticle: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            db["e_journal"].delete {
                filter { eq("id_article", idArticle) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}