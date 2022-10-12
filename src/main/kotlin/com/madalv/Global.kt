package com.madalv

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import java.io.File

val configJson = File("config/config.json").inputStream().readBytes().toString(Charsets.UTF_8)
val cfg: Config = Json.decodeFromString(Config.serializer(), configJson)

val logger = KotlinLogging.logger {}
val client = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
        })
    }
}