package fr.pawly.app.data

import fr.pawly.app.SupabaseManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest

object AnimalRepository {

    private val db = SupabaseManager.client.postgrest

    fun getUserId(): String =
        SupabaseManager.client.auth.currentUserOrNull()?.id ?: ""

    // ── Récupérer tous les animaux de l'utilisateur connecté ─────
    suspend fun getAnimaux(): Result<List<AnimalSupabase>> {
        return try {
            val userId = getUserId()
            val list = db["animal"].select {
                filter { eq("id_user", userId) }
            }.decodeList<AnimalSupabase>()
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Ajouter un animal ─────────────────────────────────────────
    suspend fun ajouterAnimal(
        nom: String,
        typeAnimal: String,
        dateNaissance: String? = null,
        race: String? = null
    ): Result<Unit> {
        return try {
            val userId = getUserId()
            db["animal"].insert(
                AnimalSupabase(
                    idUser = userId,
                    nom = nom,
                    typeAnimal = typeAnimal,
                    dateNaissance = dateNaissance,
                    race = race
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Supprimer un animal ───────────────────────────────────────
    suspend fun supprimerAnimal(idAnimal: String): Result<Unit> {
        return try {
            db["animal"].delete {
                filter { eq("id_animal", idAnimal) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}