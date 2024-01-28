package me.madhead.derezzed.pipeline

import dev.inmo.tgbotapi.types.update.abstracts.Update

interface UpdateProcessor {
    suspend fun process(update: Update): UpdateReaction?
}
