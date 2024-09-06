package me.madhead.derezzed.pipeline

import dev.inmo.tgbotapi.types.update.abstracts.Update
import org.apache.logging.log4j.LogManager

class UpdateProcessingPipeline(
    private val processors: List<UpdateProcessor>,
) {
    companion object {
        private val logger = LogManager.getLogger(UpdateProcessingPipeline::class.java)!!
    }

    suspend fun process(update: Update) {
        logger.debug("Processing update: {}", update)

        processors
            .mapNotNull { it.process(update) }
            .also { reactions ->
                logger.debug("Reactions ({}): {}", reactions.size, reactions.map { it::class })
                if (reactions.size != 1) {
                    logger.warn("No suitable processor found or found more than one")
                }
            }
            .singleOrNull()
            ?.invoke()
    }
}
