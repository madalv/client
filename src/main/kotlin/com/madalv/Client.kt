package com.madalv

import MenusData
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.roundToLong

class Client {
    private var id: Int = ThreadLocalRandom.current().nextInt(0, cfg.clientIdMax)
    private var orderList: TakeoutList = TakeoutList(id, mutableListOf())
    private var responseList = TakeoutResponseList(-5, mutableListOf())
    private var ratings = Ratings(id, -5, mutableListOf())
    private var jobs =  mutableListOf<Job>()
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


    suspend fun sendOrder() {

        var response: TakeoutResponseList? = null

        runBlocking {
            generateTakeoutList()

            logger.debug { orderList }
             response = client.post("http://${cfg.ordserv}/order") {
                contentType(ContentType.Application.Json)
                setBody(orderList)
            }.body()
        }

        responseList = response!!
        logger.debug { "$response ${clients.get()}" }

        for (r in responseList.responses) {
            val job = CoroutineScope(Dispatchers.Default).launch {
                delay(r.estimatedWait.roundToLong() * cfg.timeUnit)
                var re: DetailedTakeout = client.get("http://${r.resAddress}/v2/order/${r.id}").body()

                while(!re.isReady) {
                    logger.debug { "Takeout ${re.id} from not ready res ${r.restaurantID} not ready yet." }
                    delay(re.estimatedWait.roundToLong() * cfg.timeUnit)
                    re = client.get("http://${r.resAddress}/v2/order/${r.id}").body()
                }

                val rating = calculateRating(re.cookingTime, re.maxWait)
                logger.debug { " Client $id got takeout ${re.id} from res ${r.restaurantID}: MAXWAIT ${re.maxWait} TIME ${re.cookingTime} RATING $rating"  }
                ratings.orderId = re.id
                ratings.ratings.add(Rating(r.restaurantID, rating, re.id, re.estimatedWait, re.cookingTime))

            }
            jobs.add(job)

        }

        jobs.joinAll() // wait for all ratings

        client.post("http://${cfg.ordserv}/rating") {
            contentType(ContentType.Application.Json)
            setBody(ratings)
        }

        logger.debug { "SENT RATINGS $ratings to FOOD ORDERING" }

        clients.getAndDecrement()
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