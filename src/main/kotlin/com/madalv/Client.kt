package com.madalv

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement

class Client(val id: Int) {
    var orderList: TakeoutList = TakeoutList(5, id, mutableListOf(
        TakeoutOrder(1, listOf(1, 2), 9, 69.0, System.currentTimeMillis()),
        TakeoutOrder(2, listOf(1, 3), 9, 68.0, System.currentTimeMillis())))
    var responseList = TakeoutResponseList(5, mutableListOf())

    // TODO receive menu
    // TODO generate orderList based on received menu

    suspend fun sendOrder() {
        val response: TakeoutResponseList = client.post("http://${cfg.ordserv}/order") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToJsonElement(orderList))
        }.body()

        println(response)
    }
}