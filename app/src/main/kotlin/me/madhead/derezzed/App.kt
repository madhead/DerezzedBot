package me.madhead.derezzed

class App {
    val greeting: String
        get() {
            return "Hello World!"
        }
}

fun main() {
    println(App().greeting)
}
