@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)
@file:Suppress("SpellCheckingInspection")

package fr.pawly.app.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Représente un animal dans la base de données Supabase.
 * Les avertissements d'orthographe et de sérialisation interne sont supprimés pour plus de clarté.
 */
@Serializable
data class AnimalSupabase(
    @SerialName("id_animal")
    val idAnimal: String = "",

    @SerialName("id_user")
    val idUser: String = "",

    val nom: String = "",

    @SerialName("type_animal")
    val typeAnimal: String = "",

    val race: String? = null,

    @SerialName("date_naissance")
    val dateNaissance: String? = null,

    @SerialName("poids_kg")
    val poidsKg: Double? = null,

    val sexe: String? = null,

    @SerialName("photo_url")
    val photoUrl: String? = null,

    @SerialName("infos_medicales")
    val infosMedicales: String? = null,

    @SerialName("habitudes_alimentation")
    val habitudesAlimentation: String? = null,

    @SerialName("habitudes_comportement")
    val habitudesComportement: String? = null,

    @SerialName("vaccinations_a_jour")
    val vaccinationsAJour: Boolean = false,

    @SerialName("numero_puce")
    val numeroPuce: String? = null,

    @SerialName("id_veterinaire_habituel")
    val idVeterinaireHabituel: String? = null
)
