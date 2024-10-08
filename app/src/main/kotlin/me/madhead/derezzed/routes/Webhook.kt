package me.madhead.derezzed.routes

import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.types.update.abstracts.UpdateDeserializationStrategy
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.server.routing.localPort
import io.ktor.server.routing.post
import kotlinx.serialization.json.Json
import me.madhead.derezzed.pipeline.UpdateProcessingPipeline
import me.madhead.derezzed.pipeline.processors.ReelProcessor
import me.madhead.derezzed.pipeline.processors.TikTokInlineQueryProcessor
import me.madhead.derezzed.pipeline.processors.TikTokProcessor
import me.madhead.derezzed.pipeline.processors.XProcessor
import me.madhead.derezzed.pipeline.processors.YouTubeInlineQueryProcessor
import me.madhead.derezzed.pipeline.processors.YouTubeProcessor
import org.apache.logging.log4j.LogManager

fun Route.webhook() {
    val logger = LogManager.getLogger("me.madhead.derezzed.routes.Webhook")
    val config = this.application.environment.config
    val json = Json {
        ignoreUnknownKeys = true
    }
    val bot = telegramBot(config.property("telegram.token").getString())
    val pipeline = UpdateProcessingPipeline(
        listOf(
            TikTokProcessor(bot),
            YouTubeProcessor(bot),
            ReelProcessor(bot),
            XProcessor(bot),
            TikTokInlineQueryProcessor(bot),
            YouTubeInlineQueryProcessor(bot),
        )
    )

    localPort(config.property("deployment.port").getString().toInt()) {
        post(config.property("telegram.token").getString()) {
            try {
                val payload = call.receiveText()

                logger.debug("Request payload: {}", payload)

                val update = json.decodeFromString(UpdateDeserializationStrategy, payload)

                logger.info("Update object: {}", update)

                pipeline.process(update)
            } catch (ignored: Exception) {
                logger.error("Failed to handle the request", ignored)
            }

            call.respond(HttpStatusCode.OK)
        }
    }
}
