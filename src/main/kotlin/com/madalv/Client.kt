package com.madalv

import MenusData
import com.sun.tools.javac.Main
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.coroutines.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.roundToLong

class Client {
    var id: Int = ThreadLocalRandom.current().nextInt(0, cfg.clientIdMax)
    var orderList: TakeoutList = TakeoutList(id, mutableListOf())
    var responseList = TakeoutResponseList(-5, mutableListOf())

    // TODO pickup order
    // TODO client rating
    // TODO avg rating

    private suspend fun getMenu(): MenusData {
        return client.get("http://${cfg.ordserv}/menu").body()
    }

    suspend fun generateTakeoutList() {
        val menuData = getMenu()
        val r = ThreadLocalRandom.current()
        if (menuData.restaurants < 1) {
            logger.debug { "No restaurants? Really?" }
            return
        } else for (i in 1 .. menuData.restaurants) {
            if (r.nextBoolean()) continue
            orderList.orders.add(generateOrder(i, menuData))
        }

        if (orderList.orders.size == 0) orderList.orders.add(generateOrder(menuData.restaurantsData[0].restaurantID, menuData))
    }

    private fun generateOrder(i: Int, menuData: MenusData): TakeoutOrder {
        val r = ThreadLocalRandom.current()
        val restData = menuData.restaurantsData[i - 1]
        val itemNr: Int = r.nextInt(1, cfg.maxItemsPerOrder)
        val items: List<Int> = List(itemNr) { r.nextInt(1, restData.menuItems + 1) }
        val priority = restData.menuItems - itemNr

        var prepTimeMax: Long = 0
        for (foodId in items) {
            if (restData.menu[foodId - 1].preparationTime > prepTimeMax)
                prepTimeMax = restData.menu[foodId - 1].preparationTime
        }

        return TakeoutOrder(
            i,
            items,
            priority,
            prepTimeMax * 1.8,
            System.currentTimeMillis()
        )
    }

    @OptIn(InternalAPI::class)
    suspend fun sendOrder() {
        logger.debug { orderList }
        val response: TakeoutResponseList = client.post("http://${cfg.ordserv}/order") {
            contentType(ContentType.Application.Json)
            setBody(orderList)
        }.body()

        responseList = response
        logger.debug { "$response ${clients.get()}" }

        for (r in responseList.responses) {
            CoroutineScope(Dispatchers.Default).launch {
                delay(r.estimatedWait.roundToLong() * cfg.timeUnit)
                var re: DetailedTakeout = client.get("http://${r.resAddress}/v2/order/${r.id}").body()

                while(!re.isReady) {
                    logger.debug { "Takeout ${re.id} from not ready res ${r.restaurantID} not ready yet." }
                    delay(re.estimatedWait.roundToLong() * cfg.timeUnit)
                    re = client.get("http://${r.resAddress}/v2/order/${r.id}").body()
                }

                if (re.isReady) {
                    val rating = calculateRating(re.cookingTime, re.maxWait)
                    logger.debug { " Client $id got takeout ${re.id} from res ${r.restaurantID}: MAXWAIT ${re.maxWait} TIME ${re.cookingTime} RATING $rating"  }
                } else {
                    delay(re.estimatedWait.roundToLong() * cfg.timeUnit)
                    logger.debug { "Something went terribly wrong." }
                }
            }
        }
    }

    private fun calculateRating(waitTime: Long, maxWait: Double): Int {
        var rating = 0
        if (waitTime <= maxWait * cfg.timeUnit) rating = 5
        else if (waitTime <= maxWait * 1.1 * cfg.timeUnit) rating = 4
        else if (waitTime <= maxWait * 1.2 * cfg.timeUnit) rating = 3
        else if (waitTime <= maxWait * 1.3 * cfg.timeUnit) rating = 2
        else if (waitTime <= maxWait * 1.4 * cfg.timeUnit) rating = 1

        return rating
    }
}