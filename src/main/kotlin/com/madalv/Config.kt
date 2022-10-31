package com.madalv
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class Config(
    val port: Int,
    val ordserv: String,
    @SerialName("client_id_max") val clientIdMax: Int,
    @SerialName("timeunit") val timeUnit: Long,
    @SerialName("client_wait_min") val clientWaitMin: Int,
    @SerialName("client_wait_max") val clientWaitMax: Int,
    @SerialName("max_items_order") val maxItemsPerOrder: Int,
    @SerialName("max_clients") val maxClients: Int
)
