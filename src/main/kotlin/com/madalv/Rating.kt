package com.madalv

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Ratings(
    @SerialName("client_id") val clientId: Int,
    @SerialName("order_id") var orderId: Int,
    @SerialName("orders") val ratings: MutableList<Rating>
)

@Serializable
data class Rating(
    @SerialName("restaurant_id") val restaurantId: Int,
    @SerialName("rating") val rating: Int,
    @SerialName("order_id") val orderId: Int,
    @SerialName("estimated_waiting_time") val estimatedWait: Double,
    @SerialName("waiting_time") val waitingTime: Long,
)