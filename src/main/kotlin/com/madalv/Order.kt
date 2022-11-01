package com.madalv

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class TakeoutOrder(
    @SerialName("restaurant_id") val restaurantID: Int,
    val items: List<Int>,
    val priority: Int,
    @SerialName("max_wait") var maxWait: Double,
    @SerialName("created_time") var createdTime: Long
)

@Serializable
data class TakeoutList(
    @SerialName("client_id") val clientID: Int,
    val orders: MutableList<TakeoutOrder>
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
data class TakeoutResponseList(
    @SerialName("order_id") val orderID: Int,
    val responses: MutableList<TakeoutResponse>
)

@Serializable
data class DetailedTakeout (
    @SerialName("order_id") val id: Int,
    @SerialName("is_ready") val isReady: Boolean,
    val priority: Int,
    @SerialName("estimated_waiting_time") val estimatedWait: Double,
    @SerialName("created_time") val createdTime: Long,
    @SerialName("registered_time") val registeredTime: Long,
    @SerialName("prepared_time") val preparedTime: Long,
    @SerialName("cooking_time") val cookingTime: Long,
    @SerialName("max_wait") var maxWait: Double,
    @SerialName("cooking_details") var orderItems: MutableList<OrderItem>
)

@Serializable
data class OrderItem(
    @SerialName("food_id") val foodId: Int,
    @Transient val orderId: Int = -5
) {
    @SerialName("cook_id")
    var cookId = 0
}


