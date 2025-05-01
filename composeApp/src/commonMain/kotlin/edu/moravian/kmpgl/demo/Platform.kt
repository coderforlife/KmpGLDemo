package edu.moravian.kmpgl.demo

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform