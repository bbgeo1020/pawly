@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

package fr.pawly.app.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Suppress("SpellCheckingInspection", "unused")
@Serializable
data class ReservationSupabase(
    @SerialName("id_reservation")
    val idReservation: String? = null,

    @SerialName("id_proprietaire")
    val idProprietaire: String = "",

    @SerialName("id_prestataire")
    val idPrestataire: String? = null,

    @SerialName("date_debut")
    val dateDebut: String = "",

    @SerialName("date_fin")
    val dateFin: String = "",

    val statut: String = "en_attente",

    @SerialName("type_garde")
    val typeGarde: String? = null,

    @SerialName("prix_total_frais_plateforme")
    val prixTotalFraisPlateforme: Double? = null,

    @SerialName("montant_prestataire")
    val montantPrestataire: Double? = null,

    @SerialName("motif_refus")
    val motifRefus: String? = null,

    @SerialName("date_demande")
    val dateDemande: String? = null,

    @SerialName("date_acceptation")
    val dateAcceptation: String? = null,

    @SerialName("instructions_specifique")
    val instructionsSpecifique: String? = null,

    @SerialName("contact_urgence_nom")
    val contactUrgenceNom: String? = null,

    @SerialName("contact_urgance_tel")
    val contactUrgenceTel: String? = null,

    @SerialName("id_veterinaire_urgence")
    val idVeterinaireUrgence: String? = null
)