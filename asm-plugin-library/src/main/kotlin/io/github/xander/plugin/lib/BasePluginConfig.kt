package io.github.xander.plugin.lib

open class BasePluginConfig {
    /** 是否开启日志 */
    var log = true

    /** 是否打印 visit class 日志 */
    var classLog = false

    /** 是否打印 visit method 日志 */
    var methodLog = false

    /** 是否在 debug 模式下跳过处理 */
    var debugSkip = false

    /** 是否在 release 模式下跳过处理 */
    var releaseSkip = false

    /** 是否跳过处理 jar */
    var skipJar = true

    /** 是否启动线程池 */
    var useExecutor = false


    companion object {
        val debug = BasePluginConfig()
    }

    override fun toString(): String {
        return "BasePluginConfig(log=$log, classLog=$classLog, methodLog=$methodLog, debugSkip=$debugSkip, releaseSkip=$releaseSkip, skipJar=$skipJar', useExecutor=$useExecutor)"
    }

}