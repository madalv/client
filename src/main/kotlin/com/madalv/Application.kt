package com.madalv

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.madalv.plugins.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

fun main() {
    embeddedServer(Netty, port = cfg.port, host = "0.0.0.0") {
        configureSerialization()
        configureRouting()

        repeat(6){
            launch {
                val client = Client(5)
                client.sendOrder()
                logger.debug { "CLIENT ${client.id} sent order to Ordering Service: ${client.orderList}" }
            }
        }
    }.start(wait = true)
}