package com.madalv

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.madalv.plugins.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Thread.sleep
import java.util.concurrent.ThreadLocalRandom
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
                    client.generateTakeoutList()
                    client.sendOrder()
                    //logger.debug { "CLIENT ${client.id} sent order to Ordering Service: ${client.orderList}" }
                    delay(ThreadLocalRandom.current().nextInt(cfg.clientWaitMin, cfg.clientWaitMax) * cfg.timeUnit)
                    clients.getAndDecrement()
                }
                //sleep(ThreadLocalRandom.current().nextInt(cfg.clientWaitMin, cfg.clientWaitMax) * cfg.timeunit)
            }
        }
    }.start(wait = true)
}
