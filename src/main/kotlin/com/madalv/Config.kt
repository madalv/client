package com.madalv
import kotlinx.serialization.Serializable

@Serializable
class Config(
    val port: Int,
    val ordserv: String
)
