package com.mformula

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform