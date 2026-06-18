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

    private fun getUserId(): String {
        val authId = SupabaseManager.client.auth.currentUserOrNull()?.id ?: ""
        return authId.ifEmpty { UserStore.currentUserId }
    }

    suspend fun getReservations(): Result<List<ReservationSupabase>> = withContext(Dispatchers.IO) {
        try {
            val userId = getUserId()
            if (userId.isEmpty()) return@withContext Result.failure(Exception("ID manquant"))

            // CORRIGÉ : Table "Réserve" synchronisée
            val list = db["Réserve"].select {
                filter {
                    or {
                        eq("id_proprietaire", userId)
                        eq("id_prestataire", userId)
                    }
                }
                order("date_demande", Order.DESCENDING)
            }.decodeList<ReservationSupabase>()
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun ajouterReservation(
        idPrestataire: String?,
        dateDebut: String,
        dateFin: String,
        typeGarde: String?,
        prixTotal: Double?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = getUserId()
            if (userId.isEmpty()) return@withContext Result.failure(Exception("Session expirée"))

            // CORRIGÉ : Table "Réserve" synchronisée
            db["Réserve"].insert(
                ReservationSupabase(
                    idProprietaire = userId,
                    idPrestataire = idPrestataire,
                    dateDebut = dateDebut,
                    dateFin = dateFin,
                    statut = "en_attente",
                    typeGarde = typeGarde,
                    prixTotalFraisPlateforme = prixTotal,
                    statutRemboursement = "Aucun"
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun demanderRemboursement(idReservation: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // CORRIGÉ : Table "Réserve" synchronisée
            db["Réserve"].update(
                {
                    set("statut", "annulee")
                    set("statut_remboursement", "Demande")
                }
            ) {
                filter { eq("id_reservation", idReservation) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun validerRemboursementParAdmin(idReservation: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // CORRIGÉ : Table "Réserve" synchronisée
            db["Réserve"].update(
                { set("statut_remboursement", "Rembourse") }
            ) {
                filter { eq("id_reservation", idReservation) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}