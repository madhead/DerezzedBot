package me.madhead.derezzed.routes

import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

fun Route.webhook() {
    get {
        call.respond("Hello")
    }
}
