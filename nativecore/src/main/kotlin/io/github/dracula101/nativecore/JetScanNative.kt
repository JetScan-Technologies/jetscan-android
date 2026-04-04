package io.github.dracula101.nativecore

object JetScanNative {
    init {
        System.loadLibrary("jetscan")
    }

    val isLoaded: Boolean get() = true
}
