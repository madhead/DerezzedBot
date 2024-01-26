package me.madhead.derezzed.modules

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.compression.Compression
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.routing.routing
import me.madhead.derezzed.routes.webhook

fun Application.derezzed() {
    install(DefaultHeaders)
    install(CallLogging)
    install(Compression)

    routing {
        webhook()
    }
}
