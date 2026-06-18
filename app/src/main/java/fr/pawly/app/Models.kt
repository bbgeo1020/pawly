@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)
package fr.pawly.app

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UtilisateurDB(
    @SerialName("id_user") val idUser: String? = null,
    val prenom: String = "",
    val nom: String = "",
    val email: String = "",
    val telephone: String? = null,
    val adresse: String? = null,
    val role: String? = "proprietaire",
    val statut: String? = "Non vérifié",
    val bio: String? = null
)

@Serializable
data class AnimalDB(
    @SerialName("id_animal") val idAnimal: String? = null,
    @SerialName("id_user") val idUser: String = "",
    val nom: String = "",
    @SerialName("type_animal") val typeAnimal: String? = null,
    val race: String? = null,
    @SerialName("date_naissance") val dateNaissance: String? = null,
    @SerialName("poids_kg") val poidsKg: Double? = null,
    val sexe: String? = null,
    @SerialName("photo_url") val photoUrl: String? = null,
    @SerialName("infos_medicales") val infosMedicales: String? = null,
    @SerialName("vaccinations_a_jour") val vaccinationsAJour: Boolean? = null
)

@Serializable
data class MessageDB(
    @SerialName("id_message") val idMessage: String? = null,
    @SerialName("id_conversation") val idConversation: String,
    @SerialName("id_expediteur") val idExpediteur: String,
    val contenu: String,
    @SerialName("date_envoi") val dateEnvoi: String? = null
)

@Serializable
data class ReservationSupabase(
    @SerialName("id_reservation") val idReservation: String? = null,
    @SerialName("id_proprietaire") val idProprietaire: String,
    @SerialName("id_prestataire") val idPrestataire: String?,
    @SerialName("date_debut") val dateDebut: String,
    @SerialName("date_fin") val dateFin: String,
    val statut: String = "en_attente",
    @SerialName("type_garde") val typeGarde: String?,
    @SerialName("prix_total_frais_plateform") val prixTotalFraisPlateforme: Double?,
    @SerialName("statut_remboursement") val statutRemboursement: String = "Aucun",
    @SerialName("date_demande") val dateDemande: String? = null
)

@Serializable
data class JournalEntryDB(
    val id: String? = null,
    @SerialName("journal_id") val journalId: String,
    @SerialName("auteur_id") val auteurId: String,
    @SerialName("offre_id") val offreId: String,
    val titre: String,
    val contenu: String,
    @SerialName("created_at") val createdAt: String? = null
)