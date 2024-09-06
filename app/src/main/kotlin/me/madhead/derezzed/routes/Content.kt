package me.madhead.derezzed.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.response.respondFile
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.server.routing.get
import io.ktor.server.routing.localPort
import org.apache.logging.log4j.LogManager
import java.io.File

fun Route.content() {
    val logger = LogManager.getLogger("me.madhead.derezzed.routes.Content")
    val config = this.application.environment.config

    localPort(config.property("deployment.port").getString().toInt()) {
        get("/video/{fileName}") {
            val fileName = call.parameters["fileName"]
            val file = File("/data/$fileName.mp4")

            if (file.exists()) {
                call.respondFile(file)
            } else {
                call.respond(HttpStatusCode.NotFound, "Video not found")
            }
        }

        get("/thumbnail/{fileName}") {
            val fileName = call.parameters["fileName"]
            val file = File("/data/$fileName.jpg")

            if (file.exists()) {
                call.respondFile(file)
            } else {
                call.respond(HttpStatusCode.NotFound, "Thumbnail not found")
            }
        }
    }
}
