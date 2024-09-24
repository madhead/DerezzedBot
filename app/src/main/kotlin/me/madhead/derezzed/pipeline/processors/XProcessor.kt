package me.madhead.derezzed.pipeline.processors

import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.send.sendTextMessage
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

class XProcessor(
    private val bot: RequestsExecutor,
) : UpdateProcessor {
    companion object {
        private val logger = LogManager.getLogger(XProcessor::class.java)!!
    }

    override suspend fun process(update: Update): UpdateReaction? {
        val update = update as? MessageUpdate ?: return skip("Not a message update")
        val message = update.data as? ContentMessage<*> ?: return skip("Not a content message")
        val content = message.content as? TextContent ?: return skip("Not a text content")
        val XUrls = content.XUrls() ?: return skip("No X URLs found")

        logger.info("Found X URLs: $XUrls")

        return {
            XUrls.forEach { url ->
                bot.sendTextMessage(
                    chat = message.chat,
                    text = url.replace(Regex("(x\\.com)|(twitter\\.com)", RegexOption.IGNORE_CASE), "vxtwitter.com"),
                    replyParameters = ReplyParameters(message = message),
                )
            }
        }
    }

    private fun skip(message: String): UpdateReaction? {
        logger.debug(message)

        return null
    }

    private fun TextContent.XUrls(): List<String>? =
        textSources
            .mapNotNull { (it as? URLTextSource)?.source ?: (it as? TextLinkTextSource)?.url }
            .mapNotNull {
                it.takeIf {
                    it.contains(Regex("(?<!fixup)x\\.com", RegexOption.IGNORE_CASE)) ||
                        it.contains(Regex("(?<!(vx)|(fx))twitter\\.com", RegexOption.IGNORE_CASE))
                }
            }
            .takeUnless { it.isEmpty() }
}
