package com.madalv

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.madalv.plugins.*
import kotlinx.coroutines.launch
import java.lang.Thread.sleep
import java.util.concurrent.ThreadLocalRandom

fun main() {
    embeddedServer(Netty, port = cfg.port, host = "0.0.0.0") {
        configureSerialization()
        configureRouting()

        while(true){
            launch {
                val client = Client()
                client.generateTakeoutList()
                client.sendOrder()
                logger.debug { "CLIENT ${client.id} sent order to Ordering Service: ${client.orderList}" }
            }
            sleep(ThreadLocalRandom.current().nextInt(cfg.clientWaitMin, cfg.clientWaitMax) * cfg.timeunit)
        }
    }.start(wait = true)
}
