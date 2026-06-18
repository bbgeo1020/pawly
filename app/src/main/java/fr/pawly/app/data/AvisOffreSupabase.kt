package fr.pawly.app.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AvisOffreSupabase(
    val id: String = "",

    @SerialName("offre_id")
    val offreId: String? = null,

    @SerialName("proprietaire_id")
    val proprietaireId: String = "",

    @SerialName("prestataire_id")
    val prestataireId: String = "",

    val note: Int = 5,

    val commentaire: String? = null
)