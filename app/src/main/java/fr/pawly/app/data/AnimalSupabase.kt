@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)
package fr.pawly.app.data

import kotlinx.serialization.Serializable

@Serializable
data class AnimalSupabase(
    val id: Int? = null,
    val idUser: String,
    val nom: String,
    val typeAnimal: String,
    val race: String? = null
)