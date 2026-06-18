package fr.pawly.app.data

import fr.pawly.app.SupabaseManager
import fr.pawly.app.UserStore
import fr.pawly.app.ReservationSupabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ReservationRepository {
    private val db = SupabaseManager.client.postgrest

    // 1. Récupération des réservations
    suspend fun getReservations(): Result<List<ReservationSupabase>> = withContext(Dispatchers.IO) {
        try {
            val userId = UserStore.currentUserId.ifEmpty { SupabaseManager.client.auth.currentUserOrNull()?.id ?: "" }
            val list = db["Réserve"].select {
                filter { or { eq("id_proprietaire", userId); eq("id_prestataire", userId) } }
                order("date_debut", Order.DESCENDING)
            }.decodeList<ReservationSupabase>()
            Result.success(list)
        } catch (e: Exception) { Result.failure(e) }
    }

    // 2. Ajout de réservation
    suspend fun ajouterReservation(
        idPrestataire: String?,
        dateDebut: String,
        dateFin: String,
        typeGarde: String?,
        prixTotal: Double?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = UserStore.currentUserId.ifEmpty { SupabaseManager.client.auth.currentUserOrNull()?.id ?: "" }
            if (userId.isEmpty()) return@withContext Result.failure(Exception("Utilisateur non connecté"))

            val nouvelleReservation = ReservationSupabase(
                idProprietaire = userId,
                idPrestataire = idPrestataire,
                dateDebut = dateDebut,
                dateFin = dateFin,
                statut = "en_attente",
                typeGarde = typeGarde,
                prixTotalFraisPlateforme = prixTotal
            )

            db["Réserve"].insert(nouvelleReservation)
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    // 3. Demande de remboursement
    suspend fun demanderRemboursement(idReservation: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            db["Réserve"].update({
                set("statut", "annulee")
                set("statut_remboursement", "Demande")
            }) {
                filter { eq("id_reservation", idReservation) }
            }
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }
}