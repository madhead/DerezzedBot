package me.madhead.derezzed.telegram

import dev.inmo.tgbotapi.types.queries.callback.MessageCallbackQuery
import dev.inmo.tgbotapi.types.update.CallbackQueryUpdate
import dev.inmo.tgbotapi.types.update.MyChatMemberUpdatedUpdate
import dev.inmo.tgbotapi.types.update.abstracts.BaseMessageUpdate
import dev.inmo.tgbotapi.types.update.abstracts.Update

val Update.chatId: Long
    get() = when (this) {
        is BaseMessageUpdate -> this.data.chat.id.chatId
        is CallbackQueryUpdate -> {
            when (val callbackQuery = this.data) {
                is MessageCallbackQuery -> callbackQuery.message.chat.id.chatId
                else -> throw IllegalArgumentException("Unknown update type")
            }
        }

        is MyChatMemberUpdatedUpdate -> this.data.chat.id.chatId
        else -> throw IllegalArgumentException("Unknown update type")
    }
