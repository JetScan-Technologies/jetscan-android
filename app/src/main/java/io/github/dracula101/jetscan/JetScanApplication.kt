package io.github.dracula101.jetscan

import android.app.Application
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.FormatStrategy
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class JetScanApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        attachLogger()
    }

    private fun attachLogger() {
        val formatStrategy: FormatStrategy = PrettyFormatStrategy.newBuilder()
            .showThreadInfo(false)
            .methodCount(1) // (Optional) How many method line to show. Default 2
            .methodOffset(5) // Set methodOffset to 5 in order to hide internal method calls
            .tag("") // To replace the default PRETTY_LOGGER tag with a dash (-).
            .build()

        Logger.addLogAdapter(AndroidLogAdapter(formatStrategy))
        if (BuildConfig.DEBUG) {
            Timber.plant(object : Timber.DebugTree() {
                override fun log(
                    priority: Int, tag: String?, message: String, t: Throwable?
                ) = Logger.log(priority, "-$tag", message, t)

                override fun createStackElementTag(element: StackTraceElement): String {
                    return super.createStackElementTag(element) + ":" + element.lineNumber
                }

                override fun d(message: String?, vararg args: Any?) = Logger.d(message ?: "", args)

                override fun e(message: String?, vararg args: Any?) = Logger.e(message ?: "", args)

                override fun i(message: String?, vararg args: Any?) = Logger.i(message ?: "", args)

                override fun v(message: String?, vararg args: Any?) = Logger.v(message ?: "", args)

                override fun w(message: String?, vararg args: Any?) = Logger.w(message ?: "", args)

            })
        }
    }
}