package fr.pawly.app.data

import fr.pawly.app.SupabaseManager
import fr.pawly.app.UserStore
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AnimalRepository {
    private val db = SupabaseManager.client.postgrest

    private fun getUserId(): String {
        val authId = SupabaseManager.client.auth.currentUserOrNull()?.id ?: ""
        return authId.ifEmpty { UserStore.currentUserId }
    }

    suspend fun getAnimaux(): Result<List<AnimalSupabase>> = withContext(Dispatchers.IO) {
        try {
            val userId = getUserId()
            if (userId.isEmpty()) return@withContext Result.failure(Exception("Utilisateur non identifie"))

            val liste = db["animaux"].select {
                filter { eq("id_user", userId) }
            }.decodeList<AnimalSupabase>()
            Result.success(liste)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun ajouterAnimal(animal: AnimalSupabase): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            db["animaux"].insert(animal)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}