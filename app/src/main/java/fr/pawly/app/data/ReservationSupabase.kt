@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)
package fr.pawly.app.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReservationSupabase(
    @SerialName("id_reservation") val idReservation: String? = null,
    @SerialName("id_proprietaire") val idProprietaire: String = "",
    @SerialName("id_prestataire") val idPrestataire: String? = null,
    @SerialName("date_debut") val dateDebut: String = "",
    @SerialName("date_fin") val dateFin: String = "",
    val statut: String = "en_attente",
    @SerialName("type_garde") val typeGarde: String? = null,
    @SerialName("prix_total_frais_plateforme") val prixTotal: Double? = null,
    @SerialName("statut_remboursement") val statutRemboursement: String? = null
)