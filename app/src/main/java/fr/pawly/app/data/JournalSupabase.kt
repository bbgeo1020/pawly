@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

package fr.pawly.app.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JournalSupabase(
    @SerialName("id_article")
    val idArticle: String = "",

    @SerialName("id_prestataire")
    val idPrestataire: String = "",

    @SerialName("id_reservation")
    val idReservation: String? = null,

    val titre: String = "",

    val contenu: String = "",

    @SerialName("type_contenu")
    val typeContenu: String = "note",

    @SerialName("media_url")
    val mediaUrl: String? = null,

    @SerialName("likes_count")
    val likesCount: Int = 0,

    @SerialName("vues_count")
    val vuesCount: Int = 0,

    val publie: Boolean = true,

    @SerialName("date_publication")
    val datePublication: String? = null
)