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

class YouTubeProcessor(
    private val bot: RequestsExecutor,
) : UpdateProcessor {
    companion object {
        private val logger = LogManager.getLogger(YouTubeProcessor::class.java)!!
    }

    override suspend fun process(update: Update): UpdateReaction? {
        val update = update as? MessageUpdate ?: return skip("Not a message update")
        val message = update.data as? ContentMessage<*> ?: return skip("Not a content message")
        val content = message.content as? TextContent ?: return skip("Not a text content")
        val youTubeUrls = content.youTubeUrls() ?: return skip("No YouTube URLs found")

        logger.info("Found YouTube URLs: $youTubeUrls")

        return {
            youTubeUrls.forEach { url ->
                val targetFile = createTempFile(suffix = ".mp4")

                logger.info("Saving $url to $targetFile")

                val processBuilder = ProcessBuilder()

                processBuilder.command("/usr/local/bin/yt-dlp", url, "--output", targetFile.toString(), "--force-overwrites", "--no-playlist")
                processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT)

                logger.info(processBuilder.command())

                processBuilder.start().waitFor()

                bot.sendVideo(
                    chat = message.chat,
                    video = targetFile.toFile().asMultipartFile(),
                    replyParameters = ReplyParameters(message = message),
                )

                targetFile.deleteIfExists()
            }
        }
    }

    private fun skip(message: String): UpdateReaction? {
        logger.debug(message)

        return null
    }

    private fun TextContent.youTubeUrls(): List<String>? =
        textSources
            .mapNotNull { (it as? URLTextSource)?.source ?: (it as? TextLinkTextSource)?.url }
            .mapNotNull {
                it.takeIf {
                    it.contains("youtube.com", ignoreCase = true) || it.contains("youtu.be", ignoreCase = true)
                }
            }
            .takeUnless { it.isEmpty() }
}
