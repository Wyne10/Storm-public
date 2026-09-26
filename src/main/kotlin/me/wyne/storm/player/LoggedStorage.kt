package me.wyne.storm.player

import me.wyne.storm.Storm
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.concurrent.RejectedExecutionException

internal fun loggedWrite(executor: Executor, description: String, action: () -> Unit): CompletableFuture<Void> {
    val future = try {
        CompletableFuture.runAsync(action, executor)
    } catch (e: RejectedExecutionException) {
        CompletableFuture.failedFuture(e)
    }
    return future.whenComplete { _, error ->
        if (error != null)
            Storm.logger.error("Couldn't {}", description, error)
    }
}

internal fun <T> loggedRead(executor: Executor, description: String, fallback: T, action: () -> T): CompletableFuture<T> {
    val future = try {
        CompletableFuture.supplyAsync(action, executor)
    } catch (e: RejectedExecutionException) {
        CompletableFuture.failedFuture<T>(e)
    }
    return future.exceptionally { error ->
        Storm.logger.error("Couldn't {}", description, error)
        fallback
    }
}
