package me.madhead.derezzed.pipeline.processors

import dev.inmo.tgbotapi.types.update.abstracts.Update
import me.madhead.derezzed.pipeline.UpdateProcessor
import me.madhead.derezzed.pipeline.UpdateReaction

class TikTokProcessor : UpdateProcessor {
    override suspend fun process(update: Update): UpdateReaction? {
        println("TikTokProcessor!")
        return null
    }
}
