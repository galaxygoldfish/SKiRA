package com.skira.app.utilities

/**
 * Attempts to start a second instance of the current process.
 *
 * Returns true when a new process is launched.
 */
fun relaunchCurrentProcess(): Boolean {
    val command = ProcessHandle.current().info().command().orElse(null) ?: return false
    val args = ProcessHandle.current().info().arguments().orElse(emptyArray())
    val fullCommand = listOf(command) + args
    return runCatching {
        ProcessBuilder(fullCommand).start()
        true
    }.getOrDefault(false)
}

