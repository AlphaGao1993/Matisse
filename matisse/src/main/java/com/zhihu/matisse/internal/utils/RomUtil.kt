package com.zhihu.matisse.internal.utils

import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Created by alpha on 2018/2/26.
 * 识别 ROM 信息
 */
object RomUtil {

    private const val COLOR_OS = "ro.build.version.opporom"
    private const val MIUI_OS = "ro.miui.ui.version.name"
    private const val RUNTIME_DISPLAY = "ro.build.display.id"

    fun isColorOs(): Boolean {
        return getRomProperty(COLOR_OS).isNotBlank()
    }

    /** 判断是否Flyme4以上  */
    fun isFlyme4Later(): Boolean {
        val romInfo = getRomProperty(RUNTIME_DISPLAY)
        return romInfo.contains("Flyme") && romInfo.substring(6, 7).toInt() >= 4
    }

    /** 判断是否为MIUI6以上  */
    fun isMIUI6Later(): Boolean {
        val info = getRomProperty(MIUI_OS)
        return info.isNotEmpty() && info.substring(1).toInt() >= 6
    }
}

private fun getRomProperty(prop: String): String {
    var line = ""
    var reader: BufferedReader? = null
    var p: Process? = null
    try {
        p = Runtime.getRuntime().exec("getprop " + prop)
        reader = BufferedReader(InputStreamReader(p.inputStream), 1024)
        line = reader.readLine()
    } catch (e: Exception) {
    } finally {
        reader?.close()
        p?.destroy()
        return line
    }
}