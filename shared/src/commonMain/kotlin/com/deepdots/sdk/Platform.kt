package com.deepdots.sdk

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform