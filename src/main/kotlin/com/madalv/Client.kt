package com.madalv

import MenusData
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import java.util.concurrent.ThreadLocalRandom

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
            prepTimeMax * 1.3,
            System.currentTimeMillis()
        )
    }

    suspend fun sendOrder() {
        val response: TakeoutResponseList = client.post("http://${cfg.ordserv}/order") {
            contentType(ContentType.Application.Json)
            setBody(orderList)
        }.body()

        //println(response)
        responseList = response
    }
}