package fr.pawly.app.data

import fr.pawly.app.SupabaseManager
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object JournalGardeRepository {
    private val db = SupabaseManager.client.postgrest

    // On cherche dans ta table "e_journal" et on utilise ton modèle "JournalSupabase"
    suspend fun getLogsDuJournal(idReservation: String): Result<List<JournalSupabase>> = withContext(Dispatchers.IO) {
        try {
            val liste = db["e_journal"].select {
                filter { eq("id_reservation", idReservation) }
            }.decodeList<JournalSupabase>()
            Result.success(liste)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}