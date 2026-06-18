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
        return UserStore.currentUserId.ifEmpty {
            SupabaseManager.client.auth.currentUserOrNull()?.id ?: ""
        }
    }

    suspend fun getAnimaux(): Result<List<AnimalSupabase>> = withContext(Dispatchers.IO) {
        try {
            val userId = getUserId()
            if (userId.isEmpty()) return@withContext Result.failure(Exception("ID manquant"))
            // Cible bien "animaux" (le nom de ta table réelle)
            val list = db["animaux"].select { filter { eq("idUser", userId) } }.decodeList<AnimalSupabase>()
            Result.success(list)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun ajouterAnimal(animal: AnimalSupabase): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            db["animaux"].insert(animal)
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }
}