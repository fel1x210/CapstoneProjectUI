package ca.gbc.comp3074.uiprototype.data.supabase.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserFavorite(
    val id: String? = null,
    @SerialName("user_id")
    val userId: String,
    @SerialName("google_place_id")
    val googlePlaceId: String,
    val name: String,
    val address: String? = null,
    val rating: Float? = null,
    @SerialName("user_ratings_total")
    val userRatingsTotal: Int? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("place_type")
    val placeType: String? = null,
    @SerialName("quiet_score")
    val quietScore: Float? = null,
    @SerialName("created_at")
    val createdAt: String? = null
)
