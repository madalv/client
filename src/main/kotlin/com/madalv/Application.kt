package com.madalv

import com.madalv.plugins.configureRouting
import com.madalv.plugins.configureSerialization
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

val clients = AtomicInteger(0)

fun main() {
    embeddedServer(Netty, port = cfg.port, host = "0.0.0.0") {
        configureSerialization()
        configureRouting()

        while(true){
            if (clients.get() < cfg.maxClients) {
                clients.getAndIncrement()
                launch {
                    val client = Client()
                    client.order()
                }
            }
        }
    }.start(wait = true)
}
