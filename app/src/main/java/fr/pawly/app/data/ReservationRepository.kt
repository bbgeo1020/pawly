package fr.pawly.app.data

import fr.pawly.app.SupabaseManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

object ReservationRepository {

    private val db = SupabaseManager.client.postgrest

    private fun getUserId(): String =
        SupabaseManager.client.auth.currentUserOrNull()?.id ?: ""

    // ── Récupérer les réservations du propriétaire connecté ──────────
    suspend fun getReservations(): Result<List<ReservationSupabase>> {
        return try {
            val userId = getUserId()
            val list = db["reservation"].select {
                filter { eq("id_proprietaire", userId) }
                order("date_demande", Order.DESCENDING)
            }.decodeList<ReservationSupabase>()
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Créer une nouvelle réservation ────────────────────────────────
    suspend fun ajouterReservation(
        idPrestataire: String?,
        dateDebut: String,
        dateFin: String,
        typeGarde: String?,
        prixTotal: Double?
    ): Result<Unit> {
        return try {
            val userId = getUserId()
            db["reservation"].insert(
                ReservationSupabase(
                    idProprietaire = userId,
                    idPrestataire = idPrestataire,
                    dateDebut = dateDebut,
                    dateFin = dateFin,
                    statut = "en_attente",
                    typeGarde = typeGarde,
                    prixTotalFraisPlateforme = prixTotal
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Annuler une réservation ────────────────────────────────────────
    suspend fun annulerReservation(idReservation: String): Result<Unit> {
        return try {
            db["reservation"].update(
                { set("statut", "annulee") }
            ) {
                filter { eq("id_reservation", idReservation) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}