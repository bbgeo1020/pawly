package fr.pawly.app.data

import fr.pawly.app.SupabaseManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

object JournalRepository {

    private val db = SupabaseManager.client.postgrest

    private fun getUserId(): String =
        SupabaseManager.client.auth.currentUserOrNull()?.id ?: ""

    // ── Récupérer les entrées de l'utilisateur connecté ───────────
    suspend fun getEntries(): Result<List<JournalSupabase>> {
        return try {
            val userId = getUserId()
            val list = db["e_journal"].select {
                filter { eq("id_prestataire", userId) }
                order("date_publication", Order.DESCENDING)
            }.decodeList<JournalSupabase>()
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Ajouter une entrée ─────────────────────────────────────────
    suspend fun ajouterEntree(
        titre: String,
        contenu: String,
        typeContenu: String = "note"
    ): Result<Unit> {
        return try {
            val userId = getUserId()
            db["e_journal"].insert(
                JournalSupabase(
                    idPrestataire = userId,
                    titre = titre,
                    contenu = contenu,
                    typeContenu = typeContenu
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Supprimer une entrée ────────────────────────────────────────
    suspend fun supprimerEntree(idArticle: String): Result<Unit> {
        return try {
            db["e_journal"].delete {
                filter { eq("id_article", idArticle) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}