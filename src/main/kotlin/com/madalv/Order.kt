package com.madalv

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TakeoutOrder(
    @SerialName("restaurant_id") val restaurantID: Int,
    val items: List<Int>,
    val priority: Int,
    @SerialName("max_wait") var maxWait: Double,
    @SerialName("created_time") var createdTime: Long
)

@Serializable
data class TakeoutResponse(
    @SerialName("order_id")val id: Int,
    @SerialName("restaurant_id") val restaurantID: Int,
    @SerialName("restaurant_address") val resAddress: String,
    @SerialName("estimated_waiting_time") val estimatedWait: Double,
    @SerialName("created_time") val createdTime: Long,
    @SerialName("registered_time") val registeredTime: Long
)

@Serializable
data class TakeoutList(
    @SerialName("order_id") val id: Int,
    @SerialName("client_id") val clientID: Int,
    val orders: MutableList<TakeoutOrder>
)

@Serializable
data class TakeoutResponseList(
    @SerialName("order_id") val orderID: Int,
    val orders: MutableList<TakeoutResponse>
)
