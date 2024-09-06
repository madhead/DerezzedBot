package me.madhead.derezzed.pipeline.processors

import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.answers.answerInlineQuery
import dev.inmo.tgbotapi.types.InlineQueries.InlineQueryResult.InlineQueryResultVideoImpl
import dev.inmo.tgbotapi.types.update.InlineQueryUpdate
import dev.inmo.tgbotapi.types.update.abstracts.Update
import dev.inmo.tgbotapi.utils.MimeType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import me.madhead.derezzed.pipeline.UpdateProcessor
import me.madhead.derezzed.pipeline.UpdateReaction
import org.apache.logging.log4j.LogManager
import java.io.File
import java.util.UUID

class YouTubeInlineQueryProcessor(
    private val bot: RequestsExecutor,
) : UpdateProcessor {
    companion object {
        private val logger = LogManager.getLogger(YouTubeInlineQueryProcessor::class.java)!!
        private val json = Json
    }

    override suspend fun process(update: Update): UpdateReaction? {
        val update = update as? InlineQueryUpdate ?: return skip("Not an inline query update")
        val inlineQuery = update.data
        val youTubeUrl = inlineQuery.query.youTubeUrl() ?: return skip("No YouTube URLs found")

        return {
            val targetFile = File("/data/${inlineQuery.id}.mp4")

            logger.info("Saving $youTubeUrl to $targetFile")

            val processBuilder = ProcessBuilder()

            processBuilder.command(
                "/usr/local/bin/yt-dlp",
                youTubeUrl,
                "--playlist-items",
                "1",
                "--output",
                targetFile.toString(),
                "--force-overwrites",
                "--no-playlist",
                "--verbose",
                // "--print-traffic",
                "--write-thumbnail",
                "--write-info-json",
            )

            processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT)
            processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT)

            logger.info(processBuilder.command())

            processBuilder
                .start()
                .waitFor()
                .also {
                    logger.info("yt-dlp exit code: $it")
                }
                .takeIf { it == 0 }
                ?.let {
                    val title = try {
                        json
                            .decodeFromStream<JsonElement>(File("/data/${inlineQuery.id}.info.json").inputStream())
                            .jsonObject["title"]!!
                            .jsonPrimitive
                            .content
                    } catch (_: Exception) {
                        youTubeUrl
                    }

                    bot.answerInlineQuery(
                        inlineQuery,
                        listOf(
                            InlineQueryResultVideoImpl(
                                id = UUID.randomUUID().toString(),
                                url = "https://derezzed-bot.fly.dev/video/${inlineQuery.id}",
                                thumbnailUrl = "https://derezzed-bot.fly.dev/thumbnail/${inlineQuery.id}",
                                mimeType = MimeType("video/mp4"),
                                title = title
                            ),
                        )
                    )
                }
        }
    }

    private fun skip(message: String): UpdateReaction? {
        logger.debug(message)

        return null
    }

    private fun String.youTubeUrl(): String? =
        this
            .split("\\s".toRegex())
            .firstOrNull { it.contains("youtube.com", ignoreCase = true) || it.contains("youtu.be", ignoreCase = true) }
}
