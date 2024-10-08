package me.madhead.derezzed.pipeline.processors

import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.send.media.sendVideo
import dev.inmo.tgbotapi.requests.abstracts.asMultipartFile
import dev.inmo.tgbotapi.types.ReplyParameters
import dev.inmo.tgbotapi.types.message.abstracts.ContentMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.message.textsources.TextLinkTextSource
import dev.inmo.tgbotapi.types.message.textsources.URLTextSource
import dev.inmo.tgbotapi.types.update.MessageUpdate
import dev.inmo.tgbotapi.types.update.abstracts.Update
import me.madhead.derezzed.pipeline.UpdateProcessor
import me.madhead.derezzed.pipeline.UpdateReaction
import org.apache.logging.log4j.LogManager
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteIfExists

class ReelProcessor(
    private val bot: RequestsExecutor,
) : UpdateProcessor {
    companion object {
        private val logger = LogManager.getLogger(ReelProcessor::class.java)!!
    }

    override suspend fun process(update: Update): UpdateReaction? {
        val update = update as? MessageUpdate ?: return skip("Not a message update")
        val message = update.data as? ContentMessage<*> ?: return skip("Not a content message")
        val content = message.content as? TextContent ?: return skip("Not a text content")
        val reelUrls = content.reelUrls() ?: return skip("No YouTube URLs found")

        logger.info("Found Reel URLs: $reelUrls")

        return {
            reelUrls.forEach { url ->
                val targetFile = createTempFile(suffix = ".mp4")

                logger.info("Saving $url to $targetFile")

                val processBuilder = ProcessBuilder()

                processBuilder.command(
                    "/usr/local/bin/yt-dlp",
                    url,
                    "--output",
                    targetFile.toString(),
                    "--force-overwrites",
                    "--verbose",
                    // "--print-traffic",
                )
                processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT)
                processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT)

                logger.info(processBuilder.command())

                processBuilder.start().waitFor()
                    .also {
                        logger.info("yt-dlp exit code: $it")
                    }
                    .takeIf { it == 0 }
                    ?.let {
                        bot.sendVideo(
                            chat = message.chat,
                            video = targetFile.toFile().asMultipartFile(),
                            replyParameters = ReplyParameters(message = message),
                        )
                    }

                targetFile.deleteIfExists()
            }
        }
    }

    private fun skip(message: String): UpdateReaction? {
        logger.debug(message)

        return null
    }

    private fun TextContent.reelUrls(): List<String>? =
        textSources
            .mapNotNull { (it as? URLTextSource)?.source ?: (it as? TextLinkTextSource)?.url }
            .mapNotNull {
                it.takeIf {
                    it.contains("instagram.com/reel", ignoreCase = true) || it.contains("instagram.com/reels", ignoreCase = true)
                }
            }
            .takeUnless { it.isEmpty() }
}
