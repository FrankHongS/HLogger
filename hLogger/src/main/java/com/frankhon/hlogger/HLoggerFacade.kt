package com.frankhon.hlogger

import android.os.Build
import android.util.Log
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created by shuaihua_a on 2023/4/7 18:41.
 * E-mail: hongshuaihua
 */
class HLoggerFacade {

    companion object {
        private const val MAX_TAG_LENGTH = 23
    }

    private val fqcnIgnore = listOf(
        HLogger::class.java.name,
        HLoggerFacade::class.java.name
    )

    private val anonymousClassPattern = Pattern.compile("(\\$[a-zA-Z]+)+$")

    var shouldSaveLocal = false

    fun i(tag: String = "", message: String, vararg args: Any) {
        log(Log.INFO, tag, message, args)
    }

    fun w(tag: String = "", message: String, vararg args: Any) {
        log(Log.WARN, tag, message, args)
    }

    fun d(tag: String = "", message: String, vararg args: Any) {
        log(Log.DEBUG, tag, message, args)
    }

    fun e(tag: String = "", message: String, vararg args: Any) {
        log(Log.ERROR, tag, message, args)
    }

    fun e(tag: String = "", throwable: Throwable, message: String, vararg args: Any) {
        log(Log.ERROR, tag, "$message\n${Log.getStackTraceString(throwable)}", args)
    }

    private fun log(priority: Int, tag: String, message: String, vararg args: Any) {
        val realTag = tag.ifEmpty { findTag() }
        if (args.isNotEmpty()) {
            Log.println(priority, realTag, String.format(message, args))
        } else {
            Log.println(priority, realTag, message)
        }
        if (shouldSaveLocal) {
            writeLog2File()
        }
    }

    private fun writeLog2File() {

    }

    private fun findTag(): String {
        val stackTraceElements = Throwable().stackTrace
        for (element in stackTraceElements) {
            if (!fqcnIgnore.contains(element.className.split("\\$").toTypedArray()[0])
            ) {
                return createStackElementTag(element)
            }
        }
        return javaClass.name
    }

    private fun createStackElementTag(element: StackTraceElement): String {
        val className = element.className
        var tag = className.substring(className.lastIndexOf('.') + 1)
        val matcher: Matcher = anonymousClassPattern.matcher(tag)
        if (matcher.find()) {
            tag = matcher.replaceAll("")
        }
        if (tag.length > MAX_TAG_LENGTH) {
            tag = tag.substring(0, MAX_TAG_LENGTH)
        }
        return tag
    }
}