package com.skira.app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform