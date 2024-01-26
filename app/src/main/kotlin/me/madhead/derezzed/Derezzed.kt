@file:JvmName("Derezzed")

package me.madhead.derezzed

import io.ktor.server.config.ConfigLoader
import io.ktor.server.config.ConfigLoader.Companion.load
import io.ktor.server.engine.addShutdownHook
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.engine.stop
import io.ktor.server.netty.Netty
import me.madhead.derezzed.modules.derezzed
import java.util.concurrent.TimeUnit

fun main() {
    val env = applicationEngineEnvironment {
        config = ConfigLoader.load()

        connector {
            port = config.property("deployment.port").getString().toInt()
        }
        connector {
            port = config.property("deployment.managementPort").getString().toInt()
        }

        module { derezzed() }
    }
    val engine = embeddedServer(Netty, env)

    engine.addShutdownHook {
        engine.stop(gracePeriod = 3, timeout = 5, TimeUnit.SECONDS)
    }

    engine.start(true)
}
