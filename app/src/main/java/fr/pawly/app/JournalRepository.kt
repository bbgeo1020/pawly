@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)
package fr.pawly.app // ✅ Ajusté à la racine pour correspondre à ton Models.kt et LoginActivity

import fr.pawly.app.SupabaseManager
import fr.pawly.app.UserStore
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JournalEntrySupabase(
    val id: Int? = null,
    @SerialName("id_utilisateur") val idUtilisateur: String, // ✅ Corrigé : CamelCase + @SerialName pour supprimer l'avertissement d'underscore
    val titre: String,
    val contenu: String,
    @SerialName("date_creation") val dateCreation: String? = null // ✅ Corrigé : Idem
)

object JournalRepository {
    private val db = SupabaseManager.client.postgrest

    suspend fun getEntries(): Result<List<JournalEntrySupabase>> = withContext(Dispatchers.IO) {
        try {
            val userId = UserStore.currentUserId.ifEmpty { SupabaseManager.client.auth.currentUserOrNull()?.id ?: "" }
            val list = db["e_journal"].select {
                filter { eq("id_utilisateur", userId) }
            }.decodeList<JournalEntrySupabase>()
            Result.success(list)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun ajouterEntree(titre: String, contenu: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = UserStore.currentUserId.ifEmpty { SupabaseManager.client.auth.currentUserOrNull()?.id ?: "" }
            db["e_journal"].insert(JournalEntrySupabase(idUtilisateur = userId, titre = titre, contenu = contenu))
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }
}
